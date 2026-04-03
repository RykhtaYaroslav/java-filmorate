package ru.yandex.practicum.filmorate.dal.repositories;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.film.GenreBatchSetter;
import ru.yandex.practicum.filmorate.exceptions.DataConflictException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public class FilmDbStorage extends BaseStorage<Film> implements FilmStorage {
    private final ResultSetExtractor<Set<Film>> extractor;

    private static final String FIND_BY_ID_QUERY = """
            SELECT f.*, m.name AS rating_name, g.id AS genre_id, g.name AS genre_name, l.user_id
            FROM films f
            LEFT JOIN mpa_ratings m ON f.rating_id = m.id
            LEFT JOIN film_genres fg ON f.id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.id
            LEFT JOIN film_likes l ON f.id = l.film_id
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
            SELECT f.*, m.name AS rating_name, g.id AS genre_id, g.name AS genre_name, l.user_id
            FROM films f
            LEFT JOIN mpa_ratings m ON f.rating_id = m.id
            LEFT JOIN film_genres fg ON f.id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.id
            LEFT JOIN film_likes l ON f.id = l.film_id
            ORDER BY f.id
            """;

    private static final String DELETE_FILM = "DELETE FROM films WHERE id = ?";

    private static final String ADD_LIKE_QUERY = """
            INSERT INTO film_likes (film_id, user_id)
            VALUES (?, ?)
            """;

    private static final String DELETE_LIKE_QUERY = """
            DELETE FROM film_likes
            WHERE film_id = ? AND user_id = ?
            """;

    private static final String FIND_POPULAR_QUERY = """
            SELECT f.*, m.name AS rating_name, g.id AS genre_id, g.name AS genre_name, l.user_id
            FROM films f
            LEFT JOIN mpa_ratings m ON f.rating_id = m.id
            LEFT JOIN film_genres fg ON f.id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.id
            LEFT JOIN film_likes l ON f.id = l.film_id
            WHERE f.id IN (
                SELECT f2.id
                FROM films f2
                LEFT JOIN film_likes fl ON f2.id = fl.film_id
                GROUP BY f2.id
                ORDER BY COUNT(fl.user_id) DESC
                LIMIT ?
            )
            ORDER BY (
                SELECT COUNT(fl2.user_id)
                FROM film_likes fl2
                WHERE fl2.film_id = f.id
            ) DESC, f.id ASC
            """;

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

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
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

    @Override
    public Film addLike(Long filmId, Long userId) {
        try {
            jdbc.update(ADD_LIKE_QUERY, filmId, userId);
        } catch (DuplicateKeyException e) {
            throw new DataConflictException(String.format("Лайк уже существует: user %d -> film %d", userId, filmId));
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException(String.format("Не найден фильм %d или пользователь %d", filmId, userId));
        }

        return findById(filmId).orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        int rowsAffected = jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
        if (rowsAffected == 0) {
            throw new NotFoundException(String.format("Лайк пользователя %d фильму %d не найден", userId, filmId));
        }
    }

    @Override
    public Collection<Film> getPopularFilms(Integer amount) {
        return findMany(FIND_POPULAR_QUERY, extractor, amount);
    }

    private void setGenres(Film film) {
        jdbc.batchUpdate(SET_GENRES_QUERY, new GenreBatchSetter(film.getId(), new ArrayList<>(film.getGenres())));
    }
}