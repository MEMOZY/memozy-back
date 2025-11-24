package com.memozy.memozy_back.domain.memory.dto.response;

public record CreateEditLockResponse(
        boolean acquired,      // 락 획득 성공 여부
        String token,          // 성공 시에만 채움
        Long ttl,              // ms 단위 TTL (성공 시: 전체 TTL, 실패 시: 남은 TTL 등을 넣어도 됨)
        Long holderId,         // 실패 시: 락을 잡고 있는 유저 id
        String holderNickname  // 실패 시: 락을 잡고 있는 유저 닉네임
) {
    public static CreateEditLockResponse acquired(String token, long ttl) {
        return new CreateEditLockResponse(true, token, ttl, null, null);
    }

    public static CreateEditLockResponse lockedBy(Long holderId, String holderNickname) {
        return new CreateEditLockResponse(false, null, null, holderId, holderNickname);
    }
}
