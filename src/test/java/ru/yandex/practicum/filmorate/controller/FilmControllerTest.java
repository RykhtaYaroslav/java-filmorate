package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Тестирование FilmController")
class FilmControllerTest {

    private final FilmController filmController = new FilmController();

    @BeforeEach
    void setUp() {
        // Очищаем состояние контроллера перед каждым тестом
        filmController.clearData();
    }

    @Test
    @DisplayName("Создание корректного фильма должно возвращать фильм с ID")
    void create_ValidFilm_ReturnsFilmWithId() {
        Film film = Film.builder()
                .name("Название фильма")
                .description("Описание фильма")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm.getId());
        assertEquals("Название фильма", createdFilm.getName());
        assertEquals(1, filmController.findAll().size());
    }

    @Test
    @DisplayName("Создание фильма с пустым названием должно выбрасывать ValidationException")
    void create_FilmWithEmptyName_ThrowsValidationException() {
        Film film = Film.builder()
                .name("")
                .description("Описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    @DisplayName("Обновление существующего фильма должно сохранять изменения")
    void update_ExistingFilm_UpdatesFilm() {
        // Сначала создаем фильм
        Film film = Film.builder()
                .name("Старое название")
                .description("Старое описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();
        Film createdFilm = filmController.create(film);
        Long filmId = createdFilm.getId();

        // Обновляем фильм
        Film updatedFilm = Film.builder()
                .id(filmId)
                .name("Новое название")
                .description("Новое описание")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(120L)
                .build();

        Film result = filmController.update(updatedFilm);

        assertEquals(filmId, result.getId());
        assertEquals("Новое название", result.getName());
        assertEquals("Новое описание", result.getDescription());
        assertEquals(120L, result.getDuration());
    }

    @Test
    @DisplayName("Обновление фильма без ID должно выбрасывать ConditionsNotMetException")
    void update_FilmWithoutId_ThrowsConditionsNotMetException() {
        Film film = Film.builder()
                .name("Название")
                .description("Описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        assertThrows(ConditionsNotMetException.class, () -> filmController.update(film));
    }

    @Test
    @DisplayName("Обновление несуществующего фильма должно выбрасывать NotFoundException")
    void update_NonExistentFilm_ThrowsNotFoundException() {
        Film film = Film.builder()
                .id(999L)
                .name("Название")
                .description("Описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        assertThrows(NotFoundException.class, () -> filmController.update(film));
    }

    @Test
    @DisplayName("Получение всех фильмов должно возвращать пустой список при отсутствии фильмов")
    void findAll_NoFilms_ReturnsEmptyList() {
        assertTrue(filmController.findAll().isEmpty());
    }

    @Test
    @DisplayName("Создание нескольких фильмов должно генерировать уникальные ID")
    void create_MultipleFilms_GeneratesUniqueIds() {
        Film film1 = Film.builder()
                .name("Фильм 1")
                .description("Описание 1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        Film film2 = Film.builder()
                .name("Фильм 2")
                .description("Описание 2")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(120L)
                .build();

        Film created1 = filmController.create(film1);
        Film created2 = filmController.create(film2);

        assertNotNull(created1.getId());
        assertNotNull(created2.getId());
        assertNotEquals(created1.getId(), created2.getId());
        assertEquals(2, filmController.findAll().size());
    }
}