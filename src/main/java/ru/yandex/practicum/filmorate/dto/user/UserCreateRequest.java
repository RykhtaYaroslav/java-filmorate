package ru.yandex.practicum.filmorate.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserCreateRequest {
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email",
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @NotNull(message = "Укажите дату рождения")
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}