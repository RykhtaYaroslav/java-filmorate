package ru.yandex.practicum.filmorate.dal.repositories;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.user.FriendshipRowMapper;
import ru.yandex.practicum.filmorate.exceptions.DataConflictException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.FriendshipStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserDbStorage extends BaseStorage<User> implements UserStorage {
    private final ResultSetExtractor<Set<User>> extractor;
    private final FriendshipRowMapper friendshipRowMapper;

    private static final String CREATE_USER_QUERY = """
            INSERT INTO users (email, login, name, birthday)
            VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE_FIELDS_QUERY = """
            UPDATE users SET
            email = COALESCE(?, email),
            login = COALESCE(?, login),
            name = COALESCE(?, name),
            birthday = COALESCE(?, birthday)
            WHERE id = ?
            """;

    private static final String FIND_ALL_USERS_QUERY = """
            SELECT *
            FROM users
            ORDER BY id
            """;

    private static final String DELETE_USERS_QUERY = "DELETE FROM users WHERE id = ?";

    private static final String FIND_USER_BY_ID_QUERY = """
            SELECT *
            FROM users
            WHERE id = ?
            """;

    private static final String SEND_FRIENDSHIP_REQUEST = """
            INSERT INTO user_friends (user_id, friend_id)
            VALUES(?, ?)""";

    private static final String DELETE_FRIENDSHIP_QUERY = """
            DELETE FROM user_friends
            WHERE (user_id = ? AND friend_id = ?)
               OR (user_id = ? AND friend_id = ?)
            """;

    private static final String UPDATE_FRIENDSHIP_STATUS_QUERY = """
            UPDATE user_friends
            SET status = 'CONFIRMED'
            WHERE (user_id = ? AND friend_id = ?)
               OR (user_id = ? AND friend_id = ?)
            """;

    private static final String FIND_FRIENDSHIP_QUERY = """
            SELECT *
            FROM user_friends
            WHERE (user_id = ? AND friend_id = ?)
               OR (user_id = ? AND friend_id = ?)
            """;

    private static final String FIND_USER_FRIENDS_QUERY = """
            SELECT u.*
            FROM users u
            WHERE u.id IN (
                SELECT friend_id FROM user_friends WHERE user_id = ?
                UNION
                SELECT user_id FROM user_friends WHERE friend_id = ?
            )
            """;

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper, ResultSetExtractor<Set<User>> extractor, FriendshipRowMapper friendshipRowMapper) {
        super(jdbc, mapper);
        this.extractor = extractor;
        this.friendshipRowMapper = friendshipRowMapper;
    }

    @Override
    public User create(User user) {
        try {
            String email = user.getEmail();
            String login = user.getLogin();
            String name = user.getName() != null ? user.getName() : login;
            LocalDate birthday = user.getBirthday();

            Long id = insert(CREATE_USER_QUERY, email, login, name, birthday);

            user.setId(id);
            return user;
        } catch (DataIntegrityViolationException e) {
            throw new DataConflictException("Пользователь с почтой " + user.getEmail() + " уже существует");
        }
    }

    @Override
    public User update(User user) {
        try {
            String email = user.getEmail();
            String login = user.getLogin();
            String name = user.getName();
            LocalDate birthday = user.getBirthday();
            Long id = user.getId();

            update(UPDATE_FIELDS_QUERY, email, login, name, birthday, id);
            return findById(user.getId()).orElseThrow(() ->
                    new IllegalStateException("Данные обновлены, но не найдены. Этого не должно было случиться."));
        } catch (DataIntegrityViolationException e) {
            throw new DataConflictException("Пользователь с почтой " + user.getEmail() + " уже существует");
        }
    }

    @Override
    public void delete(Long id) {
        delete(DELETE_USERS_QUERY, id);
    }

    @Override
    public Collection<User> getUsers() {
        return findMany(FIND_ALL_USERS_QUERY, extractor);
    }

    @Override
    public Optional<User> findById(Long id) {
        return findOne(FIND_USER_BY_ID_QUERY, id);
    }

    @Override
    public Set<User> getUserFriends(Long id) {
        return findMany(FIND_USER_FRIENDS_QUERY, extractor, id, id);
    }

    @Override
    public Friendship sendFriendRequest(Friendship friendship) {
        try {
            jdbc.update(SEND_FRIENDSHIP_REQUEST, friendship.getFromUserId(), friendship.getToUserId());

        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Один из пользователей не найден");
        }
        return findFriendship(friendship).orElseThrow(() ->
                new IllegalStateException("Данные сохранены, но не найдены. Этого не должно было случиться."));
    }

    @Override
    public void deleteFriendship(Friendship friendship) {
        Long userId = friendship.getFromUserId();
        Long friendId = friendship.getToUserId();
        delete(DELETE_FRIENDSHIP_QUERY, userId, friendId, friendId, userId);
    }

    @Override
    public Optional<Friendship> findFriendship(Friendship friendship) {
        try {
            Long userId = friendship.getFromUserId();
            Long friendId = friendship.getToUserId();
            Friendship result = jdbc.queryForObject(FIND_FRIENDSHIP_QUERY, friendshipRowMapper,
                    userId, friendId, friendId, userId);
            return Optional.ofNullable(result);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Friendship confirmFriendship(Friendship friendship) {
        Long userId = friendship.getFromUserId();
        Long friendId = friendship.getToUserId();
        update(UPDATE_FRIENDSHIP_STATUS_QUERY, userId, friendId, friendId, userId);
        friendship.setStatus(FriendshipStatus.CONFIRMED);
        return findFriendship(friendship).orElseThrow(() ->
                new IllegalStateException("Данные обновлены, но не найдены. Этого не должно было случиться."));
    }

    @Override
    public Set<User> findCommonFriends(Long id, Long otherId) {
        Set<User> idFriends = findMany(FIND_USER_FRIENDS_QUERY, extractor, id, id);
        Set<User> otherIdFriends = findMany(FIND_USER_FRIENDS_QUERY, extractor, otherId, otherId);

        return idFriends.stream().filter(otherIdFriends::contains).collect(Collectors.toSet());
    }
}