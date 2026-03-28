package ru.yandex.practicum.filmorate.dto.film;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FilmUpdateRequest {
    @NotNull
    @Positive
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private long duration;
    private long ratingId;
    List<Integer> genreIds;
}