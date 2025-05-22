package com.memozy.memozy_back.domain.user.dto;

import com.memozy.memozy_back.domain.user.constant.PolicyType;
import com.memozy.memozy_back.domain.user.domain.UserPolicyAgreement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.Builder;

@Builder
public record PolicyAgreementDto(
        @Schema(description = "서비스 이용약관, 개인정보 처리방침", example = "SERVICE_POLICY, PRIVACY_POLICY,")
        @NotNull PolicyType policyType,
        @Schema(description = "서비스 이용약관/개인정보 처리방침 버전", example = "v1.0")
        @NotNull String version,
        @NotNull Boolean isAgree,
        OffsetDateTime updatedAt
) {

    public static PolicyAgreementDto from(UserPolicyAgreement userPolicyAgreement) {
        return PolicyAgreementDto.builder()
                .policyType(userPolicyAgreement.getPolicyType())
                .version(userPolicyAgreement.getVersion())
                .isAgree(userPolicyAgreement.getIsAgree())
                .updatedAt(userPolicyAgreement.getUpdatedAt())
                .build();
    }

    public static PolicyAgreementDto of(
            PolicyType policyType,
            String version,
            Boolean isAgree,
            OffsetDateTime UpdatedAt) {
        return new PolicyAgreementDto(policyType, version, isAgree, UpdatedAt);
    }
}
