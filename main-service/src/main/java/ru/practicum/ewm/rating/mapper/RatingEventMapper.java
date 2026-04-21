package ru.practicum.ewm.rating.mapper;

import ru.practicum.ewm.rating.dto.RatingEventDto;
import ru.practicum.ewm.rating.model.RatingEvent;

import java.time.format.DateTimeFormatter;

public final class RatingEventMapper {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static RatingEventDto mapToRatingEventDto(RatingEvent ratingEvent) {
        return RatingEventDto.builder()
                .eventId(ratingEvent.getEvent().getId())
                .userId(ratingEvent.getUser().getId())
                .isLike(ratingEvent.getValue() == 1)
                .created(ratingEvent.getCreated().format(FORMATTER))
                .build();
    }
}
