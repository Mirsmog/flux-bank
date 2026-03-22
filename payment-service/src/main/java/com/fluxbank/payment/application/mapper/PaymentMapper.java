package com.fluxbank.payment.application.mapper;

import com.fluxbank.payment.application.dto.PaymentDto;
import com.fluxbank.payment.application.dto.PaymentSummaryDto;
import com.fluxbank.payment.domain.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "type", expression = "java(payment.getType().name())")
    @Mapping(target = "status", expression = "java(payment.getStatus().name())")
    PaymentDto toDto(Payment payment);

    @Mapping(target = "type", expression = "java(payment.getType().name())")
    @Mapping(target = "status", expression = "java(payment.getStatus().name())")
    PaymentSummaryDto toSummaryDto(Payment payment);

    List<PaymentSummaryDto> toSummaryDtoList(List<Payment> payments);
}
