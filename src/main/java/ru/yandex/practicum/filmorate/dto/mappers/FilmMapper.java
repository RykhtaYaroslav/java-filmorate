package ru.yandex.practicum.filmorate.dto.mappers;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.dto.director.DirectorForFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.FilmCreateRequest;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.FilmUpdateRequest;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.dto.mpa.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enums.Genre;
import ru.yandex.practicum.filmorate.model.enums.MpaRating;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public final class FilmMapper {

    public Film mapToFilm(FilmCreateRequest request) {
        Film film = new Film();

        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        if (request.getMpa() != null && request.getMpa().getId() != null) {
            film.setRating(MpaRating.fromId(request.getMpa().getId()));
        }

        if (request.getGenres() != null) {
            film.setGenres(request.getGenres().stream()
                    .map(genreReq -> Genre.fromId(genreReq.getId()))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        } else {
            film.setGenres(new LinkedHashSet<>());
        }

        setDirectors(request.getDirectors(), film);
        return film;
    }

    public Film mapToFilm(FilmUpdateRequest request) {
        Film film = new Film();
        film.setId(request.getId());

        if (request.getName() != null) film.setName(request.getName());
        if (request.getDescription() != null) film.setDescription(request.getDescription());
        if (request.getReleaseDate() != null) film.setReleaseDate(request.getReleaseDate());
        if (request.getDuration() != null) film.setDuration(request.getDuration());

        if (request.getMpa() != null && request.getMpa().getId() != null) {
            film.setRating(MpaRating.fromId(request.getMpa().getId()));
        }

        if (request.getGenres() != null) {
            film.setGenres(request.getGenres().stream()
                    .map(genreReq -> Genre.fromId(genreReq.getId()))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }

        setDirectors(request.getDirectors(), film);
        return film;
    }

    public FilmDto mapToFilmDto(Film film) {
        FilmDto dto = new FilmDto();

        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        if (film.getRating() != null) {
            MpaRatingDto mpaDto = new MpaRatingDto();
            mpaDto.setId(film.getRating().getId());
            mpaDto.setName(film.getRating().toString());
            dto.setMpa(mpaDto);
        }

        if (film.getGenres() != null) {
            dto.setGenres(film.getGenres().stream()
                    .map(genre -> {
                        GenreDto gDto = new GenreDto();
                        gDto.setId(genre.getId());
                        gDto.setName(genre.getName());
                        return gDto;
                    })
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        } else {
            dto.setGenres(Collections.emptySet());
        }

        if (film.getUserLikeIds() != null) {
            dto.setUserLikeIds(new java.util.HashSet<>(film.getUserLikeIds()));
        } else {
            dto.setUserLikeIds(Collections.emptySet());
        }

        if (film.getDirectors() != null) {
            dto.setDirectors(film.getDirectors().stream()
                    .map(DirectorMapper::mapToDirectorDto)
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        } else {
            dto.setDirectors(Collections.emptySet());
        }

        return dto;
    }
    
    private void setDirectors(Set<DirectorForFilmRequest> directorForFilmRequests, Film film) {
        if (directorForFilmRequests != null){
            Set<Director> directors = directorForFilmRequests.stream()
                    .map(DirectorMapper::mapToDirectorForFilm)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            film.setDirectors(directors);
        } else {
            film.setDirectors(new LinkedHashSet<>());
        }
    }
}