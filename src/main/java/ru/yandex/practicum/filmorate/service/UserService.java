package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.repositories.friendship.FriendshipRepository;
import ru.yandex.practicum.filmorate.dal.repositories.user.UserStorage;
import ru.yandex.practicum.filmorate.dto.mappers.UserMapper;
import ru.yandex.practicum.filmorate.dto.user.UserCreateRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.dto.user.UserUpdateRequest;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.DataConflictException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipRepository friendshipRepository;

    public UserDto create(UserCreateRequest userCreateRequest) {
        log.debug("Создание пользователя: {}", userCreateRequest);

        User user = userStorage.create(UserMapper.mapToUser(userCreateRequest));

        log.info("Пользователь создан с id = {}", user.getId());
        return UserMapper.mapToUserDto(user);
    }

    public UserDto update(UserUpdateRequest userUpdateRequest) {
        log.debug("Обновление пользователя: {}", userUpdateRequest.getId());

        User user = userStorage.update(UserMapper.mapToUser(userUpdateRequest));

        log.info("Данные пользователя с id = {} обновлены", user.getId());
        return UserMapper.mapToUserDto(user);
    }

    public void delete(Long id) {
        log.debug("Удаляется пользователь с id = {}", id);

        userStorage.delete(id);

        log.info("Пользователь с id = {} удалён", id);
    }

    public Collection<UserDto> findAll() {
        log.info("Возвращается коллекция всех пользователей");

        Collection<User> users = userStorage.getUsers();
        return users.stream().map(UserMapper::mapToUserDto).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public UserDto findById(Long id) {
        log.debug("Выполняется поиск пользователя по id = {}", id);
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        Optional<User> optionalUser = userStorage.findById(id);

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

        checkUserExist(fromUserId);
        checkUserExist(toUserId);

        Friendship friendship = friendshipRepository.sendFriendshipRequest(new Friendship(fromUserId, toUserId));

        log.info("Пользователь id = {} отправил заявку в друзья пользователю id = {}", friendship.getFromUserId(), friendship.getToUserId());
        return friendship;
    }

    public void deleteFriendship(Long userId, Long friendId) {
        log.debug("Пользователь id = {} хочет удалить из друзей пользователя id = {}", userId, friendId);
        checkUserExist(userId);
        checkUserExist(friendId);
        friendshipRepository.deleteFriendship(new Friendship(userId, friendId));

        log.info("Пользователи id = {} и id = {} больше не друзья", userId, friendId);
    }

    public Collection<UserDto> getUserFriends(Long id) {
        log.debug("Поиск друзей пользователя id = {}", id);

        if (userStorage.findById(id).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден", id));
        }
        Set<Long> friendIds = friendshipRepository.getFriends(id);
        Set<User> friendUsers = friendIds.stream()
                .map(userStorage::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        return friendUsers.stream().map(UserMapper::mapToUserDto).collect(Collectors.toSet());
    }

    public Collection<UserDto> getCommonFriends(Long id, Long otherId) {
        log.debug("Поиск общих друзей пользователей id = {} и id = {}", id, otherId);
        Set<Long> idFriends = friendshipRepository.getFriends(id);
        Set<Long> otherIdFriends = friendshipRepository.getFriends(otherId);

        Set<Long> commonFriendIds = idFriends.stream()
                .filter(otherIdFriends::contains)
                .collect(Collectors.toSet());

        Set<User> commonFriends = commonFriendIds.stream()
                .map(userStorage::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        return commonFriends.stream().map(UserMapper::mapToUserDto).collect(Collectors.toSet());
    }

    private void checkUserExist(Long id) {
        if (userStorage.findById(id).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден", id));
        }
    }
}
