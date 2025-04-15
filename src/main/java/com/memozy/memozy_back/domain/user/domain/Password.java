package com.memozy.memozy_back.domain.user.domain;

import static com.memozy.memozy_back.domain.auth.exception.AuthErrorMessage.PASSWORD_IS_NULL;
import static com.memozy.memozy_back.domain.auth.util.ValidationUtils.validateNotBlank;
import static com.memozy.memozy_back.domain.auth.util.ValidationUtils.validatePassword;
import static com.memozy.memozy_back.global.exception.UserErrorMessage.PASSWORD_FORMAT_INVALID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Password {

    public static final String PASSWORD_REGXP = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@!%*#?&^]).{8,}$";

    @Column(name = "password", length = 100)
    private String encoded;

    public Password(String password, PasswordEncoder passwordEncoder) {
        validateNotBlank(password, PASSWORD_IS_NULL);
        validatePassword(password, PASSWORD_FORMAT_INVALID);
        encoded = passwordEncoder.encode(password);
    }

    public boolean check(String rawPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, encoded);
    }
}