package ru.practicum.ewm.stats.service;

import ru.practicum.ewm.dto.NewStatsRequest;
import ru.practicum.ewm.dto.StatsDto;
import ru.practicum.ewm.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    StatsDto saveStats(NewStatsRequest request);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
