package ru.yandex.practicum.filmorate.dal.repositories.like;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface LikeRepository {
    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    Map<Long, Set<Long>> getLikes();

    Set<Long> getLikes(Long filmId);

    Map<Long, Set<Long>> getLikesForFilms(Collection<Long> filmIds);
}
