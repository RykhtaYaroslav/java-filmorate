package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.model.enums.MpaRating;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = {"id"})
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200,
            message = "Максимальная длина описания - 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    @PastOrPresent(message = "Дата релиза не может быть в будущем")
    private LocalDate releaseDate;

    @Min(value = 1, message = "Длительность должна быть положительной")
    private Long duration;

    @NotNull(message = "Возрастной рейтинг должен быть указан")
    private MpaRating rating;
}
