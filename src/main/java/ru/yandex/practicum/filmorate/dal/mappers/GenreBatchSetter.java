package ru.yandex.practicum.filmorate.dal.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import ru.yandex.practicum.filmorate.model.enums.Genre;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class GenreBatchSetter implements BatchPreparedStatementSetter {
    private final long filmId;
    private final List<Genre> genres;

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        ps.setLong(1, filmId);
        ps.setInt(2, genres.get(i).getId());
    }

    @Override
    public int getBatchSize() {
        return genres.size();
    }
}
