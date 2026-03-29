package ru.practicum.ewm.events;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.events.dto.*;
import ru.practicum.ewm.events.service.EventService;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class EventController {
    private final EventService eventService;

    @GetMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    public Collection<EventShortDto> getEventsByUserId(@PathVariable Integer userId,
                                                       @RequestParam(defaultValue = "0") int from,
                                                       @RequestParam(defaultValue = "10") int size) {
        return eventService.getEventsByUserId(userId, from, size);
    }

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Integer userId,
                                    @RequestBody NewEventDto request) {
        return eventService.createEvent(userId, request);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventById(@PathVariable Integer userId,
                                     @PathVariable Integer eventId) {
        return eventService.getEventDtoById(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventUser(@PathVariable Integer userId,
                                        @PathVariable Integer eventId,
                                        @RequestBody @Valid UpdateEventUserRequest request) {
        return eventService.updateEventUser(userId, eventId, request);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public Collection<ParticipationRequestDto> getParticipationRequestsByUserIdAndEventId(
            @PathVariable Integer userId,
            @PathVariable Integer eventId) {
        return eventService.getParticipationRequestsByUserIdAndEventId(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateEventRequestStatus(
            @PathVariable Integer userId,
            @PathVariable Integer eventId,
            @RequestBody @Valid EventRequestStatusUpdateRequest request) {
        return eventService.updateEventRequestStatus(userId, eventId, request);
    }

    @GetMapping("/admin/events")
    @ResponseStatus(HttpStatus.OK)
    public Collection<EventFullDto> getAllEventsAdmin(@RequestParam List<Integer> users,
                                                      @RequestParam List<String> states,
                                                      @RequestParam List<Integer> categories,
                                                      @RequestParam String rangeStart,
                                                      @RequestParam String rangeEnd,
                                                      @RequestParam(defaultValue = "0") int from,
                                                      @RequestParam(defaultValue = "10") int size) {
        return eventService.getAllEventsByParameters(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/admin/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventAdmin(@PathVariable Integer eventId,
                                         @RequestBody @Valid UpdateEventAdminRequest request) {
        return eventService.updateEventAdmin(eventId, request);
    }

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public Collection<EventShortDto> getAllEventsPublic(@RequestParam String text,
                                                        @RequestParam List<Integer> categories,
                                                        @RequestParam Boolean paid,
                                                        @RequestParam String rangeStart,
                                                        @RequestParam String rangeEnd,
                                                        @RequestParam(defaultValue = "false") boolean onlyAvailable,
                                                        @RequestParam String sort,
                                                        @RequestParam(defaultValue = "0") int from,
                                                        @RequestParam(defaultValue = "10") int size) {
        return eventService.getAllEventsPublic(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size
        );
    }

    @GetMapping("/events/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventByIdPublic(@PathVariable Integer id) {
        return eventService.getEventByIdPublic(id);
    }

    @GetMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public Collection<ParticipationRequestDto> getParticipationRequestDtoByUserId(@PathVariable Integer userId) {
        return eventService.getParticipationRequestDtoByUserId(userId);
    }

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createParticipationRequest(@PathVariable Integer userId,
                                                              @RequestParam Integer eventId) {
        return eventService.createParticipationRequest(userId, eventId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelParticipationRequest(@PathVariable Integer userId,
                                                              @PathVariable Integer requestId) {
        return eventService.cancelParticipationRequest(userId, requestId);
    }
}
