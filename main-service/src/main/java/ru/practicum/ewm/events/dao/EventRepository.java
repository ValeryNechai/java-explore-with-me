package ru.practicum.ewm.events.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {

    Page<Event> findByInitiatorId(Integer userId, Pageable pageable);

    Event findByIdAndInitiatorId(Integer id, Integer initiatorId);

    Page<Event> findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBetween(List<Integer> userIds,
                                                                                List<EventState> states,
                                                                                List<Integer> categoryIds,
                                                                                LocalDateTime startDate,
                                                                                LocalDateTime endDate,
                                                                                Pageable pageable);

    @Query("SELECT DISTINCT e FROM Event e " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (lower(e.annotation) LIKE lower(concat('%', ?1, '%')) OR " +
            "     lower(e.description) LIKE lower(concat('%', ?1, '%'))) " +
            "AND e.category.id IN ?2 " +
            "AND e.paid = ?3 " +
            "AND (e.eventDate >= CASE WHEN ?4 IS NULL then current_timestamp " +
            "                         ELSE ?4 END) " +
            "AND (?5 IS NULL OR e.eventDate <= ?5) " +
            "AND (?6 = false OR e.confirmedRequests < e.participantLimit)")
    Page<Event> findAllEventsPublic(String text,
                                    List<Integer> categories,
                                    Boolean paid,
                                    LocalDateTime rangeStart,
                                    LocalDateTime rangeEnd,
                                    boolean onlyAvailable,
                                    Pageable pageable);

    Event findByIdAndState(Integer id, EventState state);

    List<Event> findByIdIn(List<Integer> ids);
}
