package ru.practicum.ewm.events.mapper;

import ru.practicum.ewm.events.dto.ParticipationRequestDto;
import ru.practicum.ewm.events.model.ParticipationRequest;

public final class ParticipationRequestMapper {
    public static ParticipationRequestDto mapToParticipationRequestDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .created(request.getCreated())
                .eventId(request.getEvent().getId())
                .id(request.getId())
                .requesterId(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }
}
