package com.memozy.memozy_back.domain.user.dto.response;

import com.memozy.memozy_back.domain.user.dto.PolicyAgreementDto;
import java.util.List;

public record GetPolicyAgreementResponse(
        List<PolicyAgreementDto> policyAgreementList
) {
    public static GetPolicyAgreementResponse of(List<PolicyAgreementDto> policyAgreementDtoList) {
        return new GetPolicyAgreementResponse(policyAgreementDtoList);
    }
}
