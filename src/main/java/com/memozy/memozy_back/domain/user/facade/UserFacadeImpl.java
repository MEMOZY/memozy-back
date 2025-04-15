package com.memozy.memozy_back.domain.user.facade;

import com.memozy.memozy_back.domain.user.dto.UserInfoDto;
import com.memozy.memozy_back.domain.user.dto.request.UpdateUserRequest;
import com.memozy.memozy_back.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserFacadeImpl implements UserFacade {

    private final UserService userService;

    @Override
    public UserInfoDto getUserWithInfo(Long userId) {
        return UserInfoDto.from(userService.getById(userId));
    }

    @Override
    public UserInfoDto updateUserWithInfo(Long userId, UpdateUserRequest updateUserRequest) {
        return UserInfoDto.from(userService.updateUserWithInfo(userId, updateUserRequest));
    }

//    @Override
//    public List<PolicyAgreementDto> getPolicyAgreementList(Long userId) {
//        return userService.getPolicyAgreementList(userId).stream()
//                .map(PolicyAgreementDto::from)
//                .toList();
//    }
//
//    @Override
//    public List<PolicyAgreementDto> updatePolicyAgreement(Long userId,
//            List<PolicyAgreementDto> policyAgreementDtoList) {
//        return userService.updatePolicyAgreement(userId, policyAgreementDtoList)
//                .stream()
//                .map(PolicyAgreementDto::from)
//                .toList();
//    }
//

    @Override
    @Transactional
    public void withdrawUser(Long userId) {
        userService.withdrawUser(userId);
    }
}