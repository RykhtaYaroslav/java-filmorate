package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.film.FilmCreateRequest;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.FilmUpdateRequest;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
@Validated
@RequiredArgsConstructor
public class FilmController {
    private final FilmService service;

    @GetMapping("/{id}")
    public FilmDto findById(@PathVariable @Positive Long id) {
        return service.findById(id);
    }

    @GetMapping
    public Collection<FilmDto> findAll() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto create(@Valid @RequestBody FilmCreateRequest filmCreateRequest) {
        return service.create(filmCreateRequest);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody FilmUpdateRequest filmUpdateRequest) {
        return service.update(filmUpdateRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public FilmDto addLike(@PathVariable @Positive Long id, @PathVariable @Positive Long userId) {
        return service.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public FilmDto deleteLike(@PathVariable @Positive Long id, @PathVariable @Positive Long userId) {
        return service.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<FilmDto> getPopularFilms(@RequestParam(defaultValue = "10") Integer count,
                                               @RequestParam(required = false) Integer genreId,
                                               @RequestParam(required = false) Integer year) {
        return service.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/director/{directorId}")
    public Collection<FilmDto> getFilmsByDirector(@PathVariable @Positive Long directorId, @RequestParam(defaultValue = "year") String sortBy) {
        return service.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/common")
    public Collection<FilmDto> getCommonFilms(@RequestParam @Positive Long userId, @RequestParam @Positive Long friendId) {
        return service.getCommonFilms(userId, friendId);
    }

    @GetMapping("/search")
    public Collection<FilmDto> searchFilms(@RequestParam String query, @RequestParam String by) {
        return service.searchFilms(query, by);
    }
}