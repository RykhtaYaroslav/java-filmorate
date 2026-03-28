package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class FilmDbStorage implements FilmStorage {
    @Override
    public Film create(Film film) {
        return null;
    }

    @Override
    public Film update(Film film) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public Collection<Film> getFilms() {
        return List.of();
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.empty();
    }
}
