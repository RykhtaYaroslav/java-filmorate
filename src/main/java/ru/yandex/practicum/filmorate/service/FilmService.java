package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.repositories.FilmStorage;
import ru.yandex.practicum.filmorate.dto.film.FilmCreateRequest;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.FilmUpdateRequest;
import ru.yandex.practicum.filmorate.dto.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.LikeException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage storage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage storage, UserService userService) {
        this.storage = storage;
        this.userService = userService;
    }

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

    public Film addLike(Long filmId, Long userId) {
        log.debug("Пользователь id = {} хочет поставить лайк фильму id = {}", userId, filmId);
        userService.findById(userId); //throws exception when wrong id
        Film film = findById(filmId);
        if (film.getLikes().contains(userId)) {
            throw new LikeException(String.format("Пользователь с id = %d уже поставил лайк фильму с id = %d", userId, filmId));
        }
        film.getLikes().add(userId);
        log.info("Пользователь id = {} поставил лайк фильму id = {}", userId, filmId);
        return film;
    }

    public Film deleteLike(Long filmId, Long userId) {
        log.debug("Пользователь id = {} хочет убрать лайк с фильма id = {}", userId, filmId);
        userService.findById(userId);
        Film film = findById(filmId);
        if (!film.getLikes().contains(userId)) {
            throw new LikeException(String.format("Пользователь с id = %d не ставил лайк фильму с id = %d", userId, filmId));
        }
        film.getLikes().remove(userId);
        log.info("Пользователь id = {} убрал лайк с фильма id = {}", userId, filmId);
        return film;
    }

    public Collection<Film> getPopularFilms(int count) {
        return storage.getFilms()
                .stream()
                .sorted(Comparator.comparing((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }
}
