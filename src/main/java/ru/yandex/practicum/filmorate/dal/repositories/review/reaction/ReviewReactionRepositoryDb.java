package ru.yandex.practicum.filmorate.dal.repositories.review.reaction;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewReactionRepositoryDb implements ReviewReactionRepository {

    private final JdbcTemplate jdbc;

    private static final String ADD_REACTION_QUERY = """
            INSERT INTO reviews_likes (is_positive, user_id, review_id)
            VALUES (?, ?, ?)
            """;

    private static final String CHECK_REACTION_EXISTS_QUERY = """
            SELECT is_positive
            FROM reviews_likes
            WHERE review_id = ? AND user_id = ?
            LIMIT 1
            """;

    private static final String UPDATE_REACTION_QUERY = """
            UPDATE reviews_likes SET
            is_positive = COALESCE(?, is_POSITIVE)
            WHERE review_id = ? AND user_id = ?
            """;

    private static final String DELETE_REACTION_QUERY = "DELETE FROM reviews_likes WHERE review_id = ? AND user_id = ?";

    @Override
    public int addLike(Long reviewId, Long userId) {

        Boolean reaction = jdbc.query(
                CHECK_REACTION_EXISTS_QUERY,
                rs -> rs.next() ? rs.getBoolean("is_positive") : null,
                reviewId,
                userId
        );

        if (reaction == null) {
            return jdbc.update(ADD_REACTION_QUERY, true, userId, reviewId);
        } else if (!reaction) {
            return jdbc.update(UPDATE_REACTION_QUERY, true, reviewId, userId);
        }

        return 0;
    }

    @Override
    public int addDislike(Long reviewId, Long userId) {
        Boolean reaction = jdbc.query(
                CHECK_REACTION_EXISTS_QUERY,
                rs -> rs.next() ? rs.getBoolean("is_positive") : null,
                reviewId,
                userId
        );

        if (reaction == null) {
            return jdbc.update(ADD_REACTION_QUERY, false, userId, reviewId);
        } else if (reaction) {
            return jdbc.update(UPDATE_REACTION_QUERY, false, reviewId, userId);
        }

        return 0;
    }

    @Override
    public int deleteReaction(Long reviewId, Long userId) {
        return jdbc.update(DELETE_REACTION_QUERY, reviewId, userId);
    }
}
