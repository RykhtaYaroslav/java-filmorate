package ru.yandex.practicum.filmorate.dal.repositories;

import org.springframework.beans.BeanUtils;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

/**
 * @deprecated use UserDbStorage instead
 */
@Deprecated(since = "db-migration", forRemoval = true)
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<Friendship> friendships = new HashSet<>();

    @Override
    public User create(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User updUser) {
        User oldUser = users.get(updUser.getId());
        BeanUtils.copyProperties(updUser, oldUser, "id");
        return oldUser;
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Set<User> getUserFriends(Long id) {
        return null;
    }

    @Override
    public Friendship sendFriendRequest(Friendship friendship) {
        friendships.add(friendship);
        return friendship;
    }

    @Override
    public void deleteFriendship(Friendship friendship) {
        friendships.remove(friendship);
    }

    @Override
    public Optional<Friendship> findFriendship(Friendship friendship) {
        return friendships.stream()
                .filter(f -> f.equals(friendship))
                .findFirst();
    }

    @Override
    public Friendship confirmFriendship(Friendship friendship) {
        return null;
    }

    @Override
    public Set<User> findCommonFriends(Long id, Long otherId) {
        return Set.of();
    }
}
