package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.repositories.review.ReviewRepositoryDb;
import ru.yandex.practicum.filmorate.dal.repositories.review.reaction.ReviewReactionRepositoryDb;
import ru.yandex.practicum.filmorate.dto.mappers.ReviewMapper;
import ru.yandex.practicum.filmorate.dto.review.ReviewCreateRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewUpdateRequest;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepositoryDb reviewRepository;
    private final ReviewReactionRepositoryDb reviewReactionRepository;
    private final UserService userService;
    private final FilmService filmService;

    public ReviewDto create(ReviewCreateRequest request) {

        userService.findById(request.getUserId());
        filmService.findById(request.getFilmId());

        Review created = reviewRepository.create(ReviewMapper.mapToReview(request));

        return findById(created.getId());
    }

    public ReviewDto update(ReviewUpdateRequest request) {

        Review updated = reviewRepository.update(ReviewMapper.mapToReview(request));

        return findById(updated.getId());

    }

    public void delete(Long id) {
        reviewRepository.delete(id);
    }

    public Collection<ReviewDto> getAll(Long filmId, Long count) {
        return reviewRepository.getAll(filmId, count).stream()
                .map(ReviewMapper::mapToReviewDto)
                .toList();
    }

    public ReviewDto findById(Long id) {
        if (id == null) {
            throw new ConditionsNotMetException("Id не может быть null");
        }

        Review review = reviewRepository.getById(id).orElseThrow(
                () -> new NotFoundException("Отзыв с id=" + id + " не найден")
        );

        return ReviewMapper.mapToReviewDto(review);
    }

    public void addLike(Long reviewId, Long userId) {
        int updatedRows = reviewReactionRepository.addLike(reviewId, userId);

        if (updatedRows != 0) {
            reviewRepository.increaseUseful(reviewId, (long) updatedRows);
        }
    }

    public void addDislike(Long reviewId, Long userId) {
        int updatedRows = reviewReactionRepository.addDislike(reviewId, userId);

        if (updatedRows != 0) {
            reviewRepository.decreaseUseful(reviewId, (long) updatedRows);
        }
    }

    public void removeReaction(Long reviewId, Long userId) {
        int deletedRows = reviewReactionRepository.deleteReaction(reviewId, userId);

        if (deletedRows != 0) {
            reviewRepository.decreaseUseful(reviewId, (long) deletedRows);
        }
    }
}
