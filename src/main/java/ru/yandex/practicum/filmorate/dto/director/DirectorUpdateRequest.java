package ru.yandex.practicum.filmorate.dto.director;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DirectorUpdateRequest {
    @NotNull(message = "Id режиссера не может быть пустым")
    private Long id;
    private String name;
}
