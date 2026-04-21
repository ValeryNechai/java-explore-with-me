package ru.practicum.ewm.event.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.rating.dto.EventWithRating;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    @EntityGraph(attributePaths = {"initiator", "category"})
    Page<Event> findByInitiatorId(Long userId, Pageable pageable);

    Event findByIdAndInitiatorId(Long id, Long initiatorId);

    @Query("SELECT e FROM Event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (e.eventDate >= :startDate) " +
            "AND (e.eventDate <= :endDate)")
    Page<Event> findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBetween(
            @Param("users") List<Long> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (e.eventDate >= :startDate)")
    Page<Event> findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateAfter(
            @Param("users") List<Long> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("startDate") LocalDateTime startDate,
            Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (:text IS NULL OR lower(e.annotation) LIKE lower(concat('%', :text, '%')) OR " +
            "     lower(e.description) LIKE lower(concat('%', :text, '%'))) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (e.eventDate >= :rangeStart) " +
            "AND (e.eventDate <= :rangeEnd) " +
            "AND (:onlyAvailable = false OR e.confirmedRequests < e.participantLimit)")
    List<Event> findAllPublishedWithText(@Param("text") String text,
                                         @Param("categories") List<Long> categories,
                                         @Param("paid") Boolean paid,
                                         @Param("rangeStart") LocalDateTime rangeStart,
                                         @Param("rangeEnd") LocalDateTime rangeEnd,
                                         @Param("onlyAvailable") boolean onlyAvailable);

    @Query("SELECT e FROM Event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (e.eventDate >= :rangeStart) " +
            "AND (e.eventDate <= :rangeEnd) " +
            "AND (:onlyAvailable = false OR e.confirmedRequests < e.participantLimit)")
    List<Event> findAllPublishedWithoutText(@Param("categories") List<Long> categories,
                                            @Param("paid") Boolean paid,
                                            @Param("rangeStart") LocalDateTime rangeStart,
                                            @Param("rangeEnd") LocalDateTime rangeEnd,
                                            @Param("onlyAvailable") boolean onlyAvailable);

    @Query("SELECT e FROM Event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (:text IS NULL OR lower(e.annotation) LIKE lower(concat('%', :text, '%')) OR " +
            "     lower(e.description) LIKE lower(concat('%', :text, '%'))) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (e.eventDate >= :rangeStart) " +
            "AND (:onlyAvailable = false OR e.confirmedRequests < e.participantLimit)")
    List<Event> findAllPublishedWithTextWithoutEnd(@Param("text") String text,
                                                   @Param("categories") List<Long> categories,
                                                   @Param("paid") Boolean paid,
                                                   @Param("rangeStart") LocalDateTime rangeStart,
                                                   @Param("onlyAvailable") boolean onlyAvailable);

    @Query("SELECT e FROM Event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (e.eventDate >= :rangeStart) " +
            "AND (:onlyAvailable = false OR e.confirmedRequests < e.participantLimit)")
    List<Event> findAllPublishedWithoutTextWithoutEnd(@Param("categories") List<Long> categories,
                                                      @Param("paid") Boolean paid,
                                                      @Param("rangeStart") LocalDateTime rangeStart,
                                                      @Param("onlyAvailable") boolean onlyAvailable);

    @EntityGraph(attributePaths = {"initiator", "category"})
    Event findByIdAndState(Long id, EventState state);

    @EntityGraph(attributePaths = {"initiator", "category"})
    List<Event> findByIdIn(List<Long> ids);

    @Query("SELECT new ru.practicum.ewm.rating.dto.EventWithRating(" +
            "e.id, e.title, e.category.name, e.initiator.name, " +
            "COALESCE(SUM(r.value), 0)) " +
            "FROM Event e " +
            "LEFT JOIN RatingEvent r ON e.id = r.event.id " +
            "GROUP BY e.id, e.title, e.category.name, e.initiator.name " +
            "ORDER BY SUM(r.value) DESC NULLS LAST ")
    List<EventWithRating> findEventsWithRating(Pageable pageable);
}
