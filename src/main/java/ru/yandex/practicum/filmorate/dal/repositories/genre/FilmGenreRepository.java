package ru.yandex.practicum.filmorate.dal.repositories.genre;

import ru.yandex.practicum.filmorate.model.enums.Genre;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface FilmGenreRepository {
    void addGenres(Long filmId, Set<Genre> genres);

    void deleteGenres(Long filmId);

    Map<Long, Set<Genre>> getGenres();

    Set<Genre> getGenres(Long filmId);

    List<Genre> findAll();

    Optional<Genre> findById(int id);
}
