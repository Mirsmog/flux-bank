package com.fluxbank.transaction.application.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class TransactionHistoryResponse {
    UUID accountId;
    List<TransactionEventDto> transactions;
    int page;
    int size;
    long totalElements;
    int totalPages;
    boolean hasNext;
}
