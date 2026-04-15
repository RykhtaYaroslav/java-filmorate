package ru.yandex.practicum.filmorate.dal.repositories.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

public interface DirectorRepository {
    Director create(Director director);

    Director update(Director director);

    void delete(Long id);

    Collection<Director> findAll();

    Optional<Director> findById(Long id);

    void setDirectorsToFilm(Long filmId, Collection<Director> directors);
}
