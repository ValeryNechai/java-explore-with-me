package ru.practicum.ewm.exception.controller;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.exception.model.ApiError;
import ru.practicum.ewm.exception.model.ErrorStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(final ConflictException exception) {
        log.warn("ConflictException: {}", exception.getMessage());

        return new ApiError(
                ErrorStatus.CONFLICT,
                "Integrity constraint has been violated.",
                exception.getMessage(),
                LocalDateTime.now().format(FORMATTER)
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintViolation(final ConstraintViolationException exception) {
        log.warn("ConstraintViolationException: {}", exception.getMessage());

        return new ApiError(
                ErrorStatus.CONFLICT,
                "Integrity constraint has been violated.",
                exception.getMessage(),
                LocalDateTime.now().format(FORMATTER)
        );
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleValidation(final ValidationException exception) {
        log.warn("ValidationException: {}", exception.getMessage());

        return new ApiError(
                ErrorStatus.FORBIDDEN,
                "For the requested operation the conditions are not met.",
                exception.getMessage(),
                LocalDateTime.now().format(FORMATTER)
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(final MethodArgumentNotValidException exception) {
        log.warn("MethodArgumentNotValidException: {}", exception.getMessage());

        return new ApiError(
                ErrorStatus.BAD_REQUEST,
                "Incorrectly made request.",
                exception.getMessage(),
                LocalDateTime.now().format(FORMATTER)
        );
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(final BadRequestException exception) {
        log.warn("BadRequestException: {}", exception.getMessage());

        return new ApiError(
                ErrorStatus.BAD_REQUEST,
                "Incorrectly made request.",
                exception.getMessage(),
                LocalDateTime.now().format(FORMATTER)
        );
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException exception) {
        log.warn("NotFoundException: {}", exception.getMessage());

        return new ApiError(
                ErrorStatus.NOT_FOUND,
                "The required object was not found.",
                exception.getMessage(),
                LocalDateTime.now().format(FORMATTER)
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGenericException(final Exception exception) {
        log.error("Unexpected error: ", exception);

        return new ApiError(
                ErrorStatus.INTERNAL_SERVER_ERROR,
                "Internal server error.",
                exception.getMessage(),
                LocalDateTime.now().format(FORMATTER)
        );
    }
}
