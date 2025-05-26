package com.memozy.memozy_back.global.redis;

import com.memozy.memozy_back.domain.file.service.FileService;
import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.dto.TempMemoryDto;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class TemporaryMemoryStoreImpl implements TemporaryMemoryStore {

    private final RedisTemplate<String, TempMemoryDto> memoryRedisTemplate;
    private final UserRepository userRepository;
    private final FileService fileService;
    private static final Duration TTL = Duration.ofMinutes(1440);

    @Override
    public void save(String sessionId, TempMemoryDto tempMemoryDto) {
        memoryRedisTemplate.opsForValue().set(sessionId, tempMemoryDto, TTL);
    }

    @Override
    public Memory load(String sessionId) {
        TempMemoryDto dto = memoryRedisTemplate.opsForValue().get(sessionId);
        if (dto == null) throw new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION);

        User owner = userRepository.findById(dto.ownerId()) // 또는 인자로 넘겨받음
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER_EXCEPTION));

        return dto.toDomain(owner, fileService);
    }

    @Override
    public void remove(String sessionId) {
        memoryRedisTemplate.delete(sessionId);
    }
}