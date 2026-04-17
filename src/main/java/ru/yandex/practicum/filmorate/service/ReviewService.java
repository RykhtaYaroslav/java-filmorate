package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepositoryDb reviewRepository;
    private final ReviewReactionRepositoryDb reviewReactionRepository;
    private final UserService userService;
    private final FilmService filmService;
    private final FeedService feedService;

    public ReviewDto create(ReviewCreateRequest request) {
        log.debug("Запрос на создание отзыва: {}", request);

        userService.findById(request.getUserId());
        filmService.findById(request.getFilmId());

        Review created = reviewRepository.create(request);
        log.info("Отзыв для фильма id={} от пользователя id={} успешно создан с id={}",
                request.getFilmId(), request.getUserId(), created.getId());
        feedService.addEvent(request.getUserId(), EventType.REVIEW, EventOperation.ADD, created.getId());
        return findById(created.getId());
    }

    public ReviewDto update(ReviewUpdateRequest request) {
        log.debug("Запрос на обновление отзыва id={}", request.getReviewId());

        Review updated = reviewRepository.update(request);
        log.info("Отзыв id={} успешно обновлен", updated.getId());
        feedService.addEvent(request.getUserId(), EventType.REVIEW, EventOperation.UPDATE, updated.getId());
        return findById(updated.getId());
    }

    public void delete(Long id) {
        log.debug("Запрос на удаление отзыва id={}", id);
        ReviewDto rew = findById(id);
        reviewRepository.delete(id);
        log.info("Отзыв id={} успешно удален", id);
        feedService.addEvent(rew.getUserId(), EventType.REVIEW, EventOperation.REMOVE, id);
    }

    public Collection<ReviewDto> getAll(Long filmId, Long count) {
        log.debug("Запрос на получение {} отзывов для фильма id={}", count, filmId == null ? "всех" : filmId);
        Collection<ReviewDto> reviews = reviewRepository.getAll(filmId, count).stream()
                .map(ReviewMapper::mapToReviewDto)
                .toList();
        log.info("Возвращено {} отзывов", reviews.size());
        return reviews;
    }

    public ReviewDto findById(Long id) {
        log.debug("Запрос на поиск отзыва по id={}", id);
        if (id == null) {
            throw new ConditionsNotMetException("Id не может быть null");
        }

        Review review = reviewRepository.getById(id).orElseThrow(
                () -> {
                    log.warn("Отзыв с id={} не найден", id);
                    return new NotFoundException("Отзыв с id=" + id + " не найден");
                }
        );
        log.debug("Найден отзыв: {}", review);
        return ReviewMapper.mapToReviewDto(review);
    }

    public void addLike(Long reviewId, Long userId) {
        log.debug("Запрос от пользователя id={} на добавление лайка отзыву id={}", userId, reviewId);
        int updatedRows = reviewReactionRepository.addLike(reviewId, userId);

        if (updatedRows != 0) {
            reviewRepository.increaseUseful(reviewId, (long) updatedRows);
            log.info("Пользователь id={} успешно поставил лайк отзыву id={}", userId, reviewId);
        } else {
            log.warn("Не удалось поставить лайк отзыву id={} от пользователя id={}. Возможно, лайк уже существует или данные некорректны.", reviewId, userId);
        }
        feedService.addEvent(userId, EventType.LIKE, EventOperation.ADD, reviewId);
    }

    public void addDislike(Long reviewId, Long userId) {
        log.debug("Запрос от пользователя id={} на добавление дизлайка отзыву id={}", userId, reviewId);
        int updatedRows = reviewReactionRepository.addDislike(reviewId, userId);

        if (updatedRows != 0) {
            reviewRepository.decreaseUseful(reviewId, (long) updatedRows);
            log.info("Пользователь id={} успешно поставил дизлайк отзыву id={}", userId, reviewId);
        } else {
            log.warn("Не удалось поставить дизлайк отзыву id={} от пользователя id={}. Возможно, дизлайк уже существует или данные некорректны.", reviewId, userId);
        }
        feedService.addEvent(userId, EventType.LIKE, EventOperation.ADD, reviewId);
    }

    public void removeReaction(Long reviewId, Long userId) {
        log.debug("Запрос от пользователя id={} на удаление реакции с отзыва id={}", userId, reviewId);
        int deletedRows = reviewReactionRepository.deleteReaction(reviewId, userId);

        if (deletedRows != 0) {
            reviewRepository.decreaseUseful(reviewId, (long) deletedRows);
            log.info("Пользователь id={} успешно удалил реакцию с отзыва id={}", userId, reviewId);
        } else {
            log.warn("Не удалось удалить реакцию с отзыва id={} от пользователя id={}. Возможно, реакции не было или данные некорректны.", reviewId, userId);
        }
        feedService.addEvent(userId, EventType.LIKE, EventOperation.REMOVE, reviewId);
    }
}
