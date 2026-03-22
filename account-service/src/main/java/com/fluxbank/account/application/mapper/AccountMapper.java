package com.fluxbank.account.application.mapper;

import com.fluxbank.account.application.dto.AccountDto;
import com.fluxbank.account.application.dto.AccountSummaryDto;
import com.fluxbank.account.domain.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "type", expression = "java(account.getType().name())")
    @Mapping(target = "status", expression = "java(account.getStatus().name())")
    @Mapping(target = "balance",
            expression = "java(account.getAvailableBalance().getAmount())")
    @Mapping(target = "reservedBalance",
            expression = "java(account.getReservedBalance().getAmount())")
    @Mapping(target = "currency",
            expression = "java(account.getBalance().getCurrency().name())")
    AccountDto toDto(Account account);

    @Mapping(target = "type", expression = "java(account.getType().name())")
    @Mapping(target = "status", expression = "java(account.getStatus().name())")
    @Mapping(target = "balance",
            expression = "java(account.getAvailableBalance().getAmount())")
    @Mapping(target = "currency",
            expression = "java(account.getBalance().getCurrency().name())")
    AccountSummaryDto toSummaryDto(Account account);

    List<AccountSummaryDto> toSummaryDtoList(List<Account> accounts);
}
