package com.memozy.memozy_back.domain.user.service;

import com.memozy.memozy_back.domain.file.service.FileService;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.domain.UserPolicyAgreement;
import com.memozy.memozy_back.domain.user.dto.PolicyAgreementDto;
import com.memozy.memozy_back.domain.user.dto.request.UpdateUserRequest;
import com.memozy.memozy_back.domain.user.repository.UserPolicyAgreementRepository;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserPolicyAgreementRepository userPolicyAgreementRepository;
    private final FileService fileService;

    @Override
    @Transactional(readOnly = true)
    public User getById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(
                ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));
    }

    @Override
    @Transactional
    public User updateUserWithInfo(Long userId, UpdateUserRequest updateUserRequest) {
        var user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(
                ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));
        String profileImageUrl = updateUserRequest.profileImageUrl();
        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            // 수정하려는 이미지가 s3에 올라와있는지 검증
            fileService.validateFileKey(
                    fileService.extractFileKeyFromImageUrl(profileImageUrl)
            );
        }

        user.updateUserInfo(updateUserRequest);
        return user;
    }

    public List<UserPolicyAgreement> getPolicyAgreementList(Long userId) {
        return userPolicyAgreementRepository.findAllByUserId(userId).stream()
                .toList();
    }

    @Override
    @Transactional
    public List<UserPolicyAgreement> updatePolicyAgreement(Long userId,
            List<PolicyAgreementDto> policyAgreementDtoList) {
        var user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(
                ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));
        var existPolicyAgreementList = userPolicyAgreementRepository.findAllByUserId(userId);

        var policyAgreementListForSave = policyAgreementDtoList.stream().map(
                dto -> existPolicyAgreementList.stream()
                        .filter(
                                policyAgreement -> policyAgreement.getPolicyType().equals(dto.policyType()))
                        .findFirst()
                        .map(policyAgreement -> policyAgreement.updateIsAgree(dto.isAgree()))
                        .orElse(UserPolicyAgreement.builder()
                                .user(user)
                                .policyType(dto.policyType())
                                .version(dto.version())
                                .isAgree(dto.isAgree())
                                .build())
        ).toList();

        return userPolicyAgreementRepository.saveAll(policyAgreementListForSave);
    }

    @Override
    @Transactional
    public void withdrawUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER_EXCEPTION));

        userRepository.delete(user); // 완전 삭제
    }

    @Override
    public String getFriendCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER_EXCEPTION));
        return user.getFriendCode();
    }

    @Override
    public User getUserByFriendCode(String friendCode) {
        return userRepository.findByFriendCode(friendCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER_EXCEPTION));
    }
}
