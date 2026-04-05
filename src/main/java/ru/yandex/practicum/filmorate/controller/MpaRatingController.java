package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.mpa.MpaRatingDto;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
@Validated
public class MpaRatingController {
    private static final int RATINGS_AMOUNT = 5;
    private final MpaService service;

    @GetMapping
    public List<MpaRatingDto> findAllMpa() {
        return service.findAllMpa();
    }

    @GetMapping("/{id}")
    public MpaRatingDto findById(@PathVariable int id) {
        return service.findById(id);
    }
}
