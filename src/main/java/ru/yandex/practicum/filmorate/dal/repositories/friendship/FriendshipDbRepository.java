package ru.yandex.practicum.filmorate.dal.repositories.friendship;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.user.FriendshipRowMapper;
import ru.yandex.practicum.filmorate.dal.repositories.BaseRepository;
import ru.yandex.practicum.filmorate.exceptions.DataConflictException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class FriendshipDbRepository extends BaseRepository<Friendship> implements FriendshipRepository {
    private final FriendshipRowMapper friendshipRowMapper;

    private static final String SEND_FRIENDSHIP_REQUEST = """
            INSERT INTO user_friends (user_id, friend_id) VALUES(?, ?)
            """;
    private static final String DELETE_FRIENDSHIP_QUERY = """
            DELETE FROM user_friends
            WHERE user_id = ? AND friend_id = ?
            """;
    private static final String FIND_FRIENDSHIP_QUERY = """
            SELECT *
            FROM user_friends
            WHERE user_id = ? AND friend_id = ?
            """;
    private static final String GET_FRIENDS_QUERY = """
            SELECT user_id, friend_id
            FROM user_friends
            """;
    private static final String GET_FRIENDS_BY_USER_ID_QUERY = """
            SELECT friend_id
            FROM user_friends
            WHERE user_id = ?
            """;

    public FriendshipDbRepository(NamedParameterJdbcTemplate namedJdbc, RowMapper<Friendship> mapper, FriendshipRowMapper friendshipRowMapper) {
        super(namedJdbc, mapper);
        this.friendshipRowMapper = friendshipRowMapper;
    }

    @Override
    public Friendship sendFriendshipRequest(Friendship friendship) {
        try {
            update(SEND_FRIENDSHIP_REQUEST, friendship.getFromUserId(), friendship.getToUserId());

        } catch (DuplicateKeyException e) {
            throw new DataConflictException(
                    String.format("Запрос на дружбу между %d и %d уже существует",
                            friendship.getFromUserId(), friendship.getToUserId())
            );
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException(
                    String.format("Один из пользователей (ID: %d или %d) не найден в базе",
                            friendship.getFromUserId(), friendship.getToUserId())
            );
        }
        return findFriendship(friendship).orElseThrow(() ->
                new IllegalStateException("Данные сохранены, но не найдены. Этого не должно было случиться."));
    }

    @Override
    public boolean deleteFriendship(Friendship friendship) {
        return jdbc.update(DELETE_FRIENDSHIP_QUERY, friendship.getFromUserId(), friendship.getToUserId()) > 0;
    }

    @Override
    public Optional<Friendship> findFriendship(Friendship friendship) {
        try {
            Long userId = friendship.getFromUserId();
            Long friendId = friendship.getToUserId();
            Friendship result = jdbc.queryForObject(FIND_FRIENDSHIP_QUERY, friendshipRowMapper,
                    userId, friendId);
            return Optional.ofNullable(result);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Map<Long, Set<Long>> getFriends() {
        return jdbc.query(GET_FRIENDS_QUERY, rs -> {
            Map<Long, Set<Long>> friends = new HashMap<>();
            while (rs.next()) {
                long userId = rs.getLong("user_id");
                long friendId = rs.getLong("friend_id");
                friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
            }
            return friends;
        });
    }

    @Override
    public Set<Long> getFriends(Long userId) {
        return new HashSet<>(jdbc.query(GET_FRIENDS_BY_USER_ID_QUERY, (rs, rowNum) -> rs.getLong("friend_id"), userId));
    }
}
