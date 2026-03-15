package ru.practicum.ewm.stats;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.NewStatsRequest;
import ru.practicum.ewm.dto.StatsDto;
import ru.practicum.ewm.dto.ViewStats;
import ru.practicum.ewm.stats.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
public class StatsController {
    private final StatsService statsService;
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @PostMapping("/hit")
    @ResponseStatus( code = HttpStatus.CREATED)
    public StatsDto saveStats(@Valid @RequestBody NewStatsRequest request) {
        return statsService.saveStats(request);
    }

    @GetMapping("/stats")
    @ResponseStatus( code = HttpStatus.OK)
    public List<ViewStats> getStats(
            @RequestParam @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {
        return statsService.getStats(start, end, uris, unique);
    }
}
