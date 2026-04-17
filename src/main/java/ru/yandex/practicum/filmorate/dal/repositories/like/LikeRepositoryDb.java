package ru.yandex.practicum.filmorate.dal.repositories.like;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.like.LikeExtractor;
import ru.yandex.practicum.filmorate.exceptions.DataConflictException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class LikeRepositoryDb implements LikeRepository {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final LikeExtractor extractor;

    private static final String ADD_LIKE_QUERY = """
            INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)
            """;
    private static final String DELETE_LIKE_QUERY = """
            DELETE FROM film_likes
            WHERE film_id = ? AND user_id = ?
            """;
    private static final String GET_LIKES_QUERY = """
            SELECT film_id, user_id
            FROM film_likes
            """;
    private static final String GET_LIKES_BY_FILM_ID_QUERY = """
            SELECT user_id
            FROM film_likes
            WHERE film_id = ?
            """;
    private static final String GET_LIKES_FOR_FILMS_QUERY = """
            SELECT film_id, user_id
            FROM film_likes
            WHERE film_id IN (:filmIds)
            """;

    @Override
    public void addLike(Long filmId, Long userId) {
        try {
            jdbc.update(ADD_LIKE_QUERY, filmId, userId);
        } catch (DuplicateKeyException e) {
            throw new DataConflictException(String.format("Лайк уже существует: user %d -> film %d", userId, filmId));
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException(String.format("Не найден фильм %d или пользователь %d", filmId, userId));
        }
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        int rowsAffected = jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
        if (rowsAffected == 0) {
            throw new NotFoundException(String.format("Лайк пользователя %d фильму %d не найден", userId, filmId));
        }
    }

    @Override
    public Map<Long, Set<Long>> getLikes() {
        return jdbc.query(GET_LIKES_QUERY, rs -> {
            Map<Long, Set<Long>> likes = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                long userId = rs.getLong("user_id");
                likes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
            }
            return likes;
        });
    }

    @Override
    public Set<Long> getLikes(Long filmId) {
        return new HashSet<>(jdbc.query(GET_LIKES_BY_FILM_ID_QUERY, (rs, rowNum) -> rs.getLong("user_id"), filmId));
    }

    @Override
    public Map<Long, Set<Long>> getLikesForFilms(Collection<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Collection<Long>> params = Map.of("filmIds", filmIds);

        return namedJdbc.query(GET_LIKES_FOR_FILMS_QUERY, params, extractor);
    }
}
