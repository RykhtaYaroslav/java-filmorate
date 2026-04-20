package ru.yandex.practicum.filmorate.dal.repositories.director;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.director.DirectorBatchSetter;
import ru.yandex.practicum.filmorate.dal.mappers.director.DirectorExtractor;
import ru.yandex.practicum.filmorate.dal.repositories.BaseRepository;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class DirectorDbRepository extends BaseRepository<Director> implements DirectorRepository {
    private final DirectorExtractor extractor;

    private static final String FIND_ALL_QUERY = """
            SELECT *
            FROM directors
            ORDER BY id
            """;
    private static final String FIND_BY_ID_QUERY = """
            SELECT *
            FROM directors
            WHERE id = ?
            """;
    private static final String CREATE_QUERY = """
            INSERT INTO directors (name)
            VALUES (?)
            """;
    private static final String UPDATE_QUERY = """
            UPDATE directors
            SET name = ?
            WHERE id = ?
            """;
    private static final String DELETE_QUERY = """
            DELETE
            FROM directors
            WHERE id = ?
            """;

    private static final String SAVE_FILM_DIRECTORS_QUERY = """
            INSERT INTO film_directors (film_id, director_id)
            VALUES (?, ?)
            """;

    private static final String GET_DIRECTORS_BY_FILM_ID_QUERY = """
            SELECT d.*
            FROM directors AS d
            JOIN film_directors AS fd ON d.id = fd.director_id
            WHERE fd.film_id = ?
            """;

    private static final String GET_DIRECTORS_FOR_FILMS_QUERY = """
            SELECT fd.film_id, d.id, d.name
            FROM directors AS d
            JOIN film_directors AS fd ON d.id = fd.director_id
            WHERE fd.film_id IN (:filmIds)
            """;

    private static final String DELETE_FILM_DIRECTORS_QUERY = """
            DELETE
            FROM film_directors
            WHERE film_id = ?
            """;

    public DirectorDbRepository(NamedParameterJdbcTemplate namedJdbc, RowMapper<Director> mapper, DirectorExtractor extractor) {
        super(namedJdbc, mapper);
        this.extractor = extractor;
    }

    @Override
    public Director create(Director director) {
        Long id = insert(CREATE_QUERY, director.getName());
        director.setId(id);
        return director;
    }

    @Override
    public Director update(Director director) {
        update(UPDATE_QUERY, director.getName(), director.getId());
        return director;
    }

    @Override
    public void delete(Long id) {
        delete(DELETE_QUERY, id);
    }

    @Override
    public Collection<Director> findAll() {
        return findMany(FIND_ALL_QUERY, mapper);
    }

    @Override
    public Optional<Director> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public void setDirectorsToFilm(Long filmId, Collection<Director> directors) {
        jdbc.batchUpdate(SAVE_FILM_DIRECTORS_QUERY, new DirectorBatchSetter(filmId, new ArrayList<>(directors)));
    }

    @Override
    public Collection<Director> getDirectorsByFilmId(Long filmId) {
        return findMany(GET_DIRECTORS_BY_FILM_ID_QUERY, mapper, filmId);
    }

    @Override
    public Map<Long, Set<Director>> getDirectorsForFilms(Collection<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Collection<Long>> params = Map.of("filmIds", filmIds);

        return namedJdbc.query(GET_DIRECTORS_FOR_FILMS_QUERY, params, extractor);
    }

    @Override
    public void deleteFilmDirectors(Long filmId) {
        jdbc.update(DELETE_FILM_DIRECTORS_QUERY, filmId);
    }
}
