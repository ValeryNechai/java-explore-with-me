package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class NewCompilationDto {
    @NotBlank(message = "Список событий не может быть пустым!")
    private List<Integer> events;

    @NotNull(message = "Должна быть указана информация о закреплении подборки на главной странице сайта!")
    private Boolean pinned;

    @NotBlank(message = "Заголовок подборки не может быть пустым!")
    @Size(min = 1, max = 50, message = "Заголовок подборки должен быть от 1 до 50 символов.")
    private String title;
}
