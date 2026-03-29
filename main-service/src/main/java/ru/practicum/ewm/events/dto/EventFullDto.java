package ru.practicum.ewm.events.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.events.model.EventState;
import ru.practicum.ewm.events.model.Location;
import ru.practicum.ewm.user.dto.UserShortDto;

@Data
@Builder
public class EventFullDto {
    private String annotation;
    private CategoryDto category;
    private Integer confirmedRequests;
    private String createdOn;
    private String description;
    private String eventDate;
    private Integer id;
    private UserShortDto initiator;
    private Location location;
    private boolean paid;
    private int participantLimit;
    private String publishedOn;
    private boolean requestModeration;
    private EventState state;
    private String title;
    private int views;
}
