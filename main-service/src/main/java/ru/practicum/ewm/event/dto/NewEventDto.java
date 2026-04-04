package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.ewm.event.model.Location;

import java.time.LocalDateTime;

@Data
public class NewEventDto {
    @NotBlank(message = "Краткое описание события не может быть пустым.")
    @Size(min = 20, max = 2000, message = "Краткое описание события должно быть от 20 до 2000 символов.")
    private String annotation;

    @NotNull(message = "Id категории события должно быть заполнено.")
    @PositiveOrZero
    private Long category;

    @NotBlank(message = "Полное описание события не может быть пустым.")
    @Size(min = 20, max = 7000, message = "Полное описание события должно быть от 20 до 7000 символов.")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Место проведения события не может быть пустым.")
    private Location location;

    private Boolean paid = false;

    @PositiveOrZero
    private Integer participantLimit = 0;

    private Boolean requestModeration = true;

    @NotNull(message = "Заголовок события не может быть пустым.")
    @Size(min = 3, max = 120, message = "Заголовок события должен быть от 3 до 120 символов.")
    private String title;

    private LocalDateTime createdOn = LocalDateTime.now();

    public Boolean getPaid() {
        return paid != null ? paid : false;
    }

    public Boolean getRequestModeration() {
        return requestModeration != null ? requestModeration : true;
    }
}
