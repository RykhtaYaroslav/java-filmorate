package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.film.FilmExtractor;
import ru.yandex.practicum.filmorate.dal.mappers.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.repositories.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.repositories.UserDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.user.UserRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.user.UserExtractor;
import ru.yandex.practicum.filmorate.dal.mappers.user.FriendshipRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.MpaRating;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        FilmDbStorage.class,
        FilmRowMapper.class,
        FilmExtractor.class,
        UserDbStorage.class,
        UserRowMapper.class,
        UserExtractor.class,
        FriendshipRowMapper.class
})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    @DisplayName("Проверка сохранения и поиска фильма по ID")
    void testCreateAndFindFilm() {
        Film newFilm = TestDataGenerator.generateFilm();
        Film savedFilm = filmStorage.create(newFilm);

        Optional<Film> filmOptional = filmStorage.findById(savedFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film).hasFieldOrPropertyWithValue("id", savedFilm.getId());
                    assertThat(film).hasFieldOrPropertyWithValue("name", newFilm.getName());
                    assertThat(film.getRating()).isEqualTo(MpaRating.G);
                });
    }

    @Test
    @DisplayName("Проверка обновления данных фильма")
    void testUpdateFilm() {
        Film film = filmStorage.create(TestDataGenerator.generateFilm());
        film.setName("Updated Movie Name");
        film.setDuration(180L);
        film.setRating(MpaRating.R);

        filmStorage.update(film);
        Film updatedFilm = filmStorage.findById(film.getId()).get();

        assertThat(updatedFilm.getName()).isEqualTo("Updated Movie Name");
        assertThat(updatedFilm.getDuration()).isEqualTo(180L);
        assertThat(updatedFilm.getRating()).isEqualTo(MpaRating.R);
    }

    @Test
    @DisplayName("Проверка получения списка всех фильмов")
    void testGetAllFilms() {
        filmStorage.create(TestDataGenerator.generateFilm());
        filmStorage.create(TestDataGenerator.generateFilm());

        Collection<Film> films = filmStorage.getFilms();

        assertThat(films).hasSize(2);
    }

    @Test
    @DisplayName("Проверка удаления фильма")
    void testDeleteFilm() {
        Film film = filmStorage.create(TestDataGenerator.generateFilm());
        filmStorage.delete(film.getId());

        Optional<Film> filmOptional = filmStorage.findById(film.getId());

        assertThat(filmOptional).isEmpty();
    }

    @Test
    @DisplayName("Проверка добавления и удаления лайка")
    void testLikeLogic() {
        Film film = filmStorage.create(TestDataGenerator.generateFilm());
        User user = userStorage.create(TestDataGenerator.generateUser());

        filmStorage.addLike(film.getId(), user.getId());

        Collection<Film> popular = filmStorage.getPopularFilms(10);

        assertThat(popular).isNotEmpty();
        assertThat(popular.iterator().next().getId()).isEqualTo(film.getId());

        filmStorage.deleteLike(film.getId(), user.getId());
    }

    @Test
    @DisplayName("Проверка получения популярных фильмов")
    void testGetPopularFilms() {
        Film film1 = filmStorage.create(TestDataGenerator.generateFilm());
        Film film2 = filmStorage.create(TestDataGenerator.generateFilm());
        User user1 = userStorage.create(TestDataGenerator.generateUser());
        User user2 = userStorage.create(TestDataGenerator.generateUser());

        // У film2 будет 2 лайка, у film1 - 1 лайк
        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());

        Collection<Film> popular = filmStorage.getPopularFilms(10);

        assertThat(popular).hasSize(2);
        assertThat(popular.iterator().next().getId()).isEqualTo(film2.getId());
    }

    @Test
    @DisplayName("Ошибка при поиске несуществующего фильма")
    void testFindFilmByIdNotFound() {
        Optional<Film> filmOptional = filmStorage.findById(999L);
        assertThat(filmOptional).isEmpty();
    }

    @Test
    @DisplayName("Ошибка удаления несуществующего фильма")
    void testDeleteNonExistentFilm() {
        assertThrows(NotFoundException.class, () -> filmStorage.delete(999L));
    }

    @Test
    @DisplayName("Ошибка при лайке несуществующего фильма")
    void testLikeNonExistentFilm() {
        User user = userStorage.create(TestDataGenerator.generateUser());
        Long id = user.getId();
        assertThrows(NotFoundException.class,
                () -> filmStorage.addLike(999L, id));
    }
}