package ru.practicum.ewm.events.model;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    private float lat;
    private float lon;
}
