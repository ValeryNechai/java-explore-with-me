package ru.practicum.ewm.exception.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ApiError {
    private ErrorStatus status;
    private String reason;
    private String message;
    private String timestamp;
}
