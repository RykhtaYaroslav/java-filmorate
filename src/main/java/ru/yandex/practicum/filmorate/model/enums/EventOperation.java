package ru.yandex.practicum.filmorate.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum EventOperation {
    ADD(1, "ADD"),
    REMOVE(2, "REMOVE"),
    UPDATE(3, "UPDATE");

    private final int id;
    private final String name;

    EventOperation(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static EventOperation fromId(int id) {
        for (EventOperation operation : values()) {
            if (operation.id == id) {
                return operation;
            }
        }
        throw new IllegalArgumentException("Неизвестный ID операции ивента: " + id);
    }

    @JsonValue
    @Override
    public String toString() {
        return name;
    }
}
