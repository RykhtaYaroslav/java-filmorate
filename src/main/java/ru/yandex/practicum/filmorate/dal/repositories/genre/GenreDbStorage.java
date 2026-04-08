package ru.yandex.practicum.filmorate.dal.repositories.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.genre.GenreExtractor;
import ru.yandex.practicum.filmorate.dal.repositories.BaseStorage;
import ru.yandex.practicum.filmorate.model.enums.Genre;

import java.util.List;
import java.util.Optional;

@Repository

public class GenreDbStorage extends BaseStorage<Genre> {
    private final GenreExtractor extractor;

    private static final String FIND_ALL_QUERY = "SELECT * FROM genres ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper, GenreExtractor extractor) {
        super(jdbc, mapper);
        this.extractor = extractor;
    }

    public List<Genre> findAll() {
        return findMany(FIND_ALL_QUERY, extractor);
    }

    public Optional<Genre> findById(int id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }
}