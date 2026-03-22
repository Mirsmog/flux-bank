package com.fluxbank.kyc.domain.repository;

import com.fluxbank.kyc.domain.model.KycRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface KycRepository extends JpaRepository<KycRecord, UUID> {
    Optional<KycRecord> findByUserId(UUID userId);
}
