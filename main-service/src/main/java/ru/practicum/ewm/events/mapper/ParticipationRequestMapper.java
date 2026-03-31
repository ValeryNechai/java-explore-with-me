package ru.practicum.ewm.events.mapper;

import ru.practicum.ewm.events.dto.ParticipationRequestDto;
import ru.practicum.ewm.events.model.ParticipationRequest;

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
