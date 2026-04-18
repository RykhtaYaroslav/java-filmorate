package ru.yandex.practicum.filmorate.dal.repositories.feed;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.Collection;

public interface FeedRepository {

    public void addEvent(Event event);

    public Collection<Event> getEventsByUserId(Long id);

}
