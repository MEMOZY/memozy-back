package com.memozy.memozy_back.global.util;

import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.exception.ErrorCode;
import com.memozy.memozy_back.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EntityLoader {

    private final UserRepository userRepository;

    public User getUser(final Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND_USER_EXCEPTION));
    }

}
