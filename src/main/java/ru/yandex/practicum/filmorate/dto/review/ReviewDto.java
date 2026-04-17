package ru.yandex.practicum.filmorate.dto.review;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "reviewId")
public class ReviewDto {

    private Long reviewId;
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private Long useful;
}
