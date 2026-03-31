package ru.yandex.practicum.filmorate.model.enums;

import lombok.Getter;

@Getter
public enum Genre {
    // Стандарт (1-6)
    COMEDY(1, "Комедия"),
    DRAMA(2, "Драма"),
    CARTOON(3, "Мультфильм"),
    THRILLER(4, "Триллер"),
    DOCUMENTARY(5, "Документальный"),
    ACTION(6, "Боевик"),
    ROMANCE(7, "Мелодрама"),
    HORROR(8, "Ужасы"),
    SCI_FI(9, "Фантастика"),
    FANTASY(10, "Фэнтези"),
    ADVENTURE(11, "Приключения"),
    CRIME(12, "Криминал"),
    MYSTERY(13, "Детектив"),
    BIOGRAPHY(14, "Биография"),
    HISTORY(15, "История"),
    MUSICAL(16, "Мюзикл"),
    WESTERN(17, "Вестерн"),
    SPORT(18, "Спорт"),
    WAR(19, "Военный"),
    FAMILY(20, "Семейный");

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