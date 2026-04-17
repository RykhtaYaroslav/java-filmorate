package ru.yandex.practicum.filmorate.dto.mappers;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.dto.review.ReviewCreateRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewUpdateRequest;
import ru.yandex.practicum.filmorate.model.Review;

@UtilityClass
public class ReviewMapper {

    public Review mapToReview(ReviewCreateRequest request) {
        Review review = new Review();

        review.setContent(request.getContent());
        review.setIsPositive(request.getIsPositive());
        review.setUserId(request.getUserId());
        review.setFilmId(request.getFilmId());
        review.setUseful(0L);

        return review;
    }

    public Review mapToReview(ReviewUpdateRequest request) {
        Review review = new Review();

        review.setId(request.getReviewId());
        review.setContent(request.getContent());
        review.setIsPositive(request.getIsPositive());
        review.setUserId(request.getUserId());
        review.setFilmId(request.getFilmId());
        review.setUseful(request.getUseful());

        return review;
    }

    public ReviewDto mapToReviewDto(Review review) {
        ReviewDto dto = new ReviewDto();

        dto.setReviewId(review.getId());
        dto.setContent(review.getContent());
        dto.setIsPositive(review.getIsPositive());
        dto.setUserId(review.getUserId());
        dto.setFilmId(review.getFilmId());
        dto.setUseful(review.getUseful());

        return dto;
    }
}
