package ru.practicum.ewm.stats.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.dto.ViewStats;
import ru.practicum.ewm.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Integer> {

    @Query("select new ru.practicum.ewm.dto.ViewStats(eh.app, eh.uri, COUNT(eh.ip)) " +
            "from EndpointHit as eh " +
            "where eh.timestamp BETWEEN ?1 and ?2 " +
            "AND (?3 IS NULL OR eh.uri IN ?3) " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY COUNT(eh.ip) DESC")
    List<ViewStats> findStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris
    );

    @Query("select new ru.practicum.ewm.dto.ViewStats(eh.app, eh.uri, COUNT(distinct eh.ip)) " +
            "from EndpointHit as eh " +
            "where eh.timestamp BETWEEN ?1 and ?2 " +
            "AND (?3 IS NULL OR eh.uri IN ?3) " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY COUNT(DISTINCT eh.ip) DESC")
    List<ViewStats> findUniqueStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris
    );
}
