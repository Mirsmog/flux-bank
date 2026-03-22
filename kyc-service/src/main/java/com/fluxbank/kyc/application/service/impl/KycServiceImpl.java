package com.fluxbank.kyc.application.service.impl;

import com.fluxbank.common.constants.KafkaTopics;
import com.fluxbank.common.exception.ErrorCode;
import com.fluxbank.common.exception.FluxBankException;
import com.fluxbank.kyc.application.dto.KycDto;
import com.fluxbank.kyc.application.dto.ReviewKycRequest;
import com.fluxbank.kyc.application.dto.SubmitKycRequest;
import com.fluxbank.kyc.application.mapper.KycMapper;
import com.fluxbank.kyc.application.service.KycService;
import com.fluxbank.kyc.domain.event.KycStatusChangedEvent;
import com.fluxbank.kyc.domain.event.KycSubmittedEvent;
import com.fluxbank.kyc.domain.model.KycRecord;
import com.fluxbank.kyc.domain.model.KycStatus;
import com.fluxbank.kyc.domain.repository.KycRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private final KycRepository kycRepository;
    private final KycMapper kycMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public KycDto submitKyc(UUID userId, SubmitKycRequest request) {
        Optional<KycRecord> existing = kycRepository.findByUserId(userId);

        if (existing.isPresent()) {
            KycRecord record = existing.get();
            if (record.getStatus() == KycStatus.APPROVED) {
                throw new FluxBankException(ErrorCode.VALIDATION_ERROR, "KYC already approved.");
            }
            if (record.getStatus() == KycStatus.PENDING) {
                throw new FluxBankException(ErrorCode.VALIDATION_ERROR, "KYC review in progress.");
            }
            // REJECTED — allow re-submission by updating existing record
            record.setDocumentType(request.documentType());
            record.setDocumentNumberHash(hashDocumentNumber(request.documentNumber()));
            record.setFirstName(request.firstName());
            record.setLastName(request.lastName());
            record.setDateOfBirth(request.dateOfBirth());
            record.setStatus(KycStatus.PENDING);
            record.setSubmittedAt(Instant.now());
            record.setReviewedAt(null);
            record.setRejectionReason(null);
            KycRecord saved = kycRepository.save(record);
            log.info("KYC re-submitted: kycId={}, userId={}", saved.getId(), userId);
            publishSubmitted(saved);
            return kycMapper.toDto(saved);
        }

        KycRecord newRecord = KycRecord.builder()
                .userId(userId)
                .status(KycStatus.PENDING)
                .documentType(request.documentType())
                .documentNumberHash(hashDocumentNumber(request.documentNumber()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .submittedAt(Instant.now())
                .build();

        KycRecord saved = kycRepository.save(newRecord);
        log.info("KYC submitted: kycId={}, userId={}", saved.getId(), userId);
        publishSubmitted(saved);
        return kycMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public KycDto getKyc(UUID userId) {
        KycRecord record = kycRepository.findByUserId(userId)
                .orElseThrow(() -> new FluxBankException(ErrorCode.NOT_FOUND,
                        "No KYC record found for user: " + userId));
        return kycMapper.toDto(record);
    }

    @Override
    @Transactional
    public KycDto reviewKyc(UUID kycId, ReviewKycRequest request) {
        KycRecord record = kycRepository.findById(kycId)
                .orElseThrow(() -> new FluxBankException(ErrorCode.NOT_FOUND, "KYC record not found: " + kycId));

        if (record.getStatus() != KycStatus.PENDING) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Only PENDING KYC records can be reviewed. Current status: " + record.getStatus());
        }

        KycStatus newStatus = request.approved() ? KycStatus.APPROVED : KycStatus.REJECTED;
        record.setStatus(newStatus);
        record.setReviewedAt(Instant.now());
        if (!request.approved()) {
            record.setRejectionReason(request.rejectionReason());
        }

        KycRecord saved = kycRepository.save(record);
        log.info("KYC reviewed: kycId={}, newStatus={}", kycId, newStatus);

        KycStatusChangedEvent event = new KycStatusChangedEvent(
                saved.getId().toString(),
                saved.getUserId().toString(),
                newStatus.name(),
                saved.getRejectionReason()
        );
        kafkaTemplate.send(KafkaTopics.KYC_EVENTS, saved.getId().toString(), event);

        return kycMapper.toDto(saved);
    }

    private void publishSubmitted(KycRecord record) {
        KycSubmittedEvent event = new KycSubmittedEvent(
                record.getId().toString(),
                record.getUserId().toString(),
                record.getDocumentType().name()
        );
        kafkaTemplate.send(KafkaTopics.KYC_EVENTS, record.getId().toString(), event);
    }

    private String hashDocumentNumber(String documentNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(documentNumber.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new FluxBankException(ErrorCode.INTERNAL_ERROR, "Failed to hash document number");
        }
    }
}
