package ru.yandex.practicum.filmorate.dal.repositories;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.film.GenreBatchSetter;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

@Repository
public class FilmDbStorage extends BaseStorage<Film> implements FilmStorage {
    private final ResultSetExtractor<Set<Film>> extractor;

    private static final String FIND_BY_ID_QUERY = """
            SELECT f.*, m.name AS rating_name, g.id AS genre_id, g.name AS genre_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.rating_id = m.id
            LEFT JOIN film_genres fg ON f.id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.id
            WHERE f.id = ?
            """;

    private static final String SET_GENRES_QUERY = """
            INSERT INTO film_genres (film_id, genre_id)
            VALUES (?, ?)
            """;

    private static final String CREATE_FILM_QUERY = """
            INSERT INTO films (name, description, release_date, duration, rating_id)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_FIELDS_QUERY = """
            UPDATE films SET
            name = COALESCE(?, name),
            description = COALESCE(?, description),
            release_date = COALESCE(?, release_date),
            duration = COALESCE(?, duration),
            rating_id = COALESCE(?, rating_id)
            WHERE id = ?
            """;

    private static final String DELETE_GENRES_QUERY = """
            DELETE FROM film_genres
            WHERE film_id = ?
            """;

    private static final String FIND_ALL_WITH_GENRES_QUERY = """
            SELECT f.*, m.name AS rating_name, g.id AS genre_id, g.name AS genre_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.rating_id = m.id
            LEFT JOIN film_genres fg ON f.id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.id
            ORDER BY f.id, g.id
            """;

    private static final String DELETE_FILM = "DELETE FROM films WHERE id = ?";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, ResultSetExtractor<Set<Film>> extractor) {
        super(jdbc, mapper);
        this.extractor = extractor;
    }

    @Override
    public Film create(Film film) {
        long id = insert(CREATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRating() != null ? film.getRating().getId() : null);

        film.setId(id);
        setGenres(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        update(UPDATE_FIELDS_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRating().getId(),
                film.getId());

        if (film.getGenres() != null) {
            delete(DELETE_GENRES_QUERY, film.getId());
            setGenres(film);
        }

        return findById(film.getId()).orElseThrow(() ->
                new IllegalStateException("Фильм был обновлен, но не найден. Этого не должно было случиться."));
    }

    @Override
    public void delete(Long id) {
        delete(DELETE_FILM, id);
    }

    @Override
    public Set<Film> getFilms() {
        return findMany(FIND_ALL_WITH_GENRES_QUERY, extractor);
    }

    @Override
    public Optional<Film> findById(Long id) {
        Set<Film> film = jdbc.query(FIND_BY_ID_QUERY, extractor, id);
        return film != null ? film.stream().findFirst() : Optional.empty();
    }

    private void setGenres(Film film) {
        jdbc.batchUpdate(SET_GENRES_QUERY, new GenreBatchSetter(film.getId(), new ArrayList<>(film.getGenres())));
    }
}