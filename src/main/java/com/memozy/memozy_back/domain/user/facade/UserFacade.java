package com.memozy.memozy_back.domain.user.facade;

import com.memozy.memozy_back.domain.user.dto.UserInfoDto;
import com.memozy.memozy_back.domain.user.dto.request.UpdateUserRequest;
import java.util.List;

public interface UserFacade {

    UserInfoDto getUserWithInfo(Long userId);

    default UserInfoDto updateUserWithInfo(Long userId, UpdateUserRequest updateUserRequest) {
        return null;
    }

//    List<PolicyAgreementDto> getPolicyAgreementList(Long userId);

//    List<PolicyAgreementDto> updatePolicyAgreement(Long userId, List<PolicyAgreementDto> policyAgreementDtoList);

    void withdrawUser(Long userId);
}