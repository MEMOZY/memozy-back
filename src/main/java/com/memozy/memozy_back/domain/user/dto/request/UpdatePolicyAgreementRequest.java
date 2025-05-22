package com.memozy.memozy_back.domain.user.dto.request;

import com.memozy.memozy_back.domain.user.dto.PolicyAgreementDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdatePolicyAgreementRequest(
        @Valid @Size(min = 1) List<PolicyAgreementDto> policyAgreementList
) {

}