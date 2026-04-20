package ru.yandex.practicum.filmorate.dal.mappers.like;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Like;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Класс-заглушка для маппинга строк из ResultSet в объекты типа Like.
 * <p>
 * В текущей реализации не используется и возвращает {@code null}.
 * Необходим для корректной работы {@link ru.yandex.practicum.filmorate.dal.repositories.like.LikeRepository}.
 * </p>
 */
@Component
public class LikeRowMapper implements RowMapper<Like> {
    @Override
    public Like mapRow(ResultSet rs, int rowNum) throws SQLException {
        return null;
    }
}
