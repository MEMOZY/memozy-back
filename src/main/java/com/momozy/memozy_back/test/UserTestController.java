package com.momozy.memozy_back.test;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class UserTestController {

    private final UserService userService;

    @PostMapping("/user")
    public User createUser() {
        return userService.createTestUser();
    }
}
