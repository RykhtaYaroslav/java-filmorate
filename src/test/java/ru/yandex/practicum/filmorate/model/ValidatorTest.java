package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Тестирование валидатора моделей")
class ValidatorTest {

    @Test
    @DisplayName("Фильм с датой релиза раньше 28.12.1895 должен выбрасывать ValidationException")
    void validateFilm_ReleaseDateTooEarly_ThrowsValidationException() {
        Film film = Film.builder()
                .name("Название")
                .description("Описание")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(120L)
                .build();

        assertThrows(ValidationException.class, () -> Validator.validateFilm(film));
    }

    @Test
    @DisplayName("Корректный фильм должен проходить валидацию без исключений")
    void validateFilm_ValidFilm_PassesValidation() {
        Film film = Film.builder()
                .name("Название")
                .description("Короткое описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        assertDoesNotThrow(() -> Validator.validateFilm(film));
    }

    @Test
    @DisplayName("Фильм с минимально допустимой датой релиза должен проходить валидацию")
    void validateFilm_MinimumReleaseDate_PassesValidation() {
        Film film = Film.builder()
                .name("Название")
                .description("Описание")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(60L)
                .build();

        assertDoesNotThrow(() -> Validator.validateFilm(film));
    }

    @Test
    @DisplayName("Корректный пользователь должен проходить валидацию без исключений")
    void validateUser_ValidUser_PassesValidation() {
        User user = User.builder()
                .email("test@example.com")
                .login("login")
                .name("Имя")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertDoesNotThrow(() -> Validator.validateUser(user));
    }

    @Test
    @DisplayName("Пользователь с null именем должен проходить валидацию (имя необязательно)")
    void validateUser_NullName_PassesValidation() {
        User user = User.builder()
                .email("test@example.com")
                .login("login")
                .name(null)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertDoesNotThrow(() -> Validator.validateUser(user));
    }

    @Test
    @DisplayName("Пользователь с null датой рождения должен проходить валидацию")
    void validateUser_NullBirthday_PassesValidation() {
        User user = User.builder()
                .email("test@example.com")
                .login("login")
                .name("Имя")
                .birthday(null)
                .build();

        assertDoesNotThrow(() -> Validator.validateUser(user));
    }

    @Test
    @DisplayName("Пользователь с сегодняшней датой рождения должен проходить валидацию")
    void validateUser_TodayBirthday_PassesValidation() {
        User user = User.builder()
                .email("test@example.com")
                .login("login")
                .name("Имя")
                .birthday(LocalDate.now())
                .build();

        assertDoesNotThrow(() -> Validator.validateUser(user));
    }
}