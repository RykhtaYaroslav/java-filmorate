package ru.yandex.practicum.filmorate.model;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Slf4j
public class Validator {

    private static final String SIMPLE_EMAIL_REGEX = "^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(SIMPLE_EMAIL_REGEX);

    // Дата первого фильма в истории
    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);

    public static void validateFilm(Film film) {
        log.info("Валидация входящих данных о фильме");

        if (film == null) {
            log.error("Фильм не может быть null");
            throw new ValidationException("Фильм не может быть null");
        }

        // Название не может быть пустым
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Название фильма оказалось пустым");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        // Максимальная длина описания — 200 символов
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Слишком длинное название фильма: {} символов", film.getDescription().length());
            throw new ValidationException("Описание фильма не может превышать 200 символов");
        }

        // Дата релиза — не раньше 28 декабря 1895 года
        if (film.getReleaseDate() == null) {
            log.error("Не указана дата релиза фильма");
            throw new ValidationException("Дата релиза должна быть указана");
        }
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            log.error("Некорректная дата релиза фильма: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        // Продолжительность фильма должна быть положительным числом
        if (film.getDuration() == null || film.getDuration() < 0) {
            log.error("Некорректная продолжительность фильма: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        log.info("Валидация пройдена успешно");
    }

    public static void validateUser(User user) {
        log.info("Валидация входящих данных о пользователе");

        if (user == null) {
            log.error("Пользователь не может быть null");
            throw new ValidationException("Пользователь не может быть null");
        }

        // Электронная почта не может быть пустой и должна содержать символ @
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Электронная почта пользователя оказалась пустой");
            throw new ValidationException("Электронная почта не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            log.error("Электронная почта пользователя не содержит символ @: {}", user.getEmail());
            throw new ValidationException("Электронная почта должна содержать символ @");
        }

        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            log.error("Некорректный формат электронной почты");
            throw new ValidationException("Некорректный формат электронной почты");
        }



        // Логин не может быть пустым и содержать пробелы
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("Логин пользователя оказался пустым");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Логин пользователя содержит пробелы: '{}'", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }

        // Дата рождения не может быть в будущем
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Некорректная дата рождения пользователя: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        if (user.getName() == null || user.getName().isEmpty()) {
            log.debug("Имя пользователя было пустым, установлен логин вместо имени");
            user.setName(user.getLogin());
        }

        log.info("Валидация пользователя пройдена успешно");
    }
}
