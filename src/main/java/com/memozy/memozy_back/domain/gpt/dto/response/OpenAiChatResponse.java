package com.memozy.memozy_back.domain.gpt.dto.response;

import java.util.List;

public record OpenAiChatResponse(
        List<Choice> choices
) {
    public record Choice(
            Message message
    ) {}

    public record Message(
            String role,
            String content
    ) {}
}