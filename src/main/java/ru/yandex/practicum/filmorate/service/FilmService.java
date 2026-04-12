package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.repositories.film.FilmStorage;
import ru.yandex.practicum.filmorate.dal.repositories.genre.FilmGenreRepository;
import ru.yandex.practicum.filmorate.dal.repositories.like.LikeRepository;
import ru.yandex.practicum.filmorate.dto.film.FilmCreateRequest;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.FilmUpdateRequest;
import ru.yandex.practicum.filmorate.dto.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enums.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final FilmGenreRepository filmGenreRepository;
    private final LikeRepository likeRepository;

    private static final int MPA_RATINGS_AMOUNT = 5;
    private static final int GENRES_AMOUNT = 6;

    public FilmDto create(FilmCreateRequest filmCreateRequest) {
        log.debug("Создание фильма: {}", filmCreateRequest);
        validateFilm(filmCreateRequest);

        Film film = filmStorage.create(FilmMapper.mapToFilm(filmCreateRequest));

        log.info("Фильм создан с id = {}", film.getId());
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto update(FilmUpdateRequest filmUpdateRequest) {
        log.debug("Обновление фильма id = {}", filmUpdateRequest.getId());

        Film film = filmStorage.update(FilmMapper.mapToFilm(filmUpdateRequest));

        log.info("Данные фильма id = {} обновлены", filmUpdateRequest.getId());
        return FilmMapper.mapToFilmDto(film);
    }

    public void delete(Long id) {
        log.debug("Удаляется фильм с id = {} ", id);

        filmStorage.delete(id);

        log.info("Фильм с id = {} удалён", id);
    }

    public Collection<FilmDto> findAll() {
        log.info("Возвращается коллекция всех фильмов");
        Set<Film> filmSet = filmStorage.getFilms();
        Map<Long, Set<Genre>> genres = filmGenreRepository.getGenres();
        Map<Long, Set<Long>> likes = likeRepository.getLikes();

        filmSet.forEach(film -> {
            film.setGenres(genres.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setUserLikeIds(likes.getOrDefault(film.getId(), new HashSet<>()));
        });

        return filmSet.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public FilmDto findById(Long id) {
        log.debug("Выполняется поиск фильма по id = {}", id);
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        Optional<Film> optionalFilm = filmStorage.findById(id);

        if (optionalFilm.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        Film film = optionalFilm.get();
        film.setGenres(filmGenreRepository.getGenres(id));
        film.setUserLikeIds(likeRepository.getLikes(id));

        log.debug("Найден фильм с id = {}", id);
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto addLike(Long filmId, Long userId) {
        log.debug("Пользователь id = {} хочет поставить лайк фильму id = {}", userId, filmId);

        likeRepository.addLike(filmId, userId);
        Film film = filmStorage.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
        film.setUserLikeIds(likeRepository.getLikes(filmId));
        film.setGenres(filmGenreRepository.getGenres(filmId));

        FilmDto dto = FilmMapper.mapToFilmDto(film);

        log.info("Пользователь id = {} поставил лайк фильму id = {}", userId, filmId);
        return dto;
    }

    public FilmDto deleteLike(Long filmId, Long userId) {
        log.debug("Пользователь id = {} хочет убрать лайк с фильма id = {}", userId, filmId);

        likeRepository.deleteLike(filmId, userId);

        Film film = filmStorage.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
        film.setUserLikeIds(likeRepository.getLikes(filmId));
        film.setGenres(filmGenreRepository.getGenres(filmId));

        FilmDto dto = FilmMapper.mapToFilmDto(film);

        log.info("Пользователь id = {} убрал лайк с фильма id = {}", userId, filmId);
        return dto;
    }

    public Collection<FilmDto> getPopularFilms(int count) {
        Collection<Film> filmSet = filmStorage.getPopularFilms(count);
        Map<Long, Set<Genre>> genres = filmGenreRepository.getGenres();
        Map<Long, Set<Long>> likes = likeRepository.getLikes();

        filmSet.forEach(film -> {
            film.setGenres(genres.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setUserLikeIds(likes.getOrDefault(film.getId(), new HashSet<>()));
        });

        return filmSet.stream().map(FilmMapper::mapToFilmDto).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void validateFilm(FilmCreateRequest filmCreateRequest) {
        int mpaId = filmCreateRequest.getMpa().getId();

        if (mpaId > MPA_RATINGS_AMOUNT || mpaId < 1) {
            throw new NotFoundException(String.format("Возрастной рейтинг с id = %d не найден ", mpaId));
        }

        if (filmCreateRequest.getGenres() != null) {
            filmCreateRequest.getGenres().stream()
                    .filter(g -> g.getId() > GENRES_AMOUNT || g.getId() < 1)
                    .findFirst()
                    .ifPresent(genre -> {
                        throw new NotFoundException(String.format("Жанр с id = %d не найден", genre.getId()));
                    });
        }
    }
}
