package ru.yandex.practicum.filmorate.dal.repositories.director;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.repositories.BaseStorage;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

@Repository
public class DirectorRepositoryDb extends BaseStorage<Director> implements DirectorRepository {
    private static final String FIND_ALL_QUERY = """
            SELECT *
            FROM directors
            """;
    private static final String FIND_BY_ID_QUERY = """
            SELECT *
            FROM directors
            WHERE id = ?
            """;
    private static final String CREATE_QUERY = """
            INSERT INTO directors (name)
            VALUES (?)
            """;
    private static final String UPDATE_QUERY = """
            UPDATE directors
            SET name = ?
            WHERE id = ?
            """;
    private static final String DELETE_QUERY = """
            DELETE
            FROM directors
            WHERE id = ?
            """;

    public DirectorRepositoryDb(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Director create(Director director) {
        Long id = insert(CREATE_QUERY, director.getName());
        director.setId(id);
        return director;
    }

    @Override
    public Director update(Director director) {
        update(UPDATE_QUERY, director.getName(), director.getId());
        return director;
    }

    @Override
    public void delete(Long id) {
        delete(DELETE_QUERY, id);
    }

    @Override
    public Collection<Director> findAll() {
        return findMany(FIND_ALL_QUERY, mapper);
    }

    @Override
    public Optional<Director> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }
}
