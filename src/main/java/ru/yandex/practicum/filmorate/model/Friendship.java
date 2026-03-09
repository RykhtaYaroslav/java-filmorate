package ru.yandex.practicum.filmorate.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
public class Friendship {
    private final Long firstUserId;
    private final Long secondUserId;

    public Friendship(Long firstUserId, Long secondUserId) {
        // To avoid duplicates like (u1, u2) & (u2, u1) constructor use Math.max & min
        this.firstUserId = Math.min(firstUserId, secondUserId);
        this.secondUserId = Math.max(firstUserId, secondUserId);
    }
}
