package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
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

    public Collection<Film> findAll() {
        return storage.getFilms();
    }

    public Film create(Film film) {
        checkReleaseData(film); //Throws exception when wrong release data
        film.setId(getNextId());
        return storage.create(film, film.getId());
    }

    public Film update(Film updFilm) {
        checkReleaseData(updFilm); //Throws exception when wrong release data
        findById(updFilm.getId()); //throws exception when wrong id
        return storage.update(updFilm);
    }

    public void delete(Long id) {
        findById(id); //throws exception when wrong id
        storage.delete(id);
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

    private void checkReleaseData(Film film) {
        // Дата релиза — не раньше 28 декабря 1895 года
        //log.trace("Выполняется проверка даты релиза фильма: {}", film.getReleaseDate());
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            log.error("Некорректная дата релиза фильма: {}", film.getReleaseDate());
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
