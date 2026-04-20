package ru.yandex.practicum.filmorate.dal.repositories.like;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface LikeRepository {
    boolean addLike(Long filmId, Long userId);

    boolean deleteLike(Long filmId, Long userId);

    Map<Long, Set<Long>> getLikes();

    Set<Long> getLikes(Long filmId);

    Map<Long, Set<Long>> getLikesForFilms(Collection<Long> filmIds);
}
