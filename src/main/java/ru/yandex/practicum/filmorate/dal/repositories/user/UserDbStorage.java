package ru.yandex.practicum.filmorate.dal.repositories.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.repositories.BaseStorage;
import ru.yandex.practicum.filmorate.exceptions.DataConflictException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserDbStorage extends BaseStorage<User> implements UserStorage {
    private final ResultSetExtractor<Set<User>> extractor;

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

    private static final String DELETE_USERS_QUERY = """
            DELETE
            FROM users
            WHERE id = ?
            """;

    private static final String FIND_USER_BY_ID_QUERY = """
            SELECT *
            FROM users
            WHERE id = ?
            """;

    private static final String FIND_USER_FRIENDS_QUERY = """
            SELECT u.*
            FROM users u
            JOIN user_friends f ON u.id = f.friend_id
            WHERE f.user_id = ?
            """;

    public UserDbStorage(NamedParameterJdbcTemplate namedJdbc, RowMapper<User> mapper, ResultSetExtractor<Set<User>> extractor) {
        super(namedJdbc, mapper);
        this.extractor = extractor;
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
        return findMany(FIND_USER_FRIENDS_QUERY, extractor, id);
    }

    @Override
    public Friendship sendFriendshipRequest(Friendship friendship) {
        throw new UnsupportedOperationException("Этот метод должен быть реализован в сервисном слое");
    }

    @Override
    public void deleteFriendship(Friendship friendship) {
        throw new UnsupportedOperationException("Этот метод должен быть реализован в сервисном слое");
    }

    @Override
    public Optional<Friendship> findFriendship(Friendship friendship) {
        throw new UnsupportedOperationException("Этот метод должен быть реализован в сервисном слое");
    }

    @Override
    public Set<User> findCommonFriends(Long id, Long otherId) {
        Set<User> idFriends = findMany(FIND_USER_FRIENDS_QUERY, extractor, id);
        Set<User> otherIdFriends = findMany(FIND_USER_FRIENDS_QUERY, extractor, otherId);

        return idFriends.stream()
                .filter(otherIdFriends::contains)
                .sorted(Comparator.comparing(User::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
