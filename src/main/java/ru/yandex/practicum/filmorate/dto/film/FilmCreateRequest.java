package ru.yandex.practicum.filmorate.dto.film;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.MinimumDate;
import ru.yandex.practicum.filmorate.dto.director.DirectorForFilmRequest;
import ru.yandex.practicum.filmorate.dto.genre.GenreRequest;
import ru.yandex.practicum.filmorate.dto.mpa.MpaRatingRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class FilmCreateRequest {
    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    @PastOrPresent(message = "Дата релиза не может быть в будущем")
    @MinimumDate(message = "Дата релиза не может быть раньше 28 декабря 1895 года")
    private LocalDate releaseDate;

    @NotNull
    @Positive(message = "Длительность должна быть положительной")
    private Long duration;

    private MpaRatingRequest mpa;

    private List<GenreRequest> genres;

    private Set<DirectorForFilmRequest> directors;
}
