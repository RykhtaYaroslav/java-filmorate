package ru.yandex.practicum.filmorate.dal.mappers.director;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
public class DirectorExtractor implements ResultSetExtractor<Map<Long, Set<Director>>> {
    @Override
    public Map<Long, Set<Director>> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, Set<Director>> directors = new HashMap<>();

        while (rs.next()) {
            Long filmId = rs.getLong("film_id");
            Long directorId = rs.getLong("id");
            String directorName = rs.getString("name");

            Director director = new Director(directorId, directorName);
            directors.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(director);
        }
        return directors;
    }
}
