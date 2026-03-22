package com.fluxbank.card.domain.repository;

import com.fluxbank.card.domain.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    List<Card> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Card> findByAccountIdAndUserIdOrderByCreatedAtDesc(UUID accountId, UUID userId);
    Optional<Card> findByIdAndUserId(UUID id, UUID userId);
}
