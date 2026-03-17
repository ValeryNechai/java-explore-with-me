package ru.practicum.ewm.stats.mapper;

import ru.practicum.ewm.dto.StatsDto;
import ru.practicum.ewm.stats.model.EndpointHit;

public class StatsMapper {

    public static StatsDto mapToStatsDto(EndpointHit hit) {
        return StatsDto.builder()
                .app(hit.getApp())
                .uri(hit.getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp())
                .build();
    }
}
