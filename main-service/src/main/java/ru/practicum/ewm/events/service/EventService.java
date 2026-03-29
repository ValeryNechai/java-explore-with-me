package ru.practicum.ewm.events.service;

import ru.practicum.ewm.events.dto.*;

import java.util.Collection;
import java.util.List;

public interface EventService {
    Collection<EventShortDto> getEventsByUserId(Integer userId, int from, int size);

    EventFullDto createEvent(Integer userId, NewEventDto request);

    EventFullDto getEventDtoById(Integer userId, Integer eventId);

    EventFullDto updateEventUser(Integer userId, Integer eventId, UpdateEventUserRequest request);

    Collection<ParticipationRequestDto> getParticipationRequestsByUserIdAndEventId(Integer userId, Integer eventId);

    EventRequestStatusUpdateResult updateEventRequestStatus(Integer userId,
                                                            Integer eventId,
                                                            EventRequestStatusUpdateRequest request);

    Collection<EventFullDto> getAllEventsByParameters(List<Integer> users,
                                                      List<String> states,
                                                      List<Integer> categories,
                                                      String rangeStart,
                                                      String rangeEnd,
                                                      int from,
                                                      int size);

    EventFullDto updateEventAdmin(Integer eventId, UpdateEventAdminRequest request);

    Collection<EventShortDto> getAllEventsPublic(String text,
                                                 List<Integer> categories,
                                                 Boolean paid,
                                                 String rangeStart,
                                                 String rangeEnd,
                                                 boolean onlyAvailable,
                                                 String sort,
                                                 int from,
                                                 int size);

    EventFullDto getEventByIdPublic(Integer id);

    Collection<ParticipationRequestDto> getParticipationRequestDtoByUserId(Integer userId);

    ParticipationRequestDto createParticipationRequest(Integer userId, Integer eventId);

    ParticipationRequestDto cancelParticipationRequest(Integer userId, Integer requestId);
}
