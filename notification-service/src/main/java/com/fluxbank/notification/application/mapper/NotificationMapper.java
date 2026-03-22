package com.fluxbank.notification.application.mapper;

import com.fluxbank.notification.application.dto.NotificationDto;
import com.fluxbank.notification.domain.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "channel", expression = "java(notification.getChannel().name())")
    @Mapping(target = "status", expression = "java(notification.getStatus().name())")
    NotificationDto toDto(Notification notification);

    List<NotificationDto> toDtoList(List<Notification> notifications);
}
