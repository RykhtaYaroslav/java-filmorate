package ru.yandex.practicum.filmorate.dal.mappers.like;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class LikeExtractor implements ResultSetExtractor<Map<Long, Set<Long>>> {
    @Override
    public Map<Long, Set<Long>> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, Set<Long>> likes = new HashMap<>();

        while (rs.next()) {
            long filmId = rs.getLong("film_id");
            long userId = rs.getLong("user_id");
            likes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        }

        return likes;
    }
}
