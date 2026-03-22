package com.fluxbank.kyc.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "kyc_records")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class KycRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "kyc_status")
    @Builder.Default
    private KycStatus status = KycStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, columnDefinition = "document_type")
    private DocumentType documentType;

    @Column(name = "document_number_hash", nullable = false, length = 64)
    private String documentNumberHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    // updatable = true (default) — re-submissions after rejection must refresh this timestamp
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Override
    public String toString() {
        return "KycRecord{id=" + id + ", userId=" + userId + ", status=" + status + "}";
    }
}
