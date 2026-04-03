package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.repositories.UserStorage;
import ru.yandex.practicum.filmorate.dto.mappers.UserMapper;
import ru.yandex.practicum.filmorate.dto.user.UserCreateRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.dto.user.UserUpdateRequest;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.DataConflictException;
import ru.yandex.practicum.filmorate.exceptions.FriendshipException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.FriendshipStatus;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage storage;

    @Autowired
    public UserService(UserStorage storage) {
        this.storage = storage;
    }

    public UserDto create(UserCreateRequest userCreateRequest) {
        log.debug("Создание пользователя: {}", userCreateRequest);

        User user = storage.create(UserMapper.mapToUser(userCreateRequest));

        log.info("Пользователь создан с id = {}", user.getId());
        return UserMapper.mapToUserDto(user);
    }

    public UserDto update(UserUpdateRequest userUpdateRequest) {
        log.debug("Обновление пользователя: {}", userUpdateRequest.getId());

        User user = storage.update(UserMapper.mapToUser(userUpdateRequest));

        log.info("Данные пользователя с id = {} обновлены", user.getId());
        return UserMapper.mapToUserDto(user);
    }

    public void delete(Long id) {
        log.debug("Удаляется пользователь с id = {}", id);

        storage.delete(id);

        log.info("Пользователь с id = {} удалён", id);
    }

    public Collection<UserDto> findAll() {
        log.info("Возвращается коллекция всех пользователей");

        Collection<User> users = storage.getUsers();
        return users.stream().map(UserMapper::mapToUserDto).collect(Collectors.toSet());
    }

    public UserDto findById(Long id) {
        log.debug("Выполняется поиск пользователя по id = {}", id);
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        Optional<User> optionalUser = storage.findById(id);

        if (optionalUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }

        User user = optionalUser.get();

        log.debug("Найден пользователь с id = {}", user.getId());
        return UserMapper.mapToUserDto(user);
    }

    public Friendship sendFriendshipRequest(Long fromUserId, Long toUserId) {
        log.debug("Пользователь id = {} отправляет заявку в друзья пользователю id = {}", fromUserId, toUserId);

        if (fromUserId.equals(toUserId)) {
            throw new DataConflictException("Нельзя добавить самого себя в друзья");
        }

        Friendship friendship = storage.sendFriendshipRequest(new Friendship(fromUserId, toUserId));

        log.info("Пользователь id = {} отправил заявку в друзья пользователю id = {}", friendship.getFromUserId(), friendship.getToUserId());
        return friendship;
    }

    public Friendship confirmFriendship(Long fromUserId, Long toUserId) {
        log.debug("Пользователь id = {} пытается подтвердить дружбу с пользователем id = {}", fromUserId, toUserId);

        Friendship friendship = storage.confirmFriendship(new Friendship(fromUserId, toUserId));

        log.info("Дружба между пользователями id = {} и id = {} подтверждена", fromUserId, toUserId);
        return friendship;
    }

    public void deleteFriendship(Long userId, Long friendId) {
        log.debug("Пользователь id = {} хочет удалить из друзей пользователя id = {}", userId, friendId);

        storage.deleteFriendship(new Friendship(userId, friendId));

        log.info("Пользователи id = {} и id = {} больше не друзья", userId, friendId);
    }

    public Collection<UserDto> getUserFriends(Long id) {
        log.debug("Поиск друзей пользователя id = {}", id);

        Set<User> friends = storage.getUserFriends(id);

        return friends.stream().map(UserMapper::mapToUserDto).collect(Collectors.toSet());
    }

    public Collection<UserDto> getCommonFriends(Long id, Long otherId) {
        log.debug("Поиск общих друзей пользователей id = {} и id = {}", id, otherId);

        Set<User> commonFriendsSet = storage.findCommonFriends(id, otherId);

        return commonFriendsSet.stream().map(UserMapper::mapToUserDto).collect(Collectors.toSet());
    }
}