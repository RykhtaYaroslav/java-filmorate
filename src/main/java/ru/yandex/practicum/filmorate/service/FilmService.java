package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.repositories.film.FilmStorage;
import ru.yandex.practicum.filmorate.dto.film.FilmCreateRequest;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.FilmUpdateRequest;
import ru.yandex.practicum.filmorate.dto.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage storage;

    public FilmDto create(FilmCreateRequest filmCreateRequest) {
        log.debug("Создание фильма: {}", filmCreateRequest);

        Film film = storage.create(FilmMapper.mapToFilm(filmCreateRequest));

        log.info("Фильм создан с id = {}", film.getId());
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto update(FilmUpdateRequest filmUpdateRequest) {
        log.debug("Обновление фильма id = {}", filmUpdateRequest.getId());

        Film film = storage.update(FilmMapper.mapToFilm(filmUpdateRequest));

        log.info("Данные фильма id = {} обновлены", filmUpdateRequest.getId());
        return FilmMapper.mapToFilmDto(film);
    }

    public void delete(Long id) {
        log.debug("Удаляется фильм с id = {} ", id);

        storage.delete(id);

        log.info("Фильм с id = {} удалён", id);
    }

    public Collection<FilmDto> findAll() {
        log.info("Возвращается коллекция всех фильмов");
        Set<Film> filmSet = storage.getFilms();
        return filmSet.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toSet());
    }

    public FilmDto findById(Long id) {
        log.debug("Выполняется поиск фильма по id = {}", id);
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        Optional<Film> optionalFilm = storage.findById(id);

        if (optionalFilm.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        log.debug("Найден фильм с id = {}", id);
        return FilmMapper.mapToFilmDto(optionalFilm.get());
    }

    public FilmDto addLike(Long filmId, Long userId) {
        log.debug("Пользователь id = {} хочет поставить лайк фильму id = {}", userId, filmId);

        Film film = storage.addLike(filmId, userId);

        FilmDto dto = FilmMapper.mapToFilmDto(film);

        log.info("Пользователь id = {} поставил лайк фильму id = {}", userId, filmId);
        return dto;
    }

    public FilmDto deleteLike(Long filmId, Long userId) {
        log.debug("Пользователь id = {} хочет убрать лайк с фильма id = {}", userId, filmId);

        storage.deleteLike(filmId, userId);

        Film film = storage.findById(filmId).get();

        FilmDto dto = FilmMapper.mapToFilmDto(film);

        log.info("Пользователь id = {} убрал лайк с фильма id = {}", userId, filmId);
        return dto;
    }

    public Collection<FilmDto> getPopularFilms(int count) {
        Collection<Film> filmSet = storage.getPopularFilms(count);

        return  filmSet.stream().map(FilmMapper::mapToFilmDto).collect(Collectors.toSet());
    }
}
