package ru.yandex.practicum.filmorate.model;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.time.LocalDate;

@Slf4j
public class Validator {
    // Дата первого фильма в истории
    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);

    public static void validateFilm(Film film) {
        // Дата релиза — не раньше 28 декабря 1895 года
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            log.error("Некорректная дата релиза фильма: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        log.info("Валидация входящих данных о фильме пройдена успешно");
    }

    public static void validateUser(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            log.debug("Имя пользователя было пустым, установлен логин вместо имени");
            user.setName(user.getLogin());
        }

        log.info("Валидация пользователя пройдена успешно");
    }
}
