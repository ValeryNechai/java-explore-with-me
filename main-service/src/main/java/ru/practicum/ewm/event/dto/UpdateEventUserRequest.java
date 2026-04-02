package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.ewm.event.model.EventUserStateAction;
import ru.practicum.ewm.event.model.Location;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest {
    @Size(min = 20, max = 2000, message = "Краткое описание события должно быть от 20 до 2000 символов.")
    private String annotation;

    @PositiveOrZero
    private Long categoryId;

    @Size(min = 20, max = 7000, message = "Полное описание события должно быть от 20 до 7000 символов.")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Future(message = "Дата события должна быть в будущем")
    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;

    private Boolean requestModeration;

    @JsonProperty("stateAction")
    private EventUserStateAction stateAction;

    @Size(min = 3, max = 120, message = "Заголовок события должен быть от 3 до 120 символов.")
    private String title;

    public boolean hasAnnotation() {
        return ! (annotation == null || annotation.isBlank());
    }

    public boolean hasCategoryId() {
        return categoryId != null;
    }

    public boolean hasDescription() {
        return description != null && !description.isBlank();
    }

    public boolean hasEventDate() {
        return eventDate != null;
    }

    public boolean hasLocation() {
        return location != null;
    }

    public boolean hasPaid() {
        return paid != null;
    }

    public boolean hasParticipantLimit() {
        return participantLimit != null;
    }

    public boolean hasRequestModeration() {
        return requestModeration != null;
    }

    public boolean hasStateAction() {
        return stateAction != null;
    }

    public boolean hasTitle() {
        return title != null && !title.isBlank();
    }
}
