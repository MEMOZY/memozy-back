package com.memozy.memozy_back.domain.alert.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PushAlertMsg {
    SHARED_MEMORY_TITLE("새로운 메모지가 공유되었어요!"),
    SHARED_MEMORY_BODY("%s님이 메모지를 공유했어요. 확인해보세요!");

    private final String msg;

}
