package ru.practicum.ewm.event.mapper;

import ru.practicum.ewm.event.dto.ParticipationRequestDto;
import ru.practicum.ewm.event.model.ParticipationRequest;

public final class ParticipationRequestMapper {
    public static ParticipationRequestDto mapToParticipationRequestDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .id(request.getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }
}
