package ru.yandex.practicum.filmorate.model;

import lombok.Data;

/**
 * Класс-заглушка для представления лайка.
 * <p>
 * В текущей реализации не используется и служит для корректной работы
 * {@link ru.yandex.practicum.filmorate.dal.repositories.like.LikeRepository}.
 * </p>
 */
@Data
public class Like {
    private Long userId;
}
