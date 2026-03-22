package com.fluxbank.kyc.application.mapper;

import com.fluxbank.kyc.application.dto.KycDto;
import com.fluxbank.kyc.domain.model.KycRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KycMapper {

    @Mapping(target = "status", expression = "java(record.getStatus().name())")
    @Mapping(target = "documentType", expression = "java(record.getDocumentType().name())")
    KycDto toDto(KycRecord record);
}
