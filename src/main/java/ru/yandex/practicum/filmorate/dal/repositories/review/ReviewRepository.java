package ru.yandex.practicum.filmorate.dal.repositories.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewRepository {

    Review create(Review review);

    Review update(Review review);

    void delete(Long id);

    Collection<Review> getAll(Long filmId, Long count);

    Optional<Review> getById(Long reviewId);

    void increaseUseful(Long reviewId, Long amount);

    void decreaseUseful(Long reviewId, Long amount);
}
