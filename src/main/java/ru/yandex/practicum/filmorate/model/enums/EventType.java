package ru.yandex.practicum.filmorate.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum EventType {
    LIKE(1, "LIKE"),
    REVIEW(2, "REVIEW"),
    FRIEND(3, "FRIEND");

    private final int id;
    private final String name;

    EventType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static EventType fromId(int id) {
        for (EventType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Неизвестный ID типа ивента: " + id);
    }

    @JsonValue
    @Override
    public String toString() {
        return name;
    }
}
