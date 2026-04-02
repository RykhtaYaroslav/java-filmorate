package ru.yandex.practicum.filmorate.dto.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.film.FilmCreateRequest;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.FilmUpdateRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enums.Genre;
import ru.yandex.practicum.filmorate.model.enums.MpaRating;

import java.util.Collections;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilmMapper {

    public static Film mapToFilm(FilmCreateRequest request) {
        Film film = new Film();

        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());
        film.setRating(MpaRating.fromId(request.getRatingId()));

        if (request.getGenreIds() != null) {
            film.setGenres(request.getGenreIds().stream()
                    .map(Genre::fromId)
                    .collect(Collectors.toSet()));
        }

        return film;
    }

    public static Film mapToFilm(FilmUpdateRequest request) {
        Film film = new Film();

        if (request.getName() != null) film.setName(request.getName());
        if (request.getDescription() != null) film.setDescription(request.getDescription());
        if (request.getReleaseDate() != null) film.setReleaseDate(request.getReleaseDate());
        if (request.getDuration() != null) film.setDuration(request.getDuration());
        if (request.getRatingId() != null) film.setRating(MpaRating.fromId(request.getRatingId()));

        if (request.getGenreIds() != null) {
            film.setGenres(request.getGenreIds().stream()
                    .map(Genre::fromId)
                    .collect(Collectors.toSet()));
        }

        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto dto = new FilmDto();

        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        if (film.getRating() != null) {
            dto.setRatingId(film.getRating().getId());
        }

        if (film.getGenres() != null) {
            dto.setGenreIds(film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet()));
        } else {
            dto.setGenreIds(Collections.emptySet());
        }
        dto.setUserLikeIds(film.getUserLikeIds());

        return dto;
    }
}