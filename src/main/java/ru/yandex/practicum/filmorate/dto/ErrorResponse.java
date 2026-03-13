package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

@Data
public class ErrorResponse {
    private final String name;
    private final String description;
}
