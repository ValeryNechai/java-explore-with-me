package ru.practicum.ewm.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.NewStatsRequest;
import ru.practicum.ewm.dto.StatsDto;
import ru.practicum.ewm.dto.ViewStats;
import ru.practicum.ewm.stats.dao.StatsRepository;
import ru.practicum.ewm.stats.exception.ValidationException;
import ru.practicum.ewm.stats.mapper.StatsMapper;
import ru.practicum.ewm.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public StatsDto saveStats(NewStatsRequest request) {
        EndpointHit hit = EndpointHit.builder()
                .app(request.getApp())
                .uri(request.getUri())
                .ip(request.getIp())
                .timestamp(request.getTimestamp())
                .build();

        EndpointHit createdStats = statsRepository.save(hit);
        log.debug("Информация о том, что на uri = {} был отправлен запрос пользователем," +
                " успешно сохранена.", request.getUri());

        return StatsMapper.mapToStatsDto(createdStats);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        validateStats(start, end, uris);
        if (unique) {
            return statsRepository.findUniqueStats(start, end, uris);
        } else {
            return statsRepository.findStats(start, end, uris);
        }
    }

    private void validateStats(LocalDateTime start, LocalDateTime end, List<String> uris) {
        if (end.isBefore(start)) {
            throw new ValidationException("Дата и время окончания диапазона должна быть позже даты начала.");
        }

        if (start.equals(end)) {
            throw new ValidationException("Дата и время начала и окончания диапазона не могут совпадать.");
        }
    }
}
