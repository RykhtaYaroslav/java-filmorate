package ru.yandex.practicum.filmorate.dal.repositories;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.DataConflictException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    private static final String DELETE_USERS = "DELETE FROM users WHERE id = ?";

    private static final String FIND_USER_BY_ID_QUERY = """
            SELECT *
            FROM users
            WHERE id = ?
            """;

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper, ResultSetExtractor<Set<User>> extractor) {
        super(jdbc, mapper);
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
        delete(DELETE_USERS, id);
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
