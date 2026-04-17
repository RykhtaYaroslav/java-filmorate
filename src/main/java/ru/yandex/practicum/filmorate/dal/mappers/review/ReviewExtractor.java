package ru.yandex.practicum.filmorate.dal.mappers.review;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ReviewExtractor implements ResultSetExtractor<Set<Review>> {

    private final ReviewRowMapper rowMapper;

    @Override
    public Set<Review> extractData(ResultSet rs) throws SQLException, DataAccessException {
        final Set<Review> reviews = new LinkedHashSet<>();

        while (rs.next()) {
            Review review = rowMapper.mapRow(rs, rs.getRow());
            reviews.add(review);
        }

        return reviews;
    }
}
