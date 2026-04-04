package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
@Validated
public class GenreController {
    private static final int GENRES_AMOUNT = 20;
    private final GenreService service;

    @GetMapping
    public List<GenreDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public GenreDto findById(@PathVariable
                             @Min(value = 1, message = "id может быть от 1 до " + GENRES_AMOUNT)
                             @Max(value = GENRES_AMOUNT, message = "id может быть от 1 до " + GENRES_AMOUNT)
                             int id) {
        return service.findById(id);
    }
}