package ru.yandex.practicum.filmorate.dal.mappers.director;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class DirectorBatchSetter implements BatchPreparedStatementSetter {
    private final long filmId;
    private final List<Director> directors;

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        ps.setLong(1, filmId);
        ps.setLong(2, directors.get(i).getId());
    }

    @Override
    public int getBatchSize() {
        return directors.size();
    }
}
