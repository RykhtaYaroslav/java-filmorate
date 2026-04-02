package ru.yandex.practicum.filmorate.dal.repositories;

import org.springframework.beans.BeanUtils;
import ru.yandex.practicum.filmorate.annotation.TestOnly;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

/**
 * @deprecated use FilmDbStorage instead
 */
@Deprecated(since = "db-migration", forRemoval = true)
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film updFilm) {
        Film oldFilm = films.get(updFilm.getId());
        BeanUtils.copyProperties(updFilm, oldFilm, "id", "likes");
        return oldFilm;
    }

    @Override
    public void delete(Long id) {
        films.remove(id);
    }

    @Override
    public Set<Film> getFilms() {
        return new LinkedHashSet<>(films.values());
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film addLike(Long filmId, Long userId) {
        return null;
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {

    }

    @Override
    public Collection<Film> getPopularFilms(Integer amount) {
        return List.of();
    }

    @TestOnly
    public void clearData() {
        films.clear();
    }
}
