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
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemoryEditLockService {

    private final StringRedisTemplate redis;
    private final UserRepository userRepository;

    private static final Duration TTL = Duration.ofMinutes(3);
    private static final String KEY_FMT = "memory:%d:edit-lock";

    // Hash 필드명
    private static final byte[] F_USER  = "userId".getBytes(StandardCharsets.UTF_8);
    private static final byte[] F_TOKEN = "token".getBytes(StandardCharsets.UTF_8);
    private final MemoryService memoryService;
    private final MemoryRepository memoryRepository;

    private String key(Long memoryId) { return KEY_FMT.formatted(memoryId); }
    private byte[] k(Long memoryId) { return key(memoryId).getBytes(StandardCharsets.UTF_8); }

    /**
     * 락 획득 (편집 진입): 키가 없을 때만 HMSET + PEXPIRE (원자적)
     */
    public CreateEditLockResponse acquire(Long memoryId, Long userId) {
        Memory memory = memoryRepository.findById(memoryId).orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND_MEMORY));
        if (!memory.canEdit(userId)) {
            throw new GlobalException(ErrorCode.INVALID_PERMISSION_LEVEL);
        }

        String token = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();

        // 키가 없으면 생성하고 TTL 설정, 있으면 실패
        String ACQUIRE_SCRIPT = """
            if (redis.call('EXISTS', KEYS[1]) == 1) then
              return 0
            else
              redis.call('HSET', KEYS[1], 'userId', ARGV[1], 'token', ARGV[2], 'acquiredAt', ARGV[3])
              redis.call('PEXPIRE', KEYS[1], ARGV[4])
              return 1
            end
            """;

        Long result = evalLong(ACQUIRE_SCRIPT,
                new byte[][] { k(memoryId) },
                new byte[][] {
                        String.valueOf(userId).getBytes(StandardCharsets.UTF_8),
                        token.getBytes(StandardCharsets.UTF_8),
                        String.valueOf(now).getBytes(StandardCharsets.UTF_8),
                        String.valueOf(TTL.toMillis()).getBytes(StandardCharsets.UTF_8)
                });

        // 락 획득 성공
        if (result != null && result == 1L) {
            return CreateEditLockResponse.acquired(token, TTL.toMillis());
        }

        // 이미 락이 있는 경우 → holder 정보 조회
        Long holderId = getLockHolderUserId(memoryId);
        String holderNickname = null;

        if (holderId != null) {
            holderNickname = userRepository.findById(holderId)
                    .map(User::getNickname)
                    .orElse(null);
        }

        return CreateEditLockResponse.lockedBy(holderId, holderNickname);
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