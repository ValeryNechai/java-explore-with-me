package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateCompilationRequest {
    private List<Long> events;

    private Boolean pinned;

    @Size(min = 1, max = 50, message = "Заголовок подборки должен быть от 1 до 50 символов.")
    private String title;

    public boolean hasEvents() {
        return ! (events == null || events.isEmpty());
    }

    public boolean hasPinned() {
        return pinned != null;
    }

    public boolean hasTitle() {
        return ! (title == null || title.isBlank());
    }
}
