package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Film.
 */
@Data
@Builder(toBuilder = true)
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;
    // Я хотел использовать тут Duration, но в ТЗ написано:
    // Мы подготовили набор тестовых данных — Postman-коллекцию. С её помощью вы сможете протестировать ваш API: postman.json.
    // Эти тесты ожидали duration не как Duration, а как простое число
}
