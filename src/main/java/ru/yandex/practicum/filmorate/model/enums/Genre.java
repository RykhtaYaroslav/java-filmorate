package ru.yandex.practicum.filmorate.model.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum Genre {
    COMEDY(1, "Комедия"),
    DRAMA(2, "Драма"),
    CARTOON(3, "Мультфильм"),
    THRILLER(4, "Триллер"),
    DOCUMENTARY(5, "Документальный"),
    ACTION(6, "Боевик");

    private final int id;
    private final String name;
    private static final Map<Integer, Genre> ID_MAP = new HashMap<>();

    static {
        for (Genre genre : values()) {
            ID_MAP.put(genre.id, genre);
        }
    }

    Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Genre fromId(int id) {
        Genre genre = ID_MAP.get(id);
        if (genre == null) {
            throw new IllegalArgumentException("Неизвестный ID жанра: " + id);
        }
        return genre;
    }

    @Override
    public String toString() {
        return name;
    }
}