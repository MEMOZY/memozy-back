package com.memozy.memozy_back.domain.gpt.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PromptText {
    GENERATE_STORY("end"),
    FIRST_COMMENT_PROMPT("너는 사용자의 사진일기에 대해 먼저 사진을 보고 추측하여 사용자로부터 대화를 유도하는 역할이야. 이미지를 보고 '~~한 것같은 사진이네요. 이 사진에 대해 알려주세요!' 라는 식으로 말해."),
    TEXT_PROMPT("내가 너에게 사진에 대한 정보를 알려줄거야. 너는 잘 듣고 이후 일기 작성할 때 활용해줘. 추가 궁금한 게 있으면 질문해도 좋아. 대화는 이전 내용을 참고해서 자연스럽게 이어가."),
    IMG_PROMPT("너는 나의 그림일기를 대신 작성해주는 역할이야. 내가 너에게 사진을 보여주면 사진을 보고, 그리고 대화했던 내용을 참고해서 풍부한 일기를 작성해줘. 일기의 내용 외에는 쓰지마.");

    private final String text;

}
