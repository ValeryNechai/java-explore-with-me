package ru.practicum.ewm.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dao.CategoryRepository;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.events.dao.EventRepository;
import ru.practicum.ewm.events.dao.ParticipationRequestRepository;
import ru.practicum.ewm.events.dto.*;
import ru.practicum.ewm.events.mapper.EventMapper;
import ru.practicum.ewm.events.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.events.model.*;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.user.dao.UserRepository;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Collection<EventShortDto> getEventsByUserId(Integer userId, int from, int size) {
        validateUser(userId);
        validateFromAndSize(from, size);

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());
        Page<Event> eventsPage = eventRepository.findByInitiatorId(userId, pageable);

        return eventsPage.stream()
                .map(EventMapper::mapToEventShortDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Integer userId, NewEventDto request) {
        User initiator = validateUser(userId);
        Category category = validateCategory(request.getCategoryId());
        validateEventDate(request.getEventDate());

        Event event = EventMapper.mapToEvent(request, initiator, category);
        Event createdEvent = eventRepository.save(event);
        log.debug("Событие {} успешно добавлено.", event.getTitle());

        return EventMapper.mapToEventFullDto(createdEvent);
    }

    @Override
    public EventFullDto getEventDtoById(Integer userId, Integer eventId) {
        Event event = getEventById(userId, eventId);

        return EventMapper.mapToEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventUser(Integer userId, Integer eventId, UpdateEventUserRequest request) {
        Event event = getEventById(userId, eventId);

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Изменять можно только отмененные события или " +
                    "события в ожидании модерации");
        }
        if (request.hasEventDate()) {
            validateEventDate(request.getEventDate());
        }

        Category category;
        if (request.hasCategoryId()) {
            category = validateCategory(request.getCategoryId());
        } else {
            category = event.getCategory();
        }

        Location location;
        if (request.hasLocation()) {
            location = request.getLocation();
        } else {
            location = event.getLocation();
        }

        Event newEvent = EventMapper.updateEventFields(event, request, category, location);
        Event updatedEvent = eventRepository.save(newEvent);
        log.debug("Событие {} успешно обновлено Пользователем.", updatedEvent.getTitle());

        return EventMapper.mapToEventFullDto(updatedEvent);
    }

    @Override
    public Collection<ParticipationRequestDto> getParticipationRequestsByUserIdAndEventId(Integer userId,
                                                                                          Integer eventId) {
        Event event = getEventById(userId, eventId);

        List<ParticipationRequest> participationRequests =
                participationRequestRepository.findByEventId(eventId);

        return participationRequests.stream()
                .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestStatus(Integer userId,
                                                                   Integer eventId,
                                                                   EventRequestStatusUpdateRequest request) {
        Event event = getEventById(userId, eventId);

        if (event.getParticipantLimit().equals(0) || !event.getRequestModeration()) {
            throw new BadRequestException("Для события лимит заявок равен 0 или отключена пре-модерация заявок - " +
                    "подтверждение заявок не требуется!");
        }

        int currentConfirmed = participationRequestRepository.findCountByEventId(eventId);
        if (currentConfirmed >= event.getParticipantLimit()) {
            throw new ConflictException("Уже достигнут лимит по заявкам на данное событие.");
        }

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        List<ParticipationRequest> participationRequests =
                participationRequestRepository.findByIdIn(request.getRequestIds());

        validateRequestsBelongToEvent(participationRequests, eventId);
        validateAllRequestsPending(participationRequests);

        int availableSlots = event.getParticipantLimit() - currentConfirmed;

        for (ParticipationRequest pr : participationRequests) {

            if (request.getStatus().equals(EventRequestStatus.REJECTED)) {
                pr.setStatus(RequestStatus.REJECTED);
                participationRequestRepository.save(pr);
                rejectedRequests.add(ParticipationRequestMapper.mapToParticipationRequestDto(pr));
            } else if (request.getStatus().equals(EventRequestStatus.CONFIRMED)) {
                if (availableSlots > 0) {
                    pr.setStatus(RequestStatus.CONFIRMED);
                    participationRequestRepository.save(pr);
                    confirmedRequests.add(ParticipationRequestMapper.mapToParticipationRequestDto(pr));
                    availableSlots--;
                    log.debug("Заявка с id = {} подтверждена.", pr.getId());
                } else {
                    pr.setStatus(RequestStatus.REJECTED);
                    participationRequestRepository.save(pr);
                    rejectedRequests.add(ParticipationRequestMapper.mapToParticipationRequestDto(pr));
                    log.info("Достигнут лимит заявок = {}, заявка {} отклонена.",
                            event.getParticipantLimit(), pr.getId());
                }
            }
        }
        int newConfirmedCount = participationRequestRepository.findCountByEventId(eventId);
        event.setConfirmedRequests(newConfirmedCount);
        eventRepository.save(event);

        log.debug("Обновлено заявок для события {}: подтверждено - {}, отклонено - {}",
                event.getId(), confirmedRequests.size(), rejectedRequests.size());

        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    @Override
    public Collection<EventFullDto> getAllEventsByParameters(List<Integer> users,
                                                             List<String> states,
                                                             List<Integer> categories,
                                                             String rangeStart,
                                                             String rangeEnd,
                                                             int from,
                                                             int size) {
        validateIdsLists(users, "users");
        validateIdsLists(categories, "categories");
        validateFromAndSize(from, size);
        List<EventState> eventStates = validateStates(states);
        LocalDateTime start = LocalDateTime.parse(rangeStart, FORMATTER);
        LocalDateTime end = LocalDateTime.parse(rangeEnd, FORMATTER);
        validateStartAndEndDate(start, end);

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());
        Page<Event> eventsPage = eventRepository.findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBetween(
                users, eventStates, categories, start, end, pageable
        );

        return eventsPage.stream()
                .map(EventMapper::mapToEventFullDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Integer eventId, UpdateEventAdminRequest request) {
        Event event = validateEvent(eventId);

        if (request.hasStateAction() && request.getStateAction().equals(EventAdminStateAction.PUBLISH_EVENT)){
            if (event.getEventDate().plusHours(1).equals(LocalDateTime.now()) ||
                    event.getEventDate().plusHours(1).isBefore(LocalDateTime.now())) {
                throw new ValidationException("Дата начала изменяемого события должна быть " +
                        "не ранее чем за час от даты публикации.");
            }

            if (event.getState().equals(EventState.PUBLISHED) || event.getState().equals(EventState.CANCELED)) {
                throw new ValidationException("Событие можно публиковать, " +
                        "только если оно в состоянии ожидания публикации.");
            }
        } else if (request.hasStateAction() && request.getStateAction().equals(EventAdminStateAction.REJECT_EVENT)) {
            if (event.getState().equals(EventState.PUBLISHED)) {
                throw new ValidationException("Событие можно отклонить, только если оно еще не опубликовано.");
            }
        }

        if (request.hasEventDate()) {
            validateEventDate(request.getEventDate());
        }

        Category category;
        if (request.hasCategoryId()) {
            category = validateCategory(request.getCategoryId());
        } else {
            category = event.getCategory();
        }

        Location location;
        if (request.hasLocation()) {
            location = request.getLocation();
        } else {
            location = event.getLocation();
        }

        Event newEvent = EventMapper.updateEventFields(event, request, category, location);
        Event updatedEvent = eventRepository.save(newEvent);
        log.debug("Событие {} успешно обновлено Администратором.", updatedEvent.getTitle());

        return EventMapper.mapToEventFullDto(updatedEvent);
    }

    @Override
    public Collection<EventShortDto> getAllEventsPublic(String text,
                                                       List<Integer> categories,
                                                       Boolean paid,
                                                       String rangeStart,
                                                       String rangeEnd,
                                                       boolean onlyAvailable,
                                                       String sort,
                                                       int from,
                                                       int size) {
        if (text == null) {
            throw new BadRequestException("text не может быть null.");
        } else if (paid == null) {
            throw new BadRequestException("paid не может быть null.");
        }

        validateIdsLists(categories, "categories");
        validateFromAndSize(from, size);
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (rangeStart != null && rangeEnd != null) {
            start = LocalDateTime.parse(rangeStart, FORMATTER);
            end = LocalDateTime.parse(rangeEnd, FORMATTER);
            validateStartAndEndDate(start, end);
        }

        int page = from / size;
        Pageable pageable;
        if (sort.equals("EVENT_DATE")) {
            pageable = PageRequest.of(page, size, Sort.by("eventDate").descending());
        } else if (sort.equals("VIEWS")) {
            pageable = PageRequest.of(page, size, Sort.by("views").descending());
        } else {
            throw new BadRequestException("Указан неизвестный тип сортировки.");
        }

        Page<Event> eventsPage =
                eventRepository.findAllEventsPublic(text, categories, paid, start, end, onlyAvailable, pageable);

        return eventsPage.stream()
                .map(EventMapper::mapToEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto getEventByIdPublic(Integer id) {
        Event event = validateEvent(id);

        if (event.getState().equals(EventState.PUBLISHED)) {
            return EventMapper.mapToEventFullDto(eventRepository.findByIdAndState(id, EventState.PUBLISHED));
        } else {
            throw new BadRequestException(String.format("Событие с id = %d не опубликовано!", id));
        }
    }

    @Override
    public Collection<ParticipationRequestDto> getParticipationRequestDtoByUserId(Integer userId) {
        validateUser(userId);
        List<ParticipationRequest> participationRequests = participationRequestRepository.findByRequesterId(userId);

        return participationRequests.stream()
                .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto createParticipationRequest(Integer userId, Integer eventId) {
        User requester = validateUser(userId);
        Event event = validateEvent(eventId);
        if (participationRequestRepository.existByIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Нельзя добавить повторный запрос!");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии.");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии.");
        }

        int currentConfirmed = participationRequestRepository.findCountByEventId(eventId);
        if (currentConfirmed >= event.getParticipantLimit()) {
            throw new ConflictException("Уже достигнут лимит по заявкам на данное событие.");
        }

        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .build();

        if (event.getRequestModeration()) {
            participationRequest.setStatus(RequestStatus.PENDING);
        } else {
            participationRequest.setStatus(RequestStatus.CONFIRMED);
        }

        ParticipationRequest createdParticipationRequest = participationRequestRepository.save(participationRequest);
        log.debug("Запрос пользователя {} на участие в событии {} успешно добавлен.",
                requester.getName(), event.getTitle());

        return ParticipationRequestMapper.mapToParticipationRequestDto(createdParticipationRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelParticipationRequest(Integer userId, Integer requestId) {
        validateUser(userId);
        ParticipationRequest participationRequest = validateRequest(requestId);

        if (participationRequest.getRequester().getId().equals(userId)) {
            participationRequest.setStatus(RequestStatus.CONFIRMED);
        } else {
            throw new BadRequestException("Можно отменить только свою запись на событие!");
        }

        ParticipationRequest updatedParticipationRequest = participationRequestRepository.save(participationRequest);

        return ParticipationRequestMapper.mapToParticipationRequestDto(updatedParticipationRequest);
    }

    private Event getEventById(Integer userId, Integer eventId) {
        validateUser(userId);
        validateEvent(eventId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId);
        if (event == null) {
            throw new NotFoundException("Не найдено событие с id: " + eventId);
        }

        return event;
    }

    private void validateRequestsBelongToEvent(List<ParticipationRequest> requests, Integer eventId) {
        for (ParticipationRequest request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new BadRequestException(
                        String.format("Заявка с id = %d не принадлежит событию с id = %d",
                                request.getId(), eventId)
                );
            }
        }
    }

    private void validateAllRequestsPending(List<ParticipationRequest> requests) {
        List<Integer> nonPendingIds = requests.stream()
                .filter(r -> r.getStatus() != RequestStatus.PENDING)
                .map(ParticipationRequest::getId)
                .toList();

        if (!nonPendingIds.isEmpty()) {
            throw new ConflictException(
                    String.format("Статус можно изменить только у заявок, находящихся в состоянии ожидания. " +
                            "Некорректные заявки: %s", nonPendingIds)
            );
        }
    }

    private void validateStartAndEndDate(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Дата начала не может быть позже даты окончания.");
        }

        if (start.isBefore(LocalDateTime.now()) || end.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Дата начала и окончания не может быть из прошлого.");
        }
    }

    private void validateIdsLists(List<Integer> ids, String fieldName) {
        if (ids.isEmpty()) {
            throw new BadRequestException("Список id не может быть пустым.");
        }

        if (ids.stream().anyMatch(Objects::isNull)) {
            throw new BadRequestException(
                    String.format("Список '%s' содержит null значения.", fieldName)
            );
            }
    }

    private List<EventState> validateStates(List<String> states) {
        if (states == null) {
            throw new BadRequestException("Значение статуса не может быть null.");
        }

        try {
            return states.stream()
                    .map(state -> EventState.valueOf(state.toUpperCase().trim()))
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private User validateUser(Integer userId) {
        if (userId == null) {
            throw new BadRequestException("UserId не может быть null.");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id: " + userId));
    }

    private Event validateEvent(Integer eventId) {
        if (eventId == null) {
            throw new BadRequestException("EventId не может быть null.");
        }

        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Не найдено событие с id: " + eventId));
    }

    private ParticipationRequest validateRequest(Integer requestId) {
        if (requestId == null) {
            throw new BadRequestException("RequestId не может быть null.");
        }

        return participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Не найден запрос с id: " + requestId));
    }

    private Category validateCategory(Integer catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Не найдена категория с id: " + catId));
    }

    private void validateFromAndSize(int from, int size) {
        if (from < 0) {
            throw new BadRequestException(
                    String.format("BadRequest: from = %d. From не может быть меньше 0.", from)
            );
        }

        if (size <= 0) {
            throw new BadRequestException(
                    String.format("BadRequest: size = %d. Size не может быть меньше 0.", size)
            );
        }
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Field: eventDate. " +
                    "Error: должно содержать дату, которая еще не наступила. " +
                    "Value: " + eventDate);
        }
    }
}
