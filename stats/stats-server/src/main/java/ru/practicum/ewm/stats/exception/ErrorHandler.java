package ru.practicum.ewm.stats.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.stats.exception.model.ApiError;
import ru.practicum.ewm.stats.exception.model.ErrorStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(final ValidationException exception) {
        log.warn("ValidationException: {}", exception.getMessage());

        return new ApiError(
                ErrorStatus.BAD_REQUEST,
                "For the requested operation the conditions are not met.",
                exception.getMessage(),
                LocalDateTime.now().format(FORMATTER)
        );
    }
}
