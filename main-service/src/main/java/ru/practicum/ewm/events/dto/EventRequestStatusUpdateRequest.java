package ru.practicum.ewm.events.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.ewm.events.model.EventRequestStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    @NotNull(message = "IDs запросов не могут быть null.")
    @NotEmpty(message = "Список IDs запросов не может быть пуст.")
    private List<Long> requestIds;

    @NotNull(message = "Статус не может быть null.")
    private EventRequestStatus status;
}
