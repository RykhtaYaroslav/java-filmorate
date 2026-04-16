package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
public class RecommendationController {
    private final FilmService filmService;

    @GetMapping("/users/{id}/recommendations")
    public Collection<FilmDto> findById(@PathVariable @Positive Long id) {
        return filmService.getFilmRecommendations(id);
    }
}
