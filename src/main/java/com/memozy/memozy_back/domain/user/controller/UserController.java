package com.memozy.memozy_back.domain.user.controller;

import com.memozy.memozy_back.domain.user.dto.request.UpdateUserRequest;
import com.memozy.memozy_back.domain.user.dto.UserInfoDto;
import com.memozy.memozy_back.domain.user.facade.UserFacade;
import com.memozy.memozy_back.global.annotation.V1;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "유저 관련 API", description = "")
@V1
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserFacade userFacade;

    @GetMapping()
    public ResponseEntity<UserInfoDto> getUser(Long userId) {
        return ResponseEntity.ok(userFacade.getUserWithInfo(userId));
    }

    @PatchMapping()
    public ResponseEntity<UserInfoDto> updateUser(
            Long userId,
            @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        return ResponseEntity.ok(userFacade.updateUserWithInfo(userId, updateUserRequest));
    }

//    @GetMapping("/policy-agreement")
//    public ResponseEntity<GetPolicyAgreementResponse> getPolicyAgreement(
//            @CurrentUserId Long userId) {
//        return ResponseEntity.ok(
//                GetPolicyAgreementResponse.of(userFacade.getPolicyAgreementList(userId)));
//    }

//    @PutMapping("/policy-agreement")
//    public ResponseEntity<UpdatePolicyAgreementResponse> updatePolicyAgreement(
//            Long userId,
//            @Valid @RequestBody UpdatePolicyAgreementRequest updatePolicyAgreementRequest) {
//        if (updatePolicyAgreementRequest.policyAgreementList().stream()
//                .map(PolicyAgreementDto::policyType).distinct().count()
//                != updatePolicyAgreementRequest.policyAgreementList().size()) {
//            throw new BusinessException(ErrorCode.DUPLICATED_POLICY_REQUEST_EXCEPTION);
//        }
//
//        return ResponseEntity.ok(
//                UpdatePolicyAgreementResponse.of(userFacade.updatePolicyAgreement(userId,
//                        updatePolicyAgreementRequest.policyAgreementList())));
//    }

    @DeleteMapping()
    public ResponseEntity<Void> withdrawUser(@RequestHeader("X-USER-ID") Long userId) {
        userFacade.withdrawUser(userId);
        return ResponseEntity.noContent().build();
    }

}