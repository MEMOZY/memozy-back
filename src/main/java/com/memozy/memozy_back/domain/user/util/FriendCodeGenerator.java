package com.memozy.memozy_back.domain.user.util;

import java.security.SecureRandom;

public class FriendCodeGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEFAULT_LENGTH = 8; // 원하는 길이

    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomId() {
        return generateRandomId(DEFAULT_LENGTH);
    }

    public static String generateRandomId(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }
}