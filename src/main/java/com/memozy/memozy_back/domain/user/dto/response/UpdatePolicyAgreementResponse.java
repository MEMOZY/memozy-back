package com.memozy.memozy_back.domain.user.dto.response;

import com.memozy.memozy_back.domain.user.dto.PolicyAgreementDto;
import java.util.List;

public record UpdatePolicyAgreementResponse(List<PolicyAgreementDto> policyAgreementList) {
    public static UpdatePolicyAgreementResponse of(List<PolicyAgreementDto> policyAgreementDtoList) {
        return new UpdatePolicyAgreementResponse(policyAgreementDtoList);
    }
}
