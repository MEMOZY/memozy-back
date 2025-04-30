package com.memozy.memozy_back.domain.gpt.dto;

public record ChatMessage(String role, String content) {
    public static ChatMessage user(String content) {
        return new ChatMessage("user", content);
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content);
    }
}