package ru.yandex.practicum.filmorate.dal.repositories.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface DirectorRepository {
    Director create(Director director);

    Director update(Director director);

    void delete(Long id);

    Collection<Director> findAll();

    Optional<Director> findById(Long id);

    void setDirectorsToFilm(Long filmId, Collection<Director> directors);

    Collection<Director> getDirectorsByFilmId(Long filmId);

    Map<Long, Set<Director>> getDirectorsForFilms(Collection<Long> filmIds);

    void deleteFilmDirectors(Long filmId);
}
