package ru.practicum.ewm.rating.service;

import ru.practicum.ewm.rating.dto.EventWithRating;
import ru.practicum.ewm.rating.dto.RatingStatsDto;
import ru.practicum.ewm.rating.dto.RatingEventDto;
import ru.practicum.ewm.rating.dto.UserWithRating;

import java.util.List;

public interface RatingEventService {
    RatingEventDto setRating(Long eventId, Long userId, boolean isLike);

    void deleteRating(Long eventId, Long userId);

    RatingStatsDto getRatingStats(Long eventId);

    List<EventWithRating> getEventsRating(int size);

    List<UserWithRating> getUsersRating(int size);
}
