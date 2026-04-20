package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.repositories.feed.FeedRepository;
import ru.yandex.practicum.filmorate.dto.feed.FeedDto;
import ru.yandex.practicum.filmorate.dto.mappers.FeedMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedService {
    private final FeedRepository feedRepository;

    public void addEvent(Long userId, EventType type, EventOperation operation, Long entityId) {
        Long nowToEpochMilli = Instant.now().toEpochMilli();
        Event event = Event.builder()
                .userId(userId)
                .eventType(type)
                .operation(operation)
                .timestamp(nowToEpochMilli)
                .entityId(entityId)
                .build();
        feedRepository.addEvent(event);
        log.info("Добавлен ивент {}, от пользователя {} в отношении entity с id {}",
                type.toString(), userId, entityId);
        log.debug(event.toString());
    }

    public Collection<FeedDto> getFeed(Long userId) {
        log.info("Получение пользователем {} ленты новостей", userId);
        Collection<Event> events = feedRepository.getEventsByUserId(userId);
        log.debug(events.toString());
        return events.stream().map(FeedMapper::mapToFeed)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
