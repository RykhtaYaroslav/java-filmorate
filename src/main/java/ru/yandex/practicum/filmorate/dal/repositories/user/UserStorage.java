package ru.yandex.practicum.filmorate.dal.repositories.user;

import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {
    User create(User user);

    User update(User user);

    void delete(Long id);

    Collection<User> getUsers();

    Optional<User> findById(Long id);

    Set<User> getUserFriends(Long id);

    Friendship sendFriendshipRequest(Friendship friendship);

    void deleteFriendship(Friendship friendship);

    Optional<Friendship> findFriendship(Friendship friendship);

    Set<User> findCommonFriends(Long id, Long otherId);
}
