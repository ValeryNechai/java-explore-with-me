package ru.practicum.ewm.stats.exception.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ApiError {
    private ErrorStatus status;
    private String reason;
    private String message;
    private String timestamp;
}