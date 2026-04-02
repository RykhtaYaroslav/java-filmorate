package ru.yandex.practicum.filmorate.dto.film;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class FilmDto {
    private long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private long duration;
    private Integer ratingId;
    Set<Integer> genreIds;
}