package com.momozy.memozy_back.test;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createTestUser() {
        User user = User.builder()
                .nickname("테스트유저")
                .email("test@memozy.com")
                .build();

        return userRepository.save(user);
    }
}
