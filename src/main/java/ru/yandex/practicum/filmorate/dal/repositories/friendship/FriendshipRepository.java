package ru.yandex.practicum.filmorate.dal.repositories.friendship;

import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface FriendshipRepository {
    Friendship sendFriendshipRequest(Friendship friendship);

    void deleteFriendship(Friendship friendship);

    Optional<Friendship> findFriendship(Friendship friendship);

    Map<Long, Set<Long>> getFriends();

    Collection<Long> getFriends(Long userId);
}