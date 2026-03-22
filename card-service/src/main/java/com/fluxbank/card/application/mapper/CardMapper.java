package com.fluxbank.card.application.mapper;

import com.fluxbank.card.application.dto.CardDto;
import com.fluxbank.card.application.dto.CardSummaryDto;
import com.fluxbank.card.domain.model.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "type", expression = "java(card.getType().name())")
    @Mapping(target = "status", expression = "java(card.getStatus().name())")
    CardDto toDto(Card card);

    @Mapping(target = "type", expression = "java(card.getType().name())")
    @Mapping(target = "status", expression = "java(card.getStatus().name())")
    CardSummaryDto toSummaryDto(Card card);

    List<CardSummaryDto> toSummaryDtoList(List<Card> cards);
}
