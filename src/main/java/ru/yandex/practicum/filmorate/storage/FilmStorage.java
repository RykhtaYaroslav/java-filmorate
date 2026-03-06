package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    public Film create(Film film, Long id);

    public Film update(Film film);

    public void delete(Long id);

    public Collection<Film> getFilms();

    public Optional<Film> findById(Long id);
}
