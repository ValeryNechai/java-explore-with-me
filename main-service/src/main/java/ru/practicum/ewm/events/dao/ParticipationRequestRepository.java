package ru.practicum.ewm.events.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.events.model.ParticipationRequest;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Integer> {

    List<ParticipationRequest> findByEventId(Integer eventId);

    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr " +
            "WHERE pr.event.id = ?1 AND pr.status = 'CONFIRMED'")
    int findCountByEventId(Integer eventId);

    List<ParticipationRequest> findByIdIn(List<Integer> ids);

    List<ParticipationRequest> findByRequesterId(Integer requesterId);

    boolean existByIdAndRequesterId(Integer id, Integer requesterId);
}
