package com.memozy.memozy_back.domain.user.controller;

import com.memozy.memozy_back.domain.user.dto.PolicyAgreementDto;
import com.memozy.memozy_back.domain.user.dto.request.UpdatePolicyAgreementRequest;
import com.memozy.memozy_back.domain.user.dto.request.UpdateUserRequest;
import com.memozy.memozy_back.domain.user.dto.response.GetPolicyAgreementResponse;
import com.memozy.memozy_back.domain.user.dto.response.GetUserInfoResponse;
import com.memozy.memozy_back.domain.user.dto.response.GetUserProfileResponse;
import com.memozy.memozy_back.domain.user.dto.response.UpdatePolicyAgreementResponse;
import com.memozy.memozy_back.domain.user.dto.response.UpdateUserResponse;
import com.memozy.memozy_back.domain.user.facade.UserFacade;
import com.memozy.memozy_back.global.annotation.CurrentUserId;
import com.memozy.memozy_back.global.exception.GlobalException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "유저 관련 API", description = "")
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserFacade userFacade;

    @GetMapping()
    public ResponseEntity<GetUserProfileResponse> getUser(
            @CurrentUserId Long userId) {
        return ResponseEntity.ok(
                userFacade.getUser(userId)
        );
    }

    @GetMapping("/info/{userId}")
    public ResponseEntity<GetUserInfoResponse> getUserInfoById(
            @Valid @PathVariable Long userId) {
        return ResponseEntity.ok(
                userFacade.getUserInfoById(userId)
        );
    }

    @GetMapping("/{friendCode}")
    public ResponseEntity<GetUserInfoResponse> getUserInfoById(
            @Valid @PathVariable String friendCode) {
        return ResponseEntity.ok(
                userFacade.getUserInfoByFriendCode(friendCode)
        );
    }

    @PatchMapping()
    public ResponseEntity<UpdateUserResponse> updateUser(
            @CurrentUserId Long userId,
            @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        return ResponseEntity.ok(
                userFacade.updateUserWithInfo(userId, updateUserRequest)
        );
    }



    @GetMapping("/policy-agreement")
    public ResponseEntity<GetPolicyAgreementResponse> getPolicyAgreement(
            @CurrentUserId Long userId) {
        return ResponseEntity.ok(
                GetPolicyAgreementResponse.of(userFacade.getPolicyAgreementList(userId)));
    }

    @PutMapping("/policy-agreement")
    public ResponseEntity<UpdatePolicyAgreementResponse> updatePolicyAgreement(
            @CurrentUserId Long userId,
            @Valid @RequestBody UpdatePolicyAgreementRequest updatePolicyAgreementRequest) {
        if (updatePolicyAgreementRequest.policyAgreementList().stream()
                .map(PolicyAgreementDto::policyType).distinct().count()
                != updatePolicyAgreementRequest.policyAgreementList().size()) {
            throw new GlobalException(ErrorCode.DUPLICATED_POLICY_REQUEST_EXCEPTION);
        }

        return ResponseEntity.ok(
                UpdatePolicyAgreementResponse.of(userFacade.updatePolicyAgreement(userId,
                        updatePolicyAgreementRequest.policyAgreementList())));
    }

    @DeleteMapping()
    public ResponseEntity<Void> withdrawUser(
            @CurrentUserId Long userId) {
        userFacade.withdrawUser(userId);
        return ResponseEntity.noContent().build();
    }

}