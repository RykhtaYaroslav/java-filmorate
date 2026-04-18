package ru.yandex.practicum.filmorate.dto.director;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DirectorCreateRequest {
    @NotBlank(message = "Имя режиссера не может быть пустым")
    private String name;
}
