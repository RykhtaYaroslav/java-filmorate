package ru.yandex.practicum.filmorate.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.filmorate.model.enums.FriendshipStatus;

@EqualsAndHashCode
@ToString
@Getter
public class Friendship {
    private final Long fromUserId;
    private final Long toUserId;
    @Setter
    private FriendshipStatus status;

    public Friendship(Long fromUserId, Long toUserId) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.status = FriendshipStatus.UNCONFIRMED;
    }
}