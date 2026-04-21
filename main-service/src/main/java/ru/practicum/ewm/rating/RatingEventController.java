package ru.practicum.ewm.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.rating.dto.EventWithRating;
import ru.practicum.ewm.rating.dto.RatingStatsDto;
import ru.practicum.ewm.rating.dto.RatingEventDto;
import ru.practicum.ewm.rating.dto.UserWithRating;
import ru.practicum.ewm.rating.service.RatingEventService;

import java.util.Collection;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class RatingEventController {
    private final RatingEventService ratingEventService;

    @PutMapping("/event/{eventId}/rating/{userId}")
    public RatingEventDto setRating(@PathVariable Long eventId,
                                    @PathVariable Long userId,
                                    @RequestParam boolean isLike) {
        return ratingEventService.setRating(eventId, userId, isLike);
    }

    @DeleteMapping("/event/{eventId}/rating/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRating(@PathVariable Long eventId,
                             @PathVariable Long userId) {
        ratingEventService.deleteRating(eventId, userId);
    }

    @GetMapping("/rating/event/{eventId}")
    public RatingStatsDto getRatingStats(@PathVariable Long eventId) {
        return ratingEventService.getRatingStats(eventId);
    }

    @GetMapping("/rating/events")
    public Collection<EventWithRating> getEventsRating(@RequestParam(defaultValue = "10") int size) {
        return ratingEventService.getEventsRating(size);
    }

    @GetMapping("/rating/users")
    public Collection<UserWithRating> getUsersRating(@RequestParam(defaultValue = "10") int size) {
        return ratingEventService.getUsersRating(size);
    }
}
