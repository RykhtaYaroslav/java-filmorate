package ru.yandex.practicum.filmorate.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReviewUpdateRequest {

    @NotNull
    @Positive
    private Long reviewId;

    @NotBlank
    private String content;

    @NotNull
    private Boolean isPositive;

    @NotNull
    @Positive
    private Long userId;

    @NotNull
    @Positive
    private Long filmId;

    @NotNull
    private Long useful;
}
