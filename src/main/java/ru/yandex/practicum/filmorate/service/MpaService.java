package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.repositories.mpa.MpaDbRepository;
import ru.yandex.practicum.filmorate.dto.mappers.MpaMapper;
import ru.yandex.practicum.filmorate.dto.mpa.MpaRatingDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.enums.MpaRating;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MpaService {
    private final MpaDbRepository storage;

    public List<MpaRatingDto> findAllMpa() {
        log.debug("Запрос на получение всех MPA рейтингов");
        List<MpaRating> ratingList = storage.findAllMpa();
        log.info("Возвращено {} MPA рейтингов", ratingList.size());
        return ratingList.stream().map(MpaMapper::mapToMpaDto).toList();
    }

    public MpaRatingDto findById(int id) {
        log.debug("Запрос на поиск MPA рейтинга по id={}", id);
        MpaRating rating = storage.findById(id)
                .orElseThrow(() -> {
                    log.warn("MPA рейтинг с id={} не найден", id);
                    return new NotFoundException(String.format("Рейтинг с id %d не найден", id));
                });
        log.debug("Найден MPA рейтинг: {}", rating);
        return MpaMapper.mapToMpaDto(rating);
    }
}
