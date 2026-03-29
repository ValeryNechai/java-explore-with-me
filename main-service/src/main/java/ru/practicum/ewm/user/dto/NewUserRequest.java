package ru.practicum.ewm.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewUserRequest {
    @NotNull(message = "Имя не может быть пустым.")
    @Size(min = 2, max = 250, message = "Имя должно быть от 2 до 250 символов.")
    public String name;

    @NotNull(message = "Email не может быть пустым.")
    @Email(message = "Некорректный email!")
    @Size(min = 6, max = 254, message = "Email должен быть от 6 до 254 символов.")
    public String email;
}
