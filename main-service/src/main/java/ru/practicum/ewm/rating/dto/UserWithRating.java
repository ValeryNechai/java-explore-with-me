package ru.practicum.ewm.rating.dto;

public record UserWithRating(Long userId, String userName, long rating) {
}
