package ru.yandex.practicum.filmorate.dal.mappers.film;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enums.MpaRating;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashSet;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        Date sqlDate = rs.getDate("release_date");
        if (sqlDate != null) {
            film.setReleaseDate(sqlDate.toLocalDate());
        }
        film.setDuration(rs.getLong("duration"));

        int mpaId = rs.getInt("rating_id");

        film.setRating(MpaRating.fromId(mpaId));
        film.setGenres(new LinkedHashSet<>());
        film.setUserLikeIds(new HashSet<>());
        film.setDirectors(new LinkedHashSet<>());

        return film;
    }
}