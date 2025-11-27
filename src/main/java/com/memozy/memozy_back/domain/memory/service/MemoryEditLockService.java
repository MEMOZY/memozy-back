package com.memozy.memozy_back.domain.memory.service;

import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.dto.response.CreateEditLockResponse;
import com.memozy.memozy_back.domain.memory.dto.response.UpdateEditLockTTLResponse;
import com.memozy.memozy_back.domain.memory.repository.MemoryRepository;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.exception.ErrorCode;
import com.memozy.memozy_back.global.exception.GlobalException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryEditLockService {

    private final StringRedisTemplate redis;
    private final UserRepository userRepository;

    private static final Duration TTL = Duration.ofMinutes(3);
    private static final String KEY_FMT = "memory:%d:edit-lock";

    // 클래스 필드로 빼두면 좋음
    private static final String ACQUIRE_SCRIPT = """
    local exists = redis.call('EXISTS', KEYS[1])
    if exists == 0 then
      -- 1) 락이 아직 없음: 새로 생성
      redis.call('HSET', KEYS[1],
        'userId', ARGV[1],
        'token', ARGV[2],
        'acquiredAt', ARGV[3])
      redis.call('PEXPIRE', KEYS[1], ARGV[4])
      -- {status, holderUserId}
      return {1, ARGV[1]}  -- 새로 획득
    else
      -- 2) 이미 락이 있음: 같은 사용자 락인지 검사
      local lockUserId = redis.call('HGET', KEYS[1], 'userId')

      if lockUserId == ARGV[1] then
        -- 같은 userId → 재요청(멱등)으로 보고 token/TTL 갱신
        redis.call('HSET', KEYS[1],
          'token', ARGV[2],
          'acquiredAt', ARGV[3])
        redis.call('PEXPIRE', KEYS[1], ARGV[4])
        return {2, lockUserId}  -- 재획득(멱등 성공)
      else
        -- 다른 사용자가 이미 잡은 락
        return {0, lockUserId}  -- 실패 + holderUserId 반환
      end
    end
    """;

    // Hash 필드명
    private static final byte[] F_USER  = "userId".getBytes(StandardCharsets.UTF_8);
    private static final byte[] F_TOKEN = "token".getBytes(StandardCharsets.UTF_8);
    private final MemoryRepository memoryRepository;

    private String key(Long memoryId) { return KEY_FMT.formatted(memoryId); }
    private byte[] k(Long memoryId) { return key(memoryId).getBytes(StandardCharsets.UTF_8); }

    /**
     * 락 획득 (편집 진입): 키가 없을 때만 HMSET + PEXPIRE (원자적)
     */
    public CreateEditLockResponse acquire(Long memoryId, Long userId) {
        // 1) 도메인 권한 체크
        Memory memory = memoryRepository.findByIdWithAccesses(memoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND_MEMORY));
        if (!memory.canEdit(userId)) {
            throw new GlobalException(ErrorCode.INVALID_PERMISSION_LEVEL);
        }

        // 2) 이번 요청에서 사용할 token 생성
        String token = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();

        // 3) Lua 스크립트 실행 → [status, holderUserId] 형태로 결과 수신
        List<Object> result = getEditLockInfo(memoryId, userId, token, now);
        log.info("acquire() eval result = {}", result);
        if (result == null || result.size() < 2) {
            throw new GlobalException(ErrorCode.LOCK_OPERATION_FAILED); // 필요시 에러코드 추가
        }

        Long status = parseLong(result.get(0));
        Long holderId = parseLong(result.get(1)); // 없으면 null

        // 4) status에 따른 분기 처리
        if (status != null && (status == 1L || status == 2L)) {
            // 1: 새로 획득, 2: 내가 이미 잡고 있는 락을 재요청 (멱등)
            // 둘 다 "현재 편집 락은 나에게 있음"
            return CreateEditLockResponse.acquired(token, TTL.toMillis());
        }

        // 0: 다른 사용자가 보유 중
        String holderNickname = null;
        if (holderId != null) {
            holderNickname = userRepository.findById(holderId)
                    .map(User::getNickname)
                    .orElse(null);
        }

        return CreateEditLockResponse.lockedBy(holderId, holderNickname);
    }

    @SuppressWarnings("unchecked")
    private List<Object> getEditLockInfo(Long memoryId, Long userId, String token, long nowMillis) {
        byte[] key = k(memoryId);

        byte[] aUserId = String.valueOf(userId).getBytes(StandardCharsets.UTF_8);
        byte[] aToken  = token.getBytes(StandardCharsets.UTF_8);
        byte[] aNow    = String.valueOf(nowMillis).getBytes(StandardCharsets.UTF_8);
        byte[] aTtl    = String.valueOf(TTL.toMillis()).getBytes(StandardCharsets.UTF_8);

        return redis.execute(
                // 1) RedisCallback 구현체 (con이 바로 Redis connection)
                (con) -> {
                    // 2) Lua eval 실행
                    Object raw = con
                            .scriptingCommands()
                            .eval(
                                    ACQUIRE_SCRIPT.getBytes(StandardCharsets.UTF_8),
                                    ReturnType.MULTI,   // 배열 반환
                                    1,                  // key 개수
                                    key,                // KEYS[1]
                                    aUserId, aToken, aNow, aTtl // ARGV[1..4]
                            );

                    // 3) null이면 에러
                    if (raw == null) {
                        log.error("Redis EVAL returned null");
                        return null;
                    }

                    // 4) 결과가 List가 아니면 이상한 상태
                    if (!(raw instanceof List<?> list)) {
                        log.error("Unexpected EVAL result type: {}", raw.getClass());
                        return null;
                    }

                    // 5) 이제 [ elem0, elem1 ] 이런 식이라 가정
                    // elem은 보통 byte[] 이라서 String으로 바꿔줌
                    List<Object> decoded = list.stream()
                            .map(elem -> {
                                if (elem instanceof byte[] bytes) {
                                    return new String(bytes, StandardCharsets.UTF_8);
                                }
                                return elem;
                            })
                            .toList();

                    log.info("EVAL decoded result = {}", decoded);
                    return decoded;
                },
                false,  // exposeConnection
                false   // pipeline (★ 파이프라인 끔)
        );
    }

    private Long parseLong(Object raw) {
        if (raw == null) return null;
        try {
            return Long.parseLong(raw.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 하트비트(연장): 소유자/토큰 일치 시에만 TTL 연장 (원자적)
     */
    public UpdateEditLockTTLResponse heartbeat(Long memoryId, Long userId, String token) {
        String HEARTBEAT_SCRIPT = """
        local uid = redis.call('HGET', KEYS[1], 'userId')
        local tok = redis.call('HGET', KEYS[1], 'token')
        if (not uid) or (not tok) then
          return 0
        end
        if (uid ~= ARGV[1]) or (tok ~= ARGV[2]) then
          return -1
        end

        local ttl = redis.call('PTTL', KEYS[1])
        if (ttl < 0) then
          ttl = 0
        end

        local newTtl = ttl + tonumber(ARGV[3])
        redis.call('PEXPIRE', KEYS[1], newTtl)
        return newTtl
        """;

        Long result = evalLong(HEARTBEAT_SCRIPT,
                new byte[][] { k(memoryId) },
                new byte[][] {
                        String.valueOf(userId).getBytes(StandardCharsets.UTF_8),
                        token.getBytes(StandardCharsets.UTF_8),
                        String.valueOf(TTL.toMillis()).getBytes(StandardCharsets.UTF_8)
                });

        if (result == null || result == 0L) {
            throw new GlobalException(ErrorCode.LOCK_NOT_FOUND);
        }
        if (result == -1L) {
            throw new GlobalException(ErrorCode.INVALID_LOCK_TOKEN_EXCEPTION);
        }

        return new UpdateEditLockTTLResponse(result); // 연장된 TTL 반환
    }

    /**
     * 저장 전 검증: userId + token 모두 확인 (Hash 필드 읽기)
     */
    public void verifyOwner(Long memoryId, Long userId, String token) {
        List<Object> vals = redis.executePipelined((RedisCallback<Object>) con -> {
            con.hashCommands().hGet(k(memoryId), F_USER);
            con.hashCommands().hGet(k(memoryId), F_TOKEN);
            return null;
        });

        String storedUserId = vals.get(0) == null ? null : (String) vals.get(0);
        String storedToken  = vals.get(1) == null ? null : (String) vals.get(1);

        if (storedUserId == null || storedToken == null) {
            throw new GlobalException(ErrorCode.LOCK_NOT_FOUND);
        }
        if (!storedUserId.equals(String.valueOf(userId)) || !storedToken.equals(token)) {
            throw new GlobalException(ErrorCode.INVALID_LOCK_TOKEN_EXCEPTION);
        }
    }

    /**
     * 해제: userId + token 일치 시에만 DEL (원자적)
     */
    public void release(Long memoryId, Long userId, String token) {
        String RELEASE_SCRIPT = """
            local uid = redis.call('HGET', KEYS[1], 'userId')
            local tok = redis.call('HGET', KEYS[1], 'token')
            if (not uid) or (not tok) then
              return 0
            end
            if (uid == ARGV[1]) and (tok == ARGV[2]) then
              return redis.call('DEL', KEYS[1])
            else
              return -1
            end
            """;

        Long result = evalLong(RELEASE_SCRIPT,
                new byte[][] { k(memoryId) },
                new byte[][] {
                        String.valueOf(userId).getBytes(StandardCharsets.UTF_8),
                        token.getBytes(StandardCharsets.UTF_8)
                });

        if (result == null || result == 0L) {
            throw new GlobalException(ErrorCode.LOCK_NOT_FOUND);
        }
        if (result == -1L) {
            throw new GlobalException(ErrorCode.LOCK_RELEASE_FAILED);
        }
    }

    // ---- 내부 유틸 ----
    private Long getLockHolderUserId(Long memoryId) {
        String key = key(memoryId); // k(memoryId)와 짝 맞는 String 버전

        Object raw = redis.opsForHash().get(key, "userId");
        if (raw == null) return null;

        return Long.parseLong(raw.toString());
    }

    private Long evalLong(String script, byte[][] keys, byte[][] args) throws DataAccessException {
        return redis.execute((RedisCallback<Long>) con ->
                con.scriptingCommands().eval(
                        script.getBytes(StandardCharsets.UTF_8),
                        ReturnType.INTEGER,
                        keys.length,
                        concat(keys, args)
                )
        );
    }

    private byte[][] concat(byte[][] a, byte[][] b) {
        byte[][] res = new byte[a.length + b.length][];
        System.arraycopy(a, 0, res, 0, a.length);
        System.arraycopy(b, 0, res, a.length, b.length);
        return res;
    }
}