package ru.yandex.practicum.filmorate.dal.repositories.genre;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.film.GenreBatchSetter;
import ru.yandex.practicum.filmorate.dal.repositories.BaseStorage;
import ru.yandex.practicum.filmorate.model.enums.Genre;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class FilmGenreRepositoryDb extends BaseStorage<Genre> implements FilmGenreRepository {
    private static final String ADD_GENRES_QUERY = """
            INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)
            """;
    private static final String DELETE_GENRES_QUERY = """
            DELETE FROM film_genres WHERE film_id = ?
            """;
    private static final String GET_GENRES_QUERY = """
            SELECT fg.film_id, g.id
            FROM film_genres fg
            JOIN genres g ON fg.genre_id = g.id
            ORDER BY g.id
            """;
    private static final String GET_GENRES_BY_FILM_ID_QUERY = """
            SELECT genre_id
            FROM film_genres
            WHERE film_id = ?
            ORDER BY genre_id
            """;
    private static final String GET_GENRES_FOR_FILMS_QUERY = """
            SELECT fg.film_id, g.id
            FROM film_genres fg
            JOIN genres g ON fg.genre_id = g.id
            WHERE fg.film_id IN (:filmIds)
            ORDER BY g.id
            """;
    private static final String FIND_ALL_QUERY = """
            SELECT * FROM genres ORDER BY id
            """;
    private static final String FIND_BY_ID_QUERY = """
            SELECT * FROM genres WHERE id = ?
            """;

    public FilmGenreRepositoryDb(NamedParameterJdbcTemplate namedJdbc, RowMapper<Genre> mapper) {
        super(namedJdbc, mapper);
    }

    @Override
    public void addGenres(Long filmId, Set<Genre> genres) {
        jdbc.batchUpdate(ADD_GENRES_QUERY, new GenreBatchSetter(filmId, new ArrayList<>(genres)));
    }

    @Override
    public void deleteGenres(Long filmId) {
        jdbc.update(DELETE_GENRES_QUERY, filmId);
    }

    @Override
    public Map<Long, Set<Genre>> getGenres() {
        return jdbc.query(GET_GENRES_QUERY, rs -> {
            Map<Long, Set<Genre>> filmGenres = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                int genreId = rs.getInt("id");
                filmGenres.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(Genre.fromId(genreId));
            }
            return filmGenres;
        });
    }

    @Override
    public Set<Genre> getGenres(Long filmId) {
        List<Genre> genres = jdbc.query(GET_GENRES_BY_FILM_ID_QUERY, (rs, rowNum) -> Genre.fromId(rs.getInt("genre_id")), filmId);
        return new LinkedHashSet<>(genres);
    }

    @Override
    public Map<Long, Set<Genre>> getGenresForFilms(Collection<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> params = Collections.singletonMap("filmIds", filmIds);

        return namedJdbc.query(GET_GENRES_FOR_FILMS_QUERY, params, rs -> {
            Map<Long, Set<Genre>> filmGenres = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                int genreId = rs.getInt("id");
                filmGenres.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(Genre.fromId(genreId));
            }
            return filmGenres;
        });
    }

    public List<Genre> findAll() {
        return jdbc.query(FIND_ALL_QUERY, (rs, rowNum) -> Genre.fromId(rs.getInt("id")));
    }

    public Optional<Genre> findById(int id) {
        return jdbc.query(FIND_BY_ID_QUERY, rs -> {
            if (rs.next()) {
                return Optional.of(Genre.fromId(rs.getInt("id")));
            }
            return Optional.empty();
        }, id);
    }
}
