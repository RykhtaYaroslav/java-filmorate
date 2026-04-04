package ru.yandex.practicum.filmorate.dal.repositories;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exceptions.DatabaseException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BaseStorage<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;

    public BaseStorage(JdbcTemplate jdbc, RowMapper<T> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    protected Optional<T> findOne(String query, Object... params) {
        try {
            T result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    protected List<T> findMany(String query, ResultSetExtractor<List<T>> rs) {
        return jdbc.query(query, rs);
    }

    protected Set<T> findMany(String query, ResultSetExtractor<Set<T>> rs, Object... params) {
        return jdbc.query(query, rs, params);
    }

    protected void delete(String query, long id) {
        int rowsDeleted = jdbc.update(query, id);
        if (rowsDeleted == 0) {
            throw new NotFoundException("Сущность с id " + id + " не найдена, удаление невозможно");
        }
    }

    protected void delete(String query, Object... params) {
        int rowsDeleted = jdbc.update(query, params);
        if (rowsDeleted == 0) {
            throw new NotFoundException("Пользователи не существуют или не являются друзьями");
        }
    }

    protected void update(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new NotFoundException("Сущность для обновления не найдена или данные не изменились");
        }
    }

    protected long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        if (id != null) {
            return id;
        } else {
            throw new DatabaseException("Не удалось сохранить данные: ID не был сгенерирован");
        }
    }
}