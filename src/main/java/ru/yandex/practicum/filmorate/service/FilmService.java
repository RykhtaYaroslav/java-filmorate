package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.LikeException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

@Service
@Slf4j
public class FilmService {
    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage storage;

    @Autowired
    public FilmService(FilmStorage storage) {
        this.storage = storage;
    }

    public Film create(Film film) {
        log.debug("Создание фильма: {}", film);
        checkReleaseData(film); //Throws exception when wrong release data
        film.setId(getNextId());
        log.info("Фильм создан с id = {}", film.getId());
        return storage.create(film);
    }

    public Film update(Film updFilm) {
        log.debug("Обновление фильма: {}", updFilm);
        checkReleaseData(updFilm); //Throws exception when wrong release data
        findById(updFilm.getId()); //throws exception when wrong id
        return storage.update(updFilm);
    }

    public void delete(Long id) {
        findById(id); //throws exception when wrong id
        log.info("Фильм с id = {} удалён", id);
        storage.delete(id);
    }

    public Collection<Film> findAll() {
        return storage.getFilms();
    }

    public Film findById(Long id) {
        // Although "Return value of the method is never used", it may be useful in upcoming updates
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        Optional<Film> optionalFilm = storage.findById(id);

        if (optionalFilm.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        return optionalFilm.get();
    }

    public Film addLike(Long filmId, Long userId) {
        log.debug("Пользователь id = {} хочет поставить лайк фильму id = {}", userId, filmId);
        findById(userId); //throws exception when wrong id
        Film film = findById(filmId);
        if (film.getLikes().contains(userId)) {
            throw new LikeException(String.format("Пользователь с id = %d уже поставил лайк фильму с id = %d", userId, filmId));
        }
        film.getLikes().add(userId);
        log.info("Пользователь id = {} поставил лайк фильму id = {}", userId, filmId);
        return film;
    }

    public Film deleteLike(Long filmId, Long userId){
        log.debug("Пользователь id = {} убирает лайк с фильма id = {}", userId, filmId);
        Film film = findById(filmId);
        if (!film.getLikes().contains(userId)) {
            throw new LikeException(String.format("Пользователь с id = %d не ставил лайк фильму с id = %d", userId, filmId));
        }
        film.getLikes().remove(userId);
        return film;
    }

    public Collection<Film> getPopularFilms(int count) {
        return storage.getFilms()
                .stream()
                .sorted(Comparator.comparing((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

    private void checkReleaseData(Film film) {
        // Дата релиза — не раньше 28 декабря 1895 года
        log.debug("Выполняется проверка даты релиза фильма: {}", film.getReleaseDate());
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    // вспомогательный метод для генерации идентификатора
    private long getNextId() {
        long currentMaxId = storage.getFilms()
                .stream()
                .mapToLong(Film::getId)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
