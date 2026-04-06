package ru.practicum.ewm.rating.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.rating.dto.EventIdWithRating;
import ru.practicum.ewm.rating.dto.RatingStatsDto;
import ru.practicum.ewm.rating.model.RatingEvent;

import java.util.List;
import java.util.Optional;

public interface RatingEventRepository extends JpaRepository<RatingEvent, Long> {
    Optional<RatingEvent> findByEventIdAndUserId(Long eventId, Long userId);

    @Query("SELECT new ru.practicum.ewm.rating.dto.RatingStatsDto(" +
            "COUNT(CASE WHEN r.value = 1 THEN 1 END), " +
            "COUNT(CASE WHEN r.value = -1 THEN -1 END), " +
            "COALESCE(SUM(r.value), 0) " +
            ") " +
            "FROM RatingEvent r WHERE r.event.id = :eventId")
    RatingStatsDto getEventRating(@Param("eventId") Long eventId);

    @Query("SELECT new ru.practicum.ewm.rating.dto.EventIdWithRating(" +
            "r.event.id, COALESCE(SUM(r.value), 0)) " +
            "FROM RatingEvent r " +
            "WHERE r.event.id IN :ids " +
            "GROUP BY r.event.id")
    List<EventIdWithRating> findRatingsByEventIds(@Param("ids") List<Long> ids);

    @Query("SELECT new ru.practicum.ewm.rating.dto.EventIdWithRating(" +
            "r.event.id, COALESCE(SUM(r.value), 0)) " +
            "FROM RatingEvent r " +
            "WHERE r.event.id = :eventId " +
            "GROUP BY r.event.id")
    EventIdWithRating findRatingByEventId(@Param("eventId") Long eventId);
}
