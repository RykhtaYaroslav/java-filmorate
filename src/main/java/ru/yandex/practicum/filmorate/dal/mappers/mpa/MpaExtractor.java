package ru.yandex.practicum.filmorate.dal.mappers.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.enums.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MpaExtractor implements ResultSetExtractor<List<MpaRating>> {
    private final MpaRowMapper mpaMapper;

    @Override
    public List<MpaRating> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<MpaRating> mpaRatings = new ArrayList<>();

        while (rs.next()) {
            MpaRating rating = mpaMapper.mapRow(rs, rs.getRow());

            mpaRatings.add(rating);
        }
        return mpaRatings;
    }
}