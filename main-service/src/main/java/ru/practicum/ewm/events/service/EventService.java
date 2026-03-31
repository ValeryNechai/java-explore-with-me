package ru.practicum.ewm.events.service;

import ru.practicum.ewm.events.dto.*;

import java.util.Collection;
import java.util.List;

public interface EventService {
    Collection<EventShortDto> getEventsByUserId(Long userId, int from, int size);

    EventFullDto createEvent(Long userId, NewEventDto request);

    EventFullDto getEventDtoById(Long userId, Long eventId);

    EventFullDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest request);

    Collection<ParticipationRequestDto> getParticipationRequestsByUserIdAndEventId(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequestStatus(Long userId,
                                                            Long eventId,
                                                            EventRequestStatusUpdateRequest request);

    Collection<EventFullDto> getAllEventsByParameters(List<Long> users,
                                                      List<String> states,
                                                      List<Long> categories,
                                                      String rangeStart,
                                                      String rangeEnd,
                                                      int from,
                                                      int size);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request);

    Collection<EventShortDto> getAllEventsPublic(String text,
                                                 List<Long> categories,
                                                 Boolean paid,
                                                 String rangeStart,
                                                 String rangeEnd,
                                                 boolean onlyAvailable,
                                                 String sort,
                                                 int from,
                                                 int size);

    EventFullDto getEventByIdPublic(Long id);

    Collection<ParticipationRequestDto> getParticipationRequestDtoByUserId(Long userId);

    ParticipationRequestDto createParticipationRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId);
}
