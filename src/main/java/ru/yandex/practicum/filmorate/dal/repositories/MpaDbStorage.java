package ru.yandex.practicum.filmorate.dal.repositories;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.enums.MpaRating;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaDbStorage extends BaseStorage<MpaRating> {
    private final ResultSetExtractor<List<MpaRating>> extractor;

    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa_ratings ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa_ratings WHERE id = ?";

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<MpaRating> mapper, ResultSetExtractor<List<MpaRating>> extractor) {
        super(jdbc, mapper);
        this.extractor = extractor;
    }

    public List<MpaRating> findAllMpa() {
        return findMany(FIND_ALL_QUERY, extractor);
    }

    public Optional<MpaRating> findById(int id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }
}