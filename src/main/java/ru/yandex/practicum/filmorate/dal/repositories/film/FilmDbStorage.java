package ru.yandex.practicum.filmorate.dal.repositories.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.film.FilmExtractor;
import ru.yandex.practicum.filmorate.dal.repositories.BaseStorage;
import ru.yandex.practicum.filmorate.dal.repositories.director.DirectorRepository;
import ru.yandex.practicum.filmorate.dal.repositories.genre.FilmGenreRepository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class FilmDbStorage extends BaseStorage<Film> implements FilmStorage {
    private final FilmGenreRepository filmGenreRepository;
    private final DirectorRepository directorRepository;
    private final FilmExtractor extractor;

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

    private static final String DELETE_FILM = """
            DELETE
            FROM films
            WHERE id = ?
            """;

    private static final String DELETE_FILM_DIRECTORS_QUERY = """
            DELETE
            FROM film_directors
            WHERE film_id = ?
            """;

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

    private static final String FIND_FILMS_BY_DIRECTOR_ID_QUERY = """
            SELECT f.*, m.name AS rating_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.rating_id = m.id
            INNER JOIN film_directors fd ON f.id = fd.film_id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            WHERE fd.director_id = ?
            GROUP BY f.id
            ORDER BY %s
            """;

    private static final String FIND_FILMS_BY_SEARCH_QUERY = """
            SELECT f.*, m.name AS rating_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.rating_id = m.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            %s
            GROUP BY f.id, m.name
            ORDER BY COUNT(fl.user_id) DESC
            """;

    private static final String FIND_LIKED_FILMS = """
            SELECT f.*, m.name AS rating_name
            FROM films f
            JOIN film_likes fl ON f.id = fl.film_id
            LEFT JOIN mpa_ratings m ON f.rating_id = m.id
            LEFT JOIN film_genres g ON f.id = g.film_id
            WHERE user_id = ?
            GROUP BY f.id
            ORDER BY COUNT(DISTINCT fl.user_id) DESC
            """;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, FilmExtractor extractor, FilmGenreRepository filmGenreRepository, DirectorRepository directorRepository) {
        super(jdbc, mapper);
        this.filmGenreRepository = filmGenreRepository;
        this.extractor = extractor;
        this.directorRepository = directorRepository;
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

        Collection<Director> directors = film.getDirectors();

        if (directors != null && !directors.isEmpty()) {
            directorRepository.setDirectorsToFilm(id, directors);
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

        updateGenres(film);
        updateDirectors(film);
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
        throw new UnsupportedOperationException("Метод должен быть реализован в сервисном слое");
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

    @Override
    public Collection<Film> getFilmsByDirector(Long directorId, String sortBy) {
        String orderBy = sortBy.equals("year") ? "f.release_date" : "COUNT(fl.user_id) DESC";

        String query = String.format(FIND_FILMS_BY_DIRECTOR_ID_QUERY, orderBy);

        return findMany(query, extractor, directorId);
    }

    @Override
    public Collection<Film> getSearchFilms(String query, String by) {
        final String searchPattern = "%" + query.toLowerCase() + "%";
        final List<String> searchBy = Arrays.asList(by.split(","));
        final boolean byTitle = searchBy.contains("title");
        final boolean byDirector = searchBy.contains("director");

        final List<Object> params = new ArrayList<>();

        StringBuilder whereClause = new StringBuilder();

        if (byTitle) {
            whereClause.append("LOWER(f.name) LIKE ?");
            params.add(searchPattern);
        }

        if (byDirector) {
            if (!whereClause.isEmpty()) {
                whereClause.append(" OR ");
            }
            whereClause.append("LOWER(d.name) LIKE ?");
            params.add(searchPattern);
        }

        if (params.isEmpty()) {
            return Collections.emptyList();
        }

        final String joins = byDirector ? """
                LEFT JOIN film_directors fd ON f.id = fd.film_id
                LEFT JOIN directors d ON fd.director_id = d.id
                """ : "";

        final String sql = String.format(FIND_FILMS_BY_SEARCH_QUERY, joins + " WHERE " + whereClause);

        return findMany(sql, extractor, params.toArray());
    }

    private Optional<Long> findUserWithMostIntersections(Long userId) {
        List<Long> result = jdbc.query(
                FIND_USER_LIKES_INTERSECTIONS,
                (rs, rowNum) -> rs.getLong("user_id"),
                userId, userId
        );
        return result.stream().findFirst();
    }

    private void updateGenres(Film film) {
        if (film.getGenres() != null) {
            filmGenreRepository.deleteGenres(film.getId());
            if (!film.getGenres().isEmpty()) {
                filmGenreRepository.addGenres(film.getId(), film.getGenres());
            }
        }
    }

    private void updateDirectors(Film film) {
        Collection<Director> directors = film.getDirectors();

        if (directors != null) {
            jdbc.update(DELETE_FILM_DIRECTORS_QUERY, film.getId());
            if (!directors.isEmpty()) {
                directorRepository.setDirectorsToFilm(film.getId(), directors);
            }
        }
    }

    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {

        Set<Film> userLikedFilms = findMany(FIND_LIKED_FILMS, extractor, userId);
        Set<Film> friendLikedFilms = findMany(FIND_LIKED_FILMS, extractor, friendId);

        return userLikedFilms.stream().filter(friendLikedFilms::contains).collect(Collectors.toSet());
    }
}
