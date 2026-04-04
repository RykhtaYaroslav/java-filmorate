package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.repositories.MpaDbStorage;
import ru.yandex.practicum.filmorate.dto.mappers.MpaMapper;
import ru.yandex.practicum.filmorate.dto.mpa.MpaRatingDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.enums.MpaRating;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaDbStorage storage;

    public List<MpaRatingDto> findAllMpa() {
        List<MpaRating> ratingList = storage.findAllMpa();

        return ratingList.stream().map(MpaMapper::mapToMpaDto).toList();
    }

    public MpaRatingDto findById(int id) {
        MpaRating rating = storage.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Рейтинг с id %d не найден", id)));

        return MpaMapper.mapToMpaDto(rating);
    }
}