package ru.yandex.practicum.filmorate.dto.film;

import lombok.Data;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.dto.mpa.MpaRatingDto;

import java.time.LocalDate;
import java.util.Set;

@Data
public class FilmDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;
    private MpaRatingDto mpa;
    private Set<GenreDto> genres;
    private Set<Long> userLikeIds;
    private Set<DirectorDto> directors;
}
