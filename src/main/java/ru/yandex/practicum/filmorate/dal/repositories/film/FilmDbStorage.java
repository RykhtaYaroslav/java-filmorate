package ru.yandex.practicum.filmorate.dal.repositories.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.repositories.BaseStorage;
import ru.yandex.practicum.filmorate.dal.repositories.genre.FilmGenreRepository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Repository
public class FilmDbStorage extends BaseStorage<Film> implements FilmStorage {
    private final FilmGenreRepository filmGenreRepository;
    private final ResultSetExtractor<Set<Film>> extractor;

    private static final String FIND_BY_ID_QUERY = """
            SELECT f.*, m.name AS rating_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.rating_id = m.id
            WHERE f.id = ?
            """;

    private static final String CREATE_FILM_QUERY = """
            INSERT INTO films (name, description, release_date, duration, rating_id)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String FIND_USER_LIKES_INTERSECTIONS = """
            SELECT fl2.user_id
            FROM film_likes fl1
            JOIN film_likes fl2 ON fl1.film_id = fl2.film_id
            WHERE fl1.user_id = ?
            AND fl2.user_id <> ?
            GROUP BY fl2.user_id
            ORDER BY COUNT(fl2.film_id) DESC
            LIMIT 1
            """;

    private static final String FIND_RECOMMENDATION_FILMS = """
            SELECT f.*, m.name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.rating_id = m.id
            WHERE f.id IN (
            SELECT fl.film_id
            FROM film_likes AS fl
            WHERE fl.user_id = ?
            AND fl.film_id NOT IN (SELECT film_id FROM film_likes WHERE user_id = ?)
            GROUP BY fl.film_id
            ORDER BY (SELECT COUNT(*) FROM film_likes WHERE film_id = fl.film_id) DESC
            );
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

    private static final String FIND_ALL_QUERY = """
            SELECT f.*, m.name AS rating_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.rating_id = m.id
            ORDER BY f.id
            """;

    private static final String DELETE_FILM = "DELETE FROM films WHERE id = ?";

    private static final String FIND_POPULAR_QUERY = """
            SELECT f.*, m.name AS rating_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.rating_id = m.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            GROUP BY f.id
            ORDER BY COUNT(fl.user_id) DESC
            LIMIT ?
            """;


    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, ResultSetExtractor<Set<Film>> extractor, FilmGenreRepository filmGenreRepository) {
        super(jdbc, mapper);
        this.extractor = extractor;
        this.filmGenreRepository = filmGenreRepository;
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
        if (film.getGenres() != null) {
            filmGenreRepository.addGenres(id, film.getGenres());
        }
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
            filmGenreRepository.deleteGenres(film.getId());
            if (!film.getGenres().isEmpty()) {
                filmGenreRepository.addGenres(film.getId(), film.getGenres());
            }
        }
        return film;
    }

    @Override
    public void delete(Long id) {
        delete(DELETE_FILM, id);
    }

    @Override
    public Set<Film> getFilms() {
        return findMany(FIND_ALL_QUERY, extractor);
    }

    @Override
    public Optional<Film> findById(Long id) {
        Set<Film> film = jdbc.query(FIND_BY_ID_QUERY, extractor, id);
        return film != null ? film.stream().findFirst() : Optional.empty();
    }

    @Override
    public Collection<Film> getPopularFilms(Integer amount) {
        return findMany(FIND_POPULAR_QUERY, extractor, amount);
    }

    @Override
    public Film addLike(Long filmId, Long userId) {
        throw new UnsupportedOperationException("Этот метод должен быть реализован в сервисном слое");
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        throw new UnsupportedOperationException("Этот метод должен быть реализован в сервисном слое");
    }

    @Override
    public Collection<Film> getRecommendationFilms(Long userId) {
        return findUserWithMostIntersections(userId)
                .map(neighborId -> {
                    return jdbc.query(FIND_RECOMMENDATION_FILMS, extractor, neighborId, userId);
                })
                .orElseGet(Collections::emptySet);
    }

    public Optional<Long> findUserWithMostIntersections(Long userId) {
        List<Long> result = jdbc.query(
                FIND_USER_LIKES_INTERSECTIONS,
                (rs, rowNum) -> rs.getLong("user_id"),
                userId, userId
        );

        return result.stream().findFirst();
    }
}
