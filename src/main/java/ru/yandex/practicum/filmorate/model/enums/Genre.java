package ru.yandex.practicum.filmorate.model.enums;

import lombok.Getter;

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

    Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Genre fromId(int id) {
        for (Genre genre : values()) {
            if (genre.id == id) {
                return genre;
            }
        }
        throw new IllegalArgumentException("Неизвестный ID жанра: " + id);
    }

    @Override
    public String toString() {
        return name;
    }
}