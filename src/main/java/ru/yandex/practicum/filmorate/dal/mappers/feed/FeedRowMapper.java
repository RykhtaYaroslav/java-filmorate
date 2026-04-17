package ru.yandex.practicum.filmorate.dal.mappers.feed;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FeedRowMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .eventId(rs.getLong("event_id"))
                .userId(rs.getLong("user_id"))
                .eventType(EventType.fromId(rs.getInt("event_type_id")))
                .operation(EventOperation.fromId(rs.getInt("operation_type_id")))
                .entityId(rs.getLong("entity_id"))
                .timestamp(rs.getLong("event_timestamp"))
                .build();
    }
}
