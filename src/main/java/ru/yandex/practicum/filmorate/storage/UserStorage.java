package ru.yandex.practicum.filmorate.storage;

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

    Set<Friendship> getFriendships();

    Friendship makeFriendship(Friendship friendship);

    void deleteFriendship(Friendship friendship);

    Optional<Friendship> findFriendship(Friendship friendship);

}
