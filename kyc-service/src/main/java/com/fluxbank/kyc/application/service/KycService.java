package com.fluxbank.kyc.application.service;

import com.fluxbank.kyc.application.dto.KycDto;
import com.fluxbank.kyc.application.dto.ReviewKycRequest;
import com.fluxbank.kyc.application.dto.SubmitKycRequest;
import java.util.UUID;

public interface KycService {
    KycDto submitKyc(UUID userId, SubmitKycRequest request);
    KycDto getKyc(UUID userId);
    KycDto reviewKyc(UUID kycId, ReviewKycRequest request);
}
