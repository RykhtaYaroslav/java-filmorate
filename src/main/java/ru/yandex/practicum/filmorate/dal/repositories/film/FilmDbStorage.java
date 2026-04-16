package ru.yandex.practicum.filmorate.dal.repositories.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.repositories.BaseStorage;
import ru.yandex.practicum.filmorate.dal.repositories.genre.FilmGenreRepository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

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
            LEFT JOIN film_genres g ON f.id = g.film_id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            WHERE (? IS NULL OR g.genre_id = ?) AND (? IS NULL OR EXTRACT(YEAR FROM CAST(f.release_date AS DATE)) = ?)
            GROUP BY f.id
            ORDER BY COUNT(DISTINCT fl.user_id) DESC
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
    public Collection<Film> getPopularFilms(Integer amount, Integer genreId, Integer year) {
        return findMany(FIND_POPULAR_QUERY, extractor,
                genreId,
                genreId,
                year,
                year,
                amount);
    }

    @Override
    public Film addLike(Long filmId, Long userId) {
        throw new UnsupportedOperationException("Этот метод должен быть реализован в сервисном слое");
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        throw new UnsupportedOperationException("Этот метод должен быть реализован в сервисном слое");
    }
}
