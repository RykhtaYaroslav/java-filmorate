package ru.yandex.practicum.filmorate.dal.repositories.feed;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.repositories.BaseStorage;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.Collection;

@Repository
public class FeedRepositoryDb extends BaseStorage<Event> implements FeedRepository {
    public FeedRepositoryDb(JdbcTemplate jdbc, RowMapper<Event> mapper) {
        super(jdbc, mapper);
    }

    private static final String INSERT_EVENT = """
            INSERT INTO events (user_id, event_type_id, operation_type_id, entity_id, event_timestamp)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String FIND_USER_FRIENDS_EVENTS = """
            SELECT *
            FROM events e
            WHERE e.user_id = ?
            ORDER BY e.event_timestamp ASC, e.event_id ASC;
            """;

    @Override
    public void addEvent(Event event) {
        insert(INSERT_EVENT,
                event.getUserId(),
                event.getEventType().getId(),
                event.getOperation().getId(),
                event.getEntityId(),
                event.getTimestamp());
    }

    @Override
    public Collection<Event> getEventsByUserId(Long id) {
        return findMany(FIND_USER_FRIENDS_EVENTS, mapper, id);
    }
}
