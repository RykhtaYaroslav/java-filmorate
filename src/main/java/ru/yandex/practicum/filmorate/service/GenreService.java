package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.repositories.genre.FilmGenreRepository;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.dto.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.enums.Genre;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {
    private final FilmGenreRepository filmGenreRepository;

    public List<GenreDto> findAll() {
        log.debug("Запрос на получение всех жанров");
        List<Genre> genreList = filmGenreRepository.findAll();
        log.info("Возвращено {} жанров", genreList.size());
        return genreList.stream().map(GenreMapper::mapToDto).toList();
    }

    public GenreDto findById(int id) {
        log.debug("Запрос на поиск жанра по id={}", id);
        Genre genre = filmGenreRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Жанр с id={} не найден", id);
                    return new NotFoundException(String.format("Жанр с id %d не найден", id));
                });
        log.debug("Найден жанр: {}", genre);
        return GenreMapper.mapToDto(genre);
    }
}
