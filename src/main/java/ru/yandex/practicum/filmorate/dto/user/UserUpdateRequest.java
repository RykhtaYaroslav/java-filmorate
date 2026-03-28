package ru.yandex.practicum.filmorate.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserUpdateRequest {
    @NotNull
    @Positive
    private long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}