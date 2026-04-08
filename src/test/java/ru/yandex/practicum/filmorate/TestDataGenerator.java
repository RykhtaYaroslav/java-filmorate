package ru.yandex.practicum.filmorate;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.MpaRating;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

public class TestDataGenerator {
    private static final Random random = new Random();

    public static User generateUser() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return User.builder()
                .email(uniqueId + "@test.com")
                .login("login_" + uniqueId)
                .name("Name " + uniqueId)
                .birthday(LocalDate.of(1980 + random.nextInt(40), 1, 1))
                .build();
    }

    public static Film generateFilm() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        Film film = new Film();
        film.setName("Film " + uniqueId);
        film.setDescription("Description " + uniqueId);
        film.setReleaseDate(LocalDate.of(2000 + random.nextInt(20), 1, 1));
        film.setDuration(90L + random.nextLong(60));

        MpaRating mpa = MpaRating.G;
        film.setRating(mpa);
        film.setGenres(new HashSet<>());

        return film;
    }
}