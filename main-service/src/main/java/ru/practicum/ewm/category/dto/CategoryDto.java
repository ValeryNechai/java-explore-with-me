package ru.practicum.ewm.category.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDto {
    private Integer id;
    private String name;
}
