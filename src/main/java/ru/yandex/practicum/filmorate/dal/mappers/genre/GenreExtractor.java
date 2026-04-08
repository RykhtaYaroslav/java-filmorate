package ru.yandex.practicum.filmorate.dal.mappers.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.enums.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GenreExtractor implements ResultSetExtractor<List<Genre>> {
    private final GenreRowMapper genreRowMapper;

    @Override
    public List<Genre> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<Genre> genres = new ArrayList<>();

        while (rs.next()) {
            genres.add(genreRowMapper.mapRow(rs, rs.getRow()));
        }

        return genres;
    }
}