package ru.yandex.practicum.filmorate.dal.repositories.review;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.repositories.BaseStorage;
import ru.yandex.practicum.filmorate.dto.mappers.ReviewMapper;
import ru.yandex.practicum.filmorate.dto.review.ReviewCreateRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewUpdateRequest;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.*;

@Repository
public class ReviewRepositoryDb extends BaseStorage<Review> implements ReviewRepository {

    private static final String CREATE_REVIEW_QUERY = """
            INSERT INTO reviews(content, is_positive, user_id, film_id, useful)
            VALUES (?, ?, ?, ?, ?);
            """;

    private static final String UPDATE_REVIEW_QUERY = """
            UPDATE reviews SET
            content = COALESCE(?, content),
            is_positive = COALESCE(?, is_positive),
            user_id = COALESCE(?, user_id),
            film_id = COALESCE(?, film_id)
            WHERE id = ?
            """;

    private static final String DELETE_REVIEW_QUERY = "DELETE FROM reviews WHERE id = ?";

    private static final String FIND_ALL_QUERY = "SELECT * FROM reviews";

    private static final String FIND_BY_ID_QUERY = "SELECT * FROM reviews WHERE id = ? ORDER BY useful DESC";

    private static final String ADD_REACTION_QUERY = "UPDATE reviews SET useful = useful + ? WHERE id = ?";

    private static final String DECREASE_REACTION_QUERY = "UPDATE reviews SET useful = useful - ? WHERE id = ?";

    public ReviewRepositoryDb(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Review create(Review review) {
        long id = insert(
                CREATE_REVIEW_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getUseful()
        );
        review.setId(id);

        return review;
    }

    @Override
    public Review update(Review review) {
        update(
            UPDATE_REVIEW_QUERY,
            review.getContent(),
            review.getIsPositive(),
            review.getUserId(),
            review.getFilmId(),
            review.getId()
        );
        return review;
    }

    @Override
    public void delete(Long id) {
        delete(DELETE_REVIEW_QUERY, id);
    }

    @Override
    public Collection<Review> getAll(Long filmId, Long count) {
        List<Object> params = new LinkedList<>();
        StringBuilder query = new StringBuilder(FIND_ALL_QUERY);
        if (filmId != null) {
            query.append(" WHERE film_id = ?");
            params.add(filmId);
        }

        query.append(" ORDER BY useful");

        if (count != null) {
            query.append(" LIMIT ?");
            params.add(count);
        }

        return jdbc.query(query.toString(), mapper, params.toArray());
    }

    @Override
    public Optional<Review> getById(Long reviewId) {
        return findOne(FIND_BY_ID_QUERY, reviewId);
    }

    @Override
    public void increaseUseful(Long reviewId, Long amount) {
        jdbc.update(ADD_REACTION_QUERY, amount, reviewId);
    }

    @Override
    public void decreaseUseful(Long reviewId, Long amount) {
        jdbc.update(DECREASE_REACTION_QUERY, amount, reviewId);
    }
}
