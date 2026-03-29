package ru.practicum.ewm.category.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewCategoryDto {
    @NotNull(message = "Имя не может быть пустым.")
    @Size(min = 1, max = 50, message = "Имя должно быть от 1 до 50 символов.")
    private String name;
}
