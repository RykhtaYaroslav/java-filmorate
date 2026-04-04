package ru.yandex.practicum.filmorate.dal.mappers.user;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FriendshipRowMapper implements RowMapper<Friendship> {
    @Override
    public Friendship mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long userId = rs.getLong("user_id");
        Long friendId = rs.getLong("friend_id");

        return new Friendship(userId, friendId);
    }
}