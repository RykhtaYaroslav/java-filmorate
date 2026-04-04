package ru.yandex.practicum.filmorate.dto.film;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class FilmDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;
    private Integer ratingId;
    private Set<Integer> genreIds;
    private Set<Long> userLikeIds;
}