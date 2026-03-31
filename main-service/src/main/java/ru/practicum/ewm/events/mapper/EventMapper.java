package ru.practicum.ewm.events.mapper;

import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.events.dto.*;
import ru.practicum.ewm.events.model.*;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;

import java.time.format.DateTimeFormatter;

public final class EventMapper {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EventShortDto mapToEventShortDto(Event event) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.mapToCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate().format(FORMATTER))
                .id(event.getId())
                .initiator(UserMapper.mapToUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews() != null ? event.getViews() : null)
                .build();
    }

    public static Event mapToEvent(NewEventDto newEventDto, User initiator, Category category) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .createdOn(newEventDto.getCreatedOn())
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .initiator(initiator)
                .location(newEventDto.getLocation())
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .state(EventState.PENDING)
                .title(newEventDto.getTitle())
                .build();
    }

    public static EventFullDto mapToEventFullDto(Event event) {
        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.mapToCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn().format(FORMATTER))
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(FORMATTER))
                .id(event.getId())
                .initiator(UserMapper.mapToUserShortDto(event.getInitiator()))
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn() != null ? event.getPublishedOn().format(FORMATTER) : null)
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews() != null ? event.getViews() : null)
                .build();
    }

    public static Event updateEventFields(Event event,
                                          UpdateEventUserRequest request,
                                          Category category,
                                          Location location) {
        if (request.hasAnnotation()) {
            event.setAnnotation(request.getAnnotation());
        }

        if (category != null) {
            event.setCategory(category);
        }

        if (request.hasDescription()) {
            event.setDescription(request.getDescription());
        }

        if (request.hasEventDate()) {
            event.setEventDate(request.getEventDate());
        }

        if (location != null) {
            event.setLocation(location);
        }

        if (request.hasPaid()) {
            event.setPaid(request.getPaid());
        }

        if (request.hasParticipantLimit()) {
            event.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.hasRequestModeration()) {
            event.setRequestModeration(request.getRequestModeration());
        }

        if (request.hasStateAction()) {
            if (request.getStateAction().equals(EventUserStateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else {
                event.setState(EventState.CANCELED);
            }
        }

        if (request.hasTitle()) {
            event.setTitle(request.getTitle());
        }

        return event;
    }

    public static Event updateEventFields(Event event,
                                          UpdateEventAdminRequest request,
                                          Category category,
                                          Location location) {
        if (request.hasAnnotation()) {
            event.setAnnotation(request.getAnnotation());
        }

        if (category != null) {
            event.setCategory(category);
        }

        if (request.hasDescription()) {
            event.setDescription(request.getDescription());
        }

        if (request.hasEventDate()) {
            event.setEventDate(request.getEventDate());
        }

        if (location != null) {
            event.setLocation(location);
        }

        if (request.hasPaid()) {
            event.setPaid(request.getPaid());
        }

        if (request.hasParticipantLimit()) {
            event.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.hasRequestModeration()) {
            event.setRequestModeration(request.getRequestModeration());
        }

        if (request.hasStateAction()) {
            if (request.getStateAction().equals(EventAdminStateAction.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
            } else {
                event.setState(EventState.CANCELED);
            }
        }

        if (request.hasTitle()) {
            event.setTitle(request.getTitle());
        }

        return event;
    }
}
