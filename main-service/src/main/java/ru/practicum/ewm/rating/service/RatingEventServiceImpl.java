package ru.practicum.ewm.rating.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.dao.EventRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.rating.dao.RatingEventRepository;
import ru.practicum.ewm.rating.dto.EventWithRating;
import ru.practicum.ewm.rating.dto.RatingStatsDto;
import ru.practicum.ewm.rating.dto.RatingEventDto;
import ru.practicum.ewm.rating.dto.UserWithRating;
import ru.practicum.ewm.rating.mapper.RatingEventMapper;
import ru.practicum.ewm.rating.model.RatingEvent;
import ru.practicum.ewm.user.dao.UserRepository;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RatingEventServiceImpl implements RatingEventService {
    private final RatingEventRepository ratingEventRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RatingEventDto setRating(Long eventId, Long userId, boolean isLike) {
        Event event = validateEvent(eventId);
        User user = validateUser(userId);
        Optional<RatingEvent> existingRatingEvent = ratingEventRepository.findByEventIdAndUserId(eventId, userId);

        short newValue = (short) (isLike ? 1 : -1);

        RatingEvent ratingEvent;
        if (existingRatingEvent.isPresent()) {
            ratingEvent = existingRatingEvent.get();

            if (ratingEvent.getValue() == newValue) {
                throw new BadRequestException(
                        String.format(
                                "Нельзя повторно поставить %s к данному событию!",
                                (newValue == 1) ? "LIKE" : "DISLIKE"
                                )
                );
            } else {
                ratingEvent.setValue(newValue);
                log.info("{} к событию с id = {} успешно обновлен на {}.",
                        (newValue == 1) ? "DISLIKE" : "LIKE",
                        eventId,
                        (newValue == 1) ? "LIKE" : "DISLIKE");
            }
        } else {
            ratingEvent = RatingEvent.builder()
                    .event(event)
                    .user(user)
                    .value(newValue)
                    .created(LocalDateTime.now())
                    .build();

            ratingEventRepository.save(ratingEvent);
            log.info("{} к событию с id = {} успешно сохранен.",
                    (newValue == 1) ? "LIKE" : "DISLIKE",
                    eventId);
        }

        return RatingEventMapper.mapToRatingEventDto(ratingEvent);
    }

    @Override
    @Transactional
    public void deleteRating(Long eventId, Long userId) {
        validateEvent(eventId);
        validateUser(userId);
        Optional<RatingEvent> existingRatingEvent = ratingEventRepository.findByEventIdAndUserId(eventId, userId);

        if (existingRatingEvent.isPresent()) {
            ratingEventRepository.deleteById(existingRatingEvent.get().getId());
        } else {
            throw new BadRequestException("LIKE/DISLIKE ранее не был поставлен!");
        }

    }

    @Override
    public RatingStatsDto getRatingStats(Long eventId) {
        validateEvent(eventId);

        return ratingEventRepository.getEventRating(eventId);
    }

    @Override
    public List<EventWithRating> getEventsRating(int size) {
        Pageable limit = PageRequest.of(0, size);

        return eventRepository.findEventsWithRating(limit);
    }

    @Override
    public List<UserWithRating> getUsersRating(int size) {
        Pageable limit = PageRequest.of(0, size);

        return userRepository.findUsersWithRating(limit);
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
}
