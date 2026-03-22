package com.fluxbank.transaction.application.mapper;

import com.fluxbank.transaction.application.dto.LedgerEntryDto;
import com.fluxbank.transaction.application.dto.TransactionEventDto;
import com.fluxbank.transaction.domain.model.LedgerEntry;
import com.fluxbank.transaction.domain.model.TransactionEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "eventType", expression = "java(event.getEventType().name())")
    @Mapping(target = "status", expression = "java(event.getStatus().name())")
    TransactionEventDto toDto(TransactionEvent event);

    @Mapping(target = "entryType", expression = "java(entry.getEntryType().name())")
    LedgerEntryDto toLedgerDto(LedgerEntry entry);
}
