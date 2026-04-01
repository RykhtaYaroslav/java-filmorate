package ru.yandex.practicum.filmorate.dal.mappers.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserExtractor implements ResultSetExtractor<Set<User>> {
    private final UserRowMapper userRowMapper;

    @Override
    public Set<User> extractData(ResultSet rs) throws SQLException, DataAccessException {
        final Set<User> users = new HashSet<>();

        while (rs.next()) {
            User user = userRowMapper.mapRow(rs, rs.getRow());
            users.add(user);
        }
        return users;
    }
}