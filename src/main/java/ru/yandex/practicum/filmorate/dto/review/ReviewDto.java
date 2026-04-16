package ru.yandex.practicum.filmorate.dto.review;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class ReviewDto {

    private Long id;
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private Long useful;
}
