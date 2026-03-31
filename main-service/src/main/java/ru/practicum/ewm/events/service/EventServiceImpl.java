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
    public Collection<EventShortDto> getEventsByUserId(Long userId, int from, int size) {
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
    public EventFullDto createEvent(Long userId, NewEventDto request) {
        User initiator = validateUser(userId);
        Category category = validateCategory(request.getCategory());
        validateEventDate(request.getEventDate());

        Event event = EventMapper.mapToEvent(request, initiator, category);
        Event createdEvent = eventRepository.save(event);
        log.debug("Событие {} успешно добавлено.", event.getTitle());

        return EventMapper.mapToEventFullDto(createdEvent);
    }

    @Override
    public EventFullDto getEventDtoById(Long userId, Long eventId) {
        Event event = getEventById(userId, eventId);

        return EventMapper.mapToEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = getEventById(userId, eventId);

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Изменять можно только отмененные события или " +
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
    public Collection<ParticipationRequestDto> getParticipationRequestsByUserIdAndEventId(Long userId,
                                                                                          Long eventId) {
        Event event = getEventById(userId, eventId);

        List<ParticipationRequest> participationRequests =
                participationRequestRepository.findByEventId(eventId);

        return participationRequests.stream()
                .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestStatus(Long userId,
                                                                   Long eventId,
                                                                   EventRequestStatusUpdateRequest request) {
        Event event = getEventById(userId, eventId);
        int currentConfirmed = (event.getConfirmedRequests() == null) ? 0 : event.getConfirmedRequests();
        int limit = event.getParticipantLimit() == null ? 0 : event.getParticipantLimit();

        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("Только инициатор события может изменять статусы заявок");
        }

        if (limit == 0 || !event.getRequestModeration()) {
            throw new BadRequestException("Для события лимит заявок равен 0 или отключена пре-модерация заявок - " +
                    "подтверждение заявок не требуется!");
        }

        if (currentConfirmed >= limit) {
            throw new ConflictException("Уже достигнут лимит по заявкам на данное событие.");
        }

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        List<ParticipationRequest> participationRequests =
                participationRequestRepository.findByIdIn(request.getRequestIds());

        validateRequestsBelongToEvent(participationRequests, eventId);
        validateAllRequestsPending(participationRequests);

        int availableSlots = event.getParticipantLimit() - currentConfirmed;
        int confirmedCount = 0;

        for (ParticipationRequest pr : participationRequests) {
            if (request.getStatus().equals(EventRequestStatus.REJECTED)) {
                pr.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(ParticipationRequestMapper.mapToParticipationRequestDto(pr));
            } else if (request.getStatus().equals(EventRequestStatus.CONFIRMED)) {
                if (availableSlots > 0) {
                    pr.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests.add(ParticipationRequestMapper.mapToParticipationRequestDto(pr));
                    availableSlots--;
                    confirmedCount++;
                    log.debug("Заявка с id = {} подтверждена.", pr.getId());
                } else {
                    pr.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(ParticipationRequestMapper.mapToParticipationRequestDto(pr));
                    log.info("Достигнут лимит заявок = {}, заявка {} отклонена.",
                            event.getParticipantLimit(), pr.getId());
                }
            }
        }

        event.setConfirmedRequests(currentConfirmed + confirmedCount);

        log.debug("Обновлено заявок для события {}: подтверждено - {}, отклонено - {}",
                event.getId(), confirmedRequests.size(), rejectedRequests.size());

        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    @Override
    public Collection<EventFullDto> getAllEventsByParameters(List<Long> users,
                                                             List<String> states,
                                                             List<Long> categories,
                                                             String rangeStart,
                                                             String rangeEnd,
                                                             int from,
                                                             int size) {
        if (users == null || users.isEmpty()) {
            users = null;
        } else {
            validateIdsLists(users, "users");
        }

        if (categories == null || categories.isEmpty()) {
            categories = null;
        } else {
            validateIdsLists(categories, "categories");
        }

        List<EventState> eventStates = null;
        if (states != null && !states.isEmpty()) {
            eventStates = validateStates(states);
        }

        LocalDateTime start = (rangeStart == null)
                ? LocalDateTime.now()
                : LocalDateTime.parse(rangeStart, FORMATTER);

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());

        Page<Event> eventsPage;
        if (rangeEnd != null) {
            LocalDateTime end = LocalDateTime.parse(rangeEnd, FORMATTER);
            eventsPage = eventRepository.findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBetween(
                    users, eventStates, categories, start, end, pageable);
        } else {
            eventsPage = eventRepository.findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateAfter(
                    users, eventStates, categories, start, pageable);
        }

        return eventsPage.stream()
                .map(EventMapper::mapToEventFullDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = validateEvent(eventId);

        if (request.hasStateAction() && request.getStateAction().equals(EventAdminStateAction.PUBLISH_EVENT)) {
            if (event.getEventDate().plusHours(1).equals(LocalDateTime.now()) ||
                    event.getEventDate().plusHours(1).isBefore(LocalDateTime.now())) {
                throw new ValidationException("Дата начала изменяемого события должна быть " +
                        "не ранее чем за час от даты публикации.");
            }

            if (event.getState().equals(EventState.PUBLISHED) || event.getState().equals(EventState.CANCELED)) {
                throw new ConflictException("Событие можно публиковать, " +
                        "только если оно в состоянии ожидания публикации.");
            }
        } else if (request.hasStateAction() && request.getStateAction().equals(EventAdminStateAction.REJECT_EVENT)) {
            if (event.getState().equals(EventState.PUBLISHED)) {
                throw new ConflictException("Событие можно отклонить, только если оно еще не опубликовано.");
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
                                                       List<Long> categories,
                                                       Boolean paid,
                                                       String rangeStart,
                                                       String rangeEnd,
                                                       boolean onlyAvailable,
                                                       String sort,
                                                       int from,
                                                       int size) {
        if (categories == null || categories.isEmpty()) {
            categories = null;
        } else {
            validateIdsLists(categories, "categories");
        }
        validateFromAndSize(from, size);
        LocalDateTime start = (rangeStart == null)
                ? LocalDateTime.now()
                : LocalDateTime.parse(rangeStart, FORMATTER);
        LocalDateTime end = (rangeEnd == null)
                ? null
                : LocalDateTime.parse(rangeEnd, FORMATTER);
        validateStartAndEndDate(start, end);

        int page = from / size;
        Pageable pageable;
        if (sort == null) {
            pageable = PageRequest.of(page, size);
        } else if (sort.equals("EVENT_DATE")) {
            pageable = PageRequest.of(page, size, Sort.by("eventDate").descending());
        } else if (sort.equals("VIEWS")) {
            pageable = PageRequest.of(page, size, Sort.by("views").descending());
        } else {
            throw new BadRequestException("Указан неизвестный тип сортировки.");
        }

        Page<Event> eventsPage;
        if (text != null && !text.isBlank()) {
            if (end != null) {
                eventsPage = eventRepository.findAllPublishedWithText(
                        text, categories, paid, start, end, onlyAvailable, pageable);
            } else {
                eventsPage = eventRepository.findAllPublishedWithTextWithoutEnd(
                        text, categories, paid, start, onlyAvailable, pageable);
            }
        } else {
            if (end != null) {
                eventsPage = eventRepository.findAllPublishedWithoutText(
                        categories, paid, start, end, onlyAvailable, pageable);
            } else {
                eventsPage = eventRepository.findAllPublishedWithoutTextWithoutEnd(
                        categories, paid, start, onlyAvailable, pageable);
            }
        }

        eventsPage.forEach(event -> {
            int currentViews = (event.getViews() == null) ? 0 : event.getViews();
            event.setViews(currentViews + 1);
        });

        return eventsPage.stream()
                .map(EventMapper::mapToEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto getEventByIdPublic(Long id) {
        Event event = validateEvent(id);

        if (event.getState().equals(EventState.PUBLISHED)) {
            int currentViews = (event.getViews() == null) ? 0 : event.getViews();
            event.setViews(currentViews + 1);
            return EventMapper.mapToEventFullDto(eventRepository.findByIdAndState(id, EventState.PUBLISHED));
        } else {
            throw new NotFoundException(String.format("Событие с id = %d не опубликовано!", id));
        }
    }

    @Override
    public Collection<ParticipationRequestDto> getParticipationRequestDtoByUserId(Long userId) {
        validateUser(userId);
        List<ParticipationRequest> participationRequests = participationRequestRepository.findByRequesterId(userId);

        return participationRequests.stream()
                .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto createParticipationRequest(Long userId, Long eventId) {
        User requester = validateUser(userId);
        Event event = validateEvent(eventId);
        if (participationRequestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Нельзя добавить повторный запрос!");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии.");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии.");
        }

        int currentConfirmed =
                participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() != 0 && currentConfirmed >= event.getParticipantLimit()) {
            throw new ConflictException("Уже достигнут лимит по заявкам на данное событие.");
        }

        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status((!event.getRequestModeration() || event.getParticipantLimit() == 0)
                        ? RequestStatus.CONFIRMED
                        : RequestStatus.PENDING)
                .build();

        ParticipationRequest createdParticipationRequest = participationRequestRepository.save(participationRequest);
        log.debug("Запрос пользователя {} на участие в событии {} успешно добавлен.",
                requester.getName(), event.getTitle());

        if (createdParticipationRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
            int currentConfirmedRequests = (event.getConfirmedRequests() == null) ? 0 : event.getConfirmedRequests();
            event.setConfirmedRequests(currentConfirmedRequests + 1);
        }

        return ParticipationRequestMapper.mapToParticipationRequestDto(createdParticipationRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
        validateUser(userId);
        ParticipationRequest participationRequest = validateRequest(requestId);

        if (!participationRequest.getRequester().getId().equals(userId)) {
            throw new BadRequestException("Можно отменить только свою запись на событие!");
        }

        RequestStatus oldStatus = participationRequest.getStatus();
        participationRequest.setStatus(RequestStatus.CANCELED);

        if (oldStatus.equals(RequestStatus.CONFIRMED)) {
            Event event = participationRequest.getEvent();
            int currentConfirmedRequests = (event.getConfirmedRequests() == null) ? 0 : event.getConfirmedRequests();
            event.setConfirmedRequests(currentConfirmedRequests - 1);
        }
        log.debug("Заявка пользователя {} на событие {} отменена",
                participationRequest.getRequester().getName(),
                participationRequest.getEvent().getTitle());

        return ParticipationRequestMapper.mapToParticipationRequestDto(participationRequest);
    }

    private Event getEventById(Long userId, Long eventId) {
        validateUser(userId);
        validateEvent(eventId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId);
        if (event == null) {
            throw new NotFoundException("Не найдено событие с id: " + eventId);
        }

        return event;
    }

    private void validateRequestsBelongToEvent(List<ParticipationRequest> requests, Long eventId) {
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
        List<Long> nonPendingIds = requests.stream()
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
        if (end != null && start.isAfter(end)) {
            throw new BadRequestException("Дата начала не может быть позже даты окончания.");
        }
    }

    private void validateIdsLists(List<Long> ids, String fieldName) {
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

    private User validateUser(Long userId) {
        if (userId == null) {
            throw new BadRequestException("UserId не может быть null.");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id: " + userId));
    }

    private Event validateEvent(Long eventId) {
        if (eventId == null) {
            throw new BadRequestException("EventId не может быть null.");
        }

        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Не найдено событие с id: " + eventId));
    }

    private ParticipationRequest validateRequest(Long requestId) {
        if (requestId == null) {
            throw new BadRequestException("RequestId не может быть null.");
        }

        return participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Не найден запрос с id: " + requestId));
    }

    private Category validateCategory(Long catId) {
        if (catId == null) {
            throw new BadRequestException("CategoryId не может быть null.");
        }

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
            throw new BadRequestException("Field: eventDate. " +
                    "Error: должно содержать дату, которая еще не наступила. " +
                    "Value: " + eventDate.format(FORMATTER));
        }
    }
}
