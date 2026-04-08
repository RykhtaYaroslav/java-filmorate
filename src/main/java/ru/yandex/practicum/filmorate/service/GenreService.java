package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.repositories.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.dto.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.enums.Genre;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage storage;

    public List<GenreDto> findAll() {
        List<Genre> genreList = storage.findAll();

        return genreList.stream().map(GenreMapper::mapToDto).toList();
    }

    public GenreDto findById(int id) {
        Genre genre = storage.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Жанр с id %d не найден", id)));

        return GenreMapper.mapToDto(genre);
    }
}
