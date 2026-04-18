package ru.yandex.practicum.filmorate.dto.feed;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

@Data
@Builder
public class FeedDto {
    private final Long timestamp;
    private final Long userId;
    private final EventType eventType;
    private final EventOperation operation;
    private final Long eventId;
    private final Long entityId;
}
