package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

@Data
@Builder
public class Event {
    private Long eventId;
    private final Long userId;
    private final EventType eventType;
    private final EventOperation operation;
    private final Long timestamp;
    private final Long entityId;
}
