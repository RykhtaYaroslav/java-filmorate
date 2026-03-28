package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class UserDbStorage implements UserStorage {
    @Override
    public User create(User user) {
        return null;
    }

    @Override
    public User update(User user) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public Collection<User> getUsers() {
        return List.of();
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Set<Friendship> getFriendships() {
        return Set.of();
    }

    @Override
    public Friendship makeFriendship(Friendship friendship) {
        return null;
    }

    @Override
    public void deleteFriendship(Friendship friendship) {

    }

    @Override
    public Optional<Friendship> findFriendship(Friendship friendship) {
        return Optional.empty();
    }
}
