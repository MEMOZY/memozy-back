package com.memozy.memozy_back.domain.user.facade;

import com.memozy.memozy_back.domain.user.dto.UserInfoDto;
import com.memozy.memozy_back.domain.user.dto.request.UpdateUserRequest;
import com.memozy.memozy_back.domain.user.dto.response.GetFriendCodeResponse;
import com.memozy.memozy_back.domain.user.dto.response.GetUserInfoResponse;

public interface UserFacade {

    UserInfoDto getUserWithInfo(Long userId);

    default UserInfoDto updateUserWithInfo(Long userId, UpdateUserRequest updateUserRequest) {
        return null;
    }

//    List<PolicyAgreementDto> getPolicyAgreementList(Long userId);

//    List<PolicyAgreementDto> updatePolicyAgreement(Long userId, List<PolicyAgreementDto> policyAgreementDtoList);

    void withdrawUser(Long userId);

    // 타 유저 정보 조회
    GetUserInfoResponse getUserInfoByFriendCode(String friendCode);

    // 친구 코드 조회
    GetFriendCodeResponse getFriendCode(Long userId);
}