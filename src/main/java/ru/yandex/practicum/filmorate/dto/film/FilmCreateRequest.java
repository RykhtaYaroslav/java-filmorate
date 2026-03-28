package ru.yandex.practicum.filmorate.dto.film;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FilmCreateRequest {
    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    @PastOrPresent(message = "Дата релиза не может быть в будущем")
    private LocalDate releaseDate;

    @NotNull
    @Positive(message = "Длительность должна быть положительной")
    private Long duration;

    @NotNull(message = "Возрастной рейтинг должен быть указан")
    @Positive(message = "Неверно указан id возрастного рейтинга")
    private Long ratingId;

    @NotEmpty(message = "Жанры фильма должны быть указаны")
    List<@NotNull @Positive Integer> genreIds;
}