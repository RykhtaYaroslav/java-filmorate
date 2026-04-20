package ru.yandex.practicum.filmorate.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewCreateRequest {

    @NotBlank(message = "содержимое не может быть пустым")
    private String content;

    @NotNull(message = "реакция не может быть null")
    private Boolean isPositive;

    @NotNull(message = "userId не может быть null")
    private Long userId;

    @NotNull(message = "filmId не может быть null")
    private Long filmId;
}
