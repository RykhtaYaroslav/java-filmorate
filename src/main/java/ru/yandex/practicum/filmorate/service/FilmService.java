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
        log.debug("Запрос на создание фильма: {}", filmCreateRequest);
        validateFilm(filmCreateRequest);

        Film film = filmStorage.create(FilmMapper.mapToFilm(filmCreateRequest));

        log.info("Фильм {} (id={}) успешно создан", film.getName(), film.getId());
        return findById(film.getId());
    }

    public FilmDto update(FilmUpdateRequest filmUpdateRequest) {
        log.debug("Запрос на обновление фильма id={}", filmUpdateRequest.getId());

        Film film = filmStorage.update(FilmMapper.mapToFilm(filmUpdateRequest));

        log.info("Данные фильма id={} успешно обновлены", film.getId());
        return findById(film.getId());
    }

    public void delete(Long id) {
        log.debug("Запрос на удаление фильма id={}", id);

        filmStorage.delete(id);

        log.info("Фильм id={} успешно удалён", id);
    }

    public Collection<FilmDto> findAll() {
        log.debug("Запрос на получение всех фильмов");

        Set<Film> filmSet = filmStorage.getFilms();

        if (filmSet.isEmpty()) {
            log.info("Список фильмов пуст");
            return Collections.emptyList();
        }

        Map<Long, Set<Genre>> genres = filmGenreRepository.getGenres();
        Map<Long, Set<Long>> likes = likeRepository.getLikes();

        filmSet.forEach(film -> {
            film.setGenres(genres.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setUserLikeIds(likes.getOrDefault(film.getId(), new HashSet<>()));
        });

        log.info("Возвращено {} фильмов", filmSet.size());
        return filmSet.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public FilmDto findById(Long id) {
        log.debug("Запрос на поиск фильма по id={}", id);
        if (id == null) {
            throw new ConditionsNotMetException("Id не может быть null");
        }

        Optional<Film> optionalFilm = filmStorage.findById(id);

        if (optionalFilm.isEmpty()) {
            log.warn("Фильм с id={} не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        Film film = optionalFilm.get();
        film.setGenres(filmGenreRepository.getGenres(id));
        film.setUserLikeIds(likeRepository.getLikes(id));

        log.debug("Найден фильм: {}", film);
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto addLike(Long filmId, Long userId) {
        log.debug("Запрос от пользователя id={} на добавление лайка фильму id={}", userId, filmId);

        likeRepository.addLike(filmId, userId);
        log.info("Пользователь id={} успешно поставил лайк фильму id={}", userId, filmId);
        return findById(filmId);
    }

    public FilmDto deleteLike(Long filmId, Long userId) {
        log.debug("Запрос от пользователя id={} на удаление лайка с фильма id={}", userId, filmId);

        likeRepository.deleteLike(filmId, userId);
        log.info("Пользователь id={} успешно удалил лайк с фильма id={}", userId, filmId);
        return findById(filmId);
    }

    public Collection<FilmDto> getPopularFilms(int count) {
        log.debug("Запрос на получение {} самых популярных фильмов", count);

        Collection<Film> films = filmStorage.getPopularFilms(count);

        if (films.isEmpty()) {
            log.info("Список популярных фильмов пуст");
            return Collections.emptyList();
        }

        enrichFilms(films);
        log.info("Возвращено {} популярных фильмов", films.size());
        return films.stream().map(FilmMapper::mapToFilmDto).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Collection<FilmDto> getFilmRecommendations(Long id) {
        log.debug("Запрос на получение рекомендованных фильмов для User {}", id);
        return filmStorage.getRecommendationFilms(id).stream().map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void enrichFilms(Collection<Film> films) {
        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        Map<Long, Set<Genre>> genres = filmGenreRepository.getGenresForFilms(filmIds);
        Map<Long, Set<Long>> likes = likeRepository.getLikesForFilms(filmIds);

        films.forEach(film -> {
            film.setGenres(genres.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setUserLikeIds(likes.getOrDefault(film.getId(), new HashSet<>()));
        });
    }

    private void validateFilm(FilmCreateRequest filmCreateRequest) {
        log.debug("Валидация фильма: {}", filmCreateRequest.getName());
        int mpaId = filmCreateRequest.getMpa().getId();

        if (mpaId > MPA_RATINGS_AMOUNT || mpaId < 1) {
            log.warn("Попытка создать фильм с несуществующим mpa id={}", mpaId);
            throw new NotFoundException(String.format("Возрастной рейтинг с id = %d не найден ", mpaId));
        }

        if (filmCreateRequest.getGenres() != null) {
            filmCreateRequest.getGenres().stream()
                    .filter(g -> g.getId() > GENRES_AMOUNT || g.getId() < 1)
                    .findFirst()
                    .ifPresent(genre -> {
                        log.warn("Попытка создать фильм с несуществующим жанром id={}", genre.getId());
                        throw new NotFoundException(String.format("Жанр с id = %d не найден", genre.getId()));
                    });
        }
        log.debug("Валидация фильма {} прошла успешно", filmCreateRequest.getName());
    }
}
