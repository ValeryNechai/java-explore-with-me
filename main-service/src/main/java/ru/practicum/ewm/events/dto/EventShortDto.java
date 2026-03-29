package ru.practicum.ewm.events.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.user.dto.UserShortDto;

@Data
@Builder
public class EventShortDto {
    private String annotation;
    private CategoryDto category;
    private Integer confirmedRequests;
    private String eventDate;
    private Integer id;
    private UserShortDto initiator;
    private boolean paid;
    private String title;
    private int views;
}
