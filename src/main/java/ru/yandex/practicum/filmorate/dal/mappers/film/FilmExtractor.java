package ru.yandex.practicum.filmorate.dal.mappers.film;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enums.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class FilmExtractor implements ResultSetExtractor<Set<Film>> {
    private final FilmRowMapper filmMapper;

    @Override
    public Set<Film> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, Film> films = new LinkedHashMap<>();

        while (rs.next()) {
            long id = rs.getLong("id");

            Film film = films.get(id);

            if (film == null) {
                film = filmMapper.mapRow(rs, rs.getRow());
                films.put(id, film);
            }

            int genreId = rs.getInt("genre_id");
            if (!rs.wasNull() && genreId != 0) {
                film.getGenres().add(Genre.fromId(genreId));
            }

            long userId = rs.getLong("user_id");
            if (!rs.wasNull() && userId != 0) {
                film.getUserLikeIds().add(userId);
            }
        }
        return new LinkedHashSet<>(films.values());
    }
}
