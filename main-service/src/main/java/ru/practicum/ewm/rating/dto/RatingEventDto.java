package ru.practicum.ewm.rating.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RatingEventDto {
    private Long eventId;
    private Long userId;
    private Boolean isLike;
    private String created;
}
