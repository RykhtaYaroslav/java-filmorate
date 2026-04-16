package ru.yandex.practicum.filmorate.dal.repositories.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    void delete(Long id);

    Set<Film> getFilms();

    Optional<Film> findById(Long id);

    Film addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    Collection<Film> getPopularFilms(Integer amount);

    Collection<Film> getFilmsByDirector(Long directorId, String sortBy);
}
