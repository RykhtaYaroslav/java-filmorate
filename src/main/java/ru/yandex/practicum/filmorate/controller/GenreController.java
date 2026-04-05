package ru.yandex.practicum.filmorate.controller;

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
    public GenreDto findById(@PathVariable int id) {
        return service.findById(id);
    }
}