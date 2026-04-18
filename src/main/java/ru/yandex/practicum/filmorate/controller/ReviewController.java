package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
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
import ru.yandex.practicum.filmorate.dto.review.ReviewCreateRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewUpdateRequest;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {

    private final ReviewService service;

    @GetMapping
    public Collection<ReviewDto> getAll(@RequestParam(required = false) Long filmId, @RequestParam(required = false) Long count) {
        return service.getAll(filmId, count);
    }

    @GetMapping("/{id}")
    public ReviewDto getById(@PathVariable(required = false) Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto create(@RequestBody @Valid ReviewCreateRequest request) {
        return service.create(request);
    }

    @PutMapping
    public ReviewDto update(@RequestBody @Valid ReviewUpdateRequest request) {
        return service.update(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable @Positive @NotNull Long id, @PathVariable @Positive @NotNull Long userId) {
        service.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable @Positive @NotNull Long id, @PathVariable @Positive @NotNull Long userId) {
        service.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeReaction(@PathVariable @Positive @NotNull Long id, @PathVariable @Positive @NotNull Long userId) {
        service.removeReaction(id, userId);
    }
}
