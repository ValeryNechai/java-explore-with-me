package ru.practicum.ewm.rating.dto;

public record EventWithRating(
        Long eventId,
        String title,
        String categoryName,
        String initiatorName,
        long rating
) {}
