package ru.practicum.ewm.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.events.model.RequestStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class ParticipationRequestDto {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    private Integer eventId;
    private Integer id;
    private Integer requesterId;
    private RequestStatus status;
}
