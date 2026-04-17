package ru.yandex.practicum.filmorate.dal.repositories.review.reaction;

public interface ReviewReactionRepository {

    int addLike(Long reviewId, Long userId);

    int addDislike(Long reviewId, Long userId);

    int deleteReaction(Long reviewId, Long userId);
}
