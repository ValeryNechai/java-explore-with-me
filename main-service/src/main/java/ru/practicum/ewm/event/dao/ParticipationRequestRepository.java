package ru.practicum.ewm.event.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.event.model.ParticipationRequest;
import ru.practicum.ewm.event.model.RequestStatus;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findByEventId(Long eventId);

    int countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findByIdIn(List<Long> ids);

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);
}
