package ru.yandex.practicum.filmorate.model.enums;

import lombok.Getter;

@Getter
public enum MpaRating {
    G(1),
    PG(2),
    PG_13(3),
    R(4),
    NC_17(5);

    private final int id;

    MpaRating(int id) {
        this.id = id;
    }

    public static MpaRating fromId(int id) {
        for (MpaRating rating : values()) {
            if (rating.id == id) {
                return rating;
            }
        }
        throw new IllegalArgumentException("Unknown MPA rating ID: " + id);
    }

    public static MpaRating fromString(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim().toUpperCase().replace("-", "_");
        return MpaRating.valueOf(normalized);
    }

    @Override
    public String toString() {
        return this.name().replace("_", "-");
    }
}