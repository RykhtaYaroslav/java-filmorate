package ru.yandex.practicum.filmorate.dal.repositories.genre;

import ru.yandex.practicum.filmorate.model.enums.Genre;

import java.util.*;

public interface FilmGenreRepository {
    void addGenres(Long filmId, Set<Genre> genres);

    void deleteGenres(Long filmId);

    Map<Long, Set<Genre>> getGenres();

    Set<Genre> getGenres(Long filmId);

    Map<Long, Set<Genre>> getGenresForFilms(Collection<Long> filmIds);

    List<Genre> findAll();

    Optional<Genre> findById(int id);
}
