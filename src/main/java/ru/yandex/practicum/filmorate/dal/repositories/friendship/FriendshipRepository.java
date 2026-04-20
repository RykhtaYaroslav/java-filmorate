package ru.yandex.practicum.filmorate.dal.repositories.friendship;

import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface FriendshipRepository {
    Friendship sendFriendshipRequest(Friendship friendship);

    /** @return {@code true}, если строка дружбы была удалена */
    boolean deleteFriendship(Friendship friendship);

    Optional<Friendship> findFriendship(Friendship friendship);

    Map<Long, Set<Long>> getFriends();

    Set<Long> getFriends(Long userId);
}
