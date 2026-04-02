package ru.yandex.practicum.filmorate.dto.film;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.MinimumDate;

import java.time.LocalDate;
import java.util.List;

@Data
public class FilmUpdateRequest {
    @NotNull
    @Positive
    private Long id;

    private String name;

    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private String description;

    @PastOrPresent(message = "Дата релиза не может быть в будущем")
    @MinimumDate(message = "Дата релиза не может быть раньше 28 декабря 1895 года")
    private LocalDate releaseDate;

    @Positive(message = "Длительность должна быть положительной")
    private long duration;

    @Min(message = "Неверно указан id возрастного рейтинга", value = 1L)
    @Max(message = "Неверно указан id возрастного рейтинга", value = 5L)
    private long ratingId;

    List<@Positive Integer> genreIds;
}