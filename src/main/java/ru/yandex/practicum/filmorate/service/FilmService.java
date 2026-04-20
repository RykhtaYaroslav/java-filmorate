package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.repositories.director.DirectorRepository;
import ru.yandex.practicum.filmorate.dal.repositories.film.FilmRepository;
import ru.yandex.practicum.filmorate.dal.repositories.genre.GenreRepository;
import ru.yandex.practicum.filmorate.dal.repositories.like.LikeRepository;
import ru.yandex.practicum.filmorate.dto.film.FilmCreateRequest;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.FilmUpdateRequest;
import ru.yandex.practicum.filmorate.dto.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Genre;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository filmRepository;
    private final GenreRepository genreRepository;
    private final LikeRepository likeRepository;
    private final DirectorRepository directorRepository;
    private final UserService userService;
    private final FeedService feedService;

    private static final int MPA_RATINGS_AMOUNT = 5;
    private static final int GENRES_AMOUNT = 6;

    public FilmDto create(FilmCreateRequest filmCreateRequest) {
        log.debug("Запрос на создание фильма: {}", filmCreateRequest);
        validateFilm(filmCreateRequest);

        Film film = filmRepository.create(FilmMapper.mapToFilm(filmCreateRequest));

        saveFilmGenresAndDirectors(film);

        log.info("Фильм {} (id={}) успешно создан", film.getName(), film.getId());
        return findById(film.getId());
    }

    public FilmDto update(FilmUpdateRequest filmUpdateRequest) {
        log.debug("Запрос на обновление фильма id={}", filmUpdateRequest.getId());

        Film film = filmRepository.update(FilmMapper.mapToFilm(filmUpdateRequest));

        updateFilmGenresAndDirectors(film);

        log.info("Данные фильма id={} успешно обновлены", film.getId());
        return findById(film.getId());
    }

    public void delete(Long id) {
        log.debug("Запрос на удаление фильма id={}", id);

        filmRepository.delete(id);

        log.info("Фильм id={} успешно удалён", id);
    }

    public Collection<FilmDto> findAll() {
        log.debug("Запрос на получение всех фильмов");

        Set<Film> filmSet = filmRepository.getFilms();

        if (filmSet.isEmpty()) {
            log.info("Список фильмов пуст");
            return Collections.emptyList();
        }

        List<Film> films = new ArrayList<>(filmSet);
        enrichFilms(films);

        log.info("Возвращено {} фильмов", films.size());
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public FilmDto findById(Long id) {
        log.debug("Запрос на поиск фильма по id={}", id);
        if (id == null) {
            throw new ConditionsNotMetException("Id не может быть null");
        }

        Optional<Film> optionalFilm = filmRepository.findById(id);

        if (optionalFilm.isEmpty()) {
            log.warn("Фильм с id={} не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        Film film = optionalFilm.get();
        film.setGenres(genreRepository.getGenres(id));
        film.setUserLikeIds(likeRepository.getLikes(id));
        film.setDirectors(new LinkedHashSet<>(directorRepository.getDirectorsByFilmId(id)));

        log.debug("Найден фильм: {}", film);
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto addLike(Long filmId, Long userId) {
        log.debug("Запрос от пользователя id={} на добавление лайка фильму id={}", userId, filmId);
        feedService.addEvent(userId, EventType.LIKE, EventOperation.ADD, filmId);
        likeRepository.addLike(filmId, userId);

        log.info("Пользователь id={} успешно поставил лайк фильму id={}", userId, filmId);
        return findById(filmId);
    }

    public FilmDto deleteLike(Long filmId, Long userId) {
        log.debug("Запрос от пользователя id={} на удаление лайка с фильма id={}", userId, filmId);

        boolean isDeleted = likeRepository.deleteLike(filmId, userId);

        if (isDeleted) {
            feedService.addEvent(userId, EventType.LIKE, EventOperation.REMOVE, filmId);
            log.info("Пользователь id={} успешно удалил лайк с фильма id={}", userId, filmId);
        }
        return findById(filmId);
    }

    public Collection<FilmDto> getPopularFilms(Integer count, Integer genreId, Integer year) {
        log.debug("Запрос на получение {} самых популярных фильмов", count);
        Collection<Film> films = filmRepository.getPopularFilms(count, genreId, year);
        enrichFilms(films);

        log.info("Возвращено {} популярных фильмов", films.size());
        return films.stream().map(FilmMapper::mapToFilmDto).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Collection<FilmDto> getFilmsByDirector(Long directorId, String sortBy) {
        log.debug("Запрос на получение фильмов режиссера id={} с сортировкой по {}", directorId, sortBy);

        if (directorRepository.findById(directorId).isEmpty()) {
            log.warn("Режиссер с id={} не найден", directorId);
            throw new NotFoundException("Режиссер с id = " + directorId + " не найден");
        }

        Collection<Film> films = filmRepository.getFilmsByDirector(directorId, sortBy);

        enrichFilms(films);
        log.info("Возвращено {} фильмов для режиссера id={}", films.size(), directorId);
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Collection<FilmDto> getFilmRecommendations(Long id) {
        log.debug("Запрос на получение рекомендованных фильмов для User {}", id);
        Collection<Film> recommended = new ArrayList<>(filmRepository.getRecommendationFilms(id));
        enrichFilms(recommended);
        return recommended.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Collection<FilmDto> searchFilms(String query, String by) {
        log.debug("Запрос на поиск фильмов по запросу '{}' в полях '{}'", query, by);
        Collection<Film> films = filmRepository.getSearchFilms(query, by);
        enrichFilms(films);
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Collection<FilmDto> getCommonFilms(Long userId, Long friendId) {
        log.debug("Запрос на получение общих фильмов для User {} и User {}", userId, friendId);
        userService.findById(userId);
        userService.findById(friendId);

        Collection<Film> commonFilms = filmRepository.getCommonFilms(userId, friendId);
        log.debug("Возвращено {} общих фильмов для пользователей User {} и User {}", commonFilms.size(), userId, friendId);
        enrichFilms(commonFilms);
        return commonFilms.stream().map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void enrichFilms(Collection<Film> films) {
        List<Long> filmIds = films.stream().map(Film::getId).toList();

        Map<Long, Set<Genre>> genres = genreRepository.getGenresForFilms(filmIds);
        Map<Long, Set<Long>> likes = likeRepository.getLikesForFilms(filmIds);
        Map<Long, Set<Director>> directors = directorRepository.getDirectorsForFilms(filmIds);

        films.forEach(film -> {
            film.setGenres(genres.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setUserLikeIds(likes.getOrDefault(film.getId(), new HashSet<>()));
            film.setDirectors(directors.getOrDefault(film.getId(), new LinkedHashSet<>()));
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

    private void saveFilmGenresAndDirectors(Film film) {

        if (film.getGenres() != null) {
            genreRepository.addGenres(film.getId(), film.getGenres());
        }

        Collection<Director> directors = film.getDirectors();

        if (directors != null && !directors.isEmpty()) {
            directorRepository.setDirectorsToFilm(film.getId(), directors);
        }
    }

    private void updateFilmGenresAndDirectors(Film film) {
        Collection<Director> directors = film.getDirectors();

        if (directors != null) {
            directorRepository.deleteFilmDirectors(film.getId());

            if (!directors.isEmpty()) {
                directorRepository.setDirectorsToFilm(film.getId(), directors);
            }
        }

        if (film.getGenres() != null) {
            genreRepository.deleteGenres(film.getId());
            if (!film.getGenres().isEmpty()) {
                genreRepository.addGenres(film.getId(), film.getGenres());
            }
        }
    }
}
