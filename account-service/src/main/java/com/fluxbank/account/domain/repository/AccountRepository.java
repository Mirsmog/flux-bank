package com.fluxbank.account.domain.repository;

import com.fluxbank.account.domain.model.Account;
import com.fluxbank.account.domain.model.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUserId(UUID userId);

    List<Account> findByUserIdAndStatus(UUID userId, AccountStatus status);

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    /** Combines lookup and ownership check in a single query. */
    Optional<Account> findByIdAndUserId(UUID id, UUID userId);
}
