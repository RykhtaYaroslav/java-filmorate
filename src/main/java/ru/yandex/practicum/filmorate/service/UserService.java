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
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipRepository friendshipRepository;
    private final FeedService feedService;

    public UserDto create(UserCreateRequest userCreateRequest) {
        log.debug("Запрос на создание пользователя: {}", userCreateRequest);

        User user = userStorage.create(UserMapper.mapToUser(userCreateRequest));

        log.info("Пользователь {} (id={}) успешно создан", user.getLogin(), user.getId());
        return UserMapper.mapToUserDto(user);
    }

    public UserDto update(UserUpdateRequest userUpdateRequest) {
        log.debug("Запрос на обновление пользователя id={}", userUpdateRequest.getId());

        User user = userStorage.update(UserMapper.mapToUser(userUpdateRequest));

        log.info("Данные пользователя id={} успешно обновлены", user.getId());
        return UserMapper.mapToUserDto(user);
    }

    public void delete(Long id) {
        log.debug("Запрос на удаление пользователя id={}", id);

        userStorage.delete(id);

        log.info("Пользователь id={} успешно удалён", id);
    }

    public Collection<UserDto> findAll() {
        log.debug("Запрос на получение всех пользователей");
        Collection<User> users = userStorage.getUsers();
        if (users.isEmpty()) {
            log.info("Список пользователей пуст");
            return Collections.emptyList();
        }
        log.info("Возвращено {} пользователей", users.size());
        return users.stream().map(UserMapper::mapToUserDto).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public UserDto findById(Long id) {
        log.debug("Запрос на поиск пользователя по id={}", id);
        if (id == null) {
            throw new ConditionsNotMetException("Id не может быть null");
        }

        Optional<User> optionalUser = userStorage.findById(id);

        if (optionalUser.isEmpty()) {
            log.warn("Пользователь с id={} не найден", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }

        User user = optionalUser.get();
        log.debug("Найден пользователь: {}", user);
        return UserMapper.mapToUserDto(user);
    }

    public Friendship sendFriendshipRequest(Long fromUserId, Long toUserId) {
        log.debug("Запрос на добавление в друзья от id={} к id={}", fromUserId, toUserId);

        if (fromUserId.equals(toUserId)) {
            log.warn("Попытка добавить самого себя в друзья (id={})", fromUserId);
            throw new DataConflictException("Нельзя добавить самого себя в друзья");
        }

        checkUserExist(fromUserId);
        checkUserExist(toUserId);

        Friendship friendship = friendshipRepository.sendFriendshipRequest(new Friendship(fromUserId, toUserId));
        feedService.addEvent(fromUserId, EventType.FRIEND, EventOperation.ADD, toUserId);
        log.info("Пользователь id={} успешно отправил заявку в друзья пользователю id={}", fromUserId, toUserId);
        return friendship;
    }

    public void deleteFriendship(Long userId, Long friendId) {
        log.debug("Запрос на удаление из друзей от id={} к id={}", userId, friendId);
        checkUserExist(userId);
        checkUserExist(friendId);
        boolean removed = friendshipRepository.deleteFriendship(new Friendship(userId, friendId));
        if (removed) {
            feedService.addEvent(userId, EventType.FRIEND, EventOperation.REMOVE, friendId);
            log.info("Пользователь id={} удалил из друзей пользователя id={}", userId, friendId);
        } else {
            log.debug("У пользователя id={} не было друга id={} — запись в ленту не добавлена", userId, friendId);
        }
    }

    public Collection<UserDto> getUserFriends(Long id) {
        log.debug("Запрос на получение друзей пользователя id={}", id);
        checkUserExist(id);

        Set<Long> friendIds = friendshipRepository.getFriends(id);
        if (friendIds.isEmpty()) {
            log.info("У пользователя id={} нет друзей", id);
            return Collections.emptyList();
        }

        List<UserDto> friends = friendIds.stream()
                .map(userStorage::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(User::getId))
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toCollection(ArrayList::new));

        log.info("У пользователя id={} найдено {} друзей", id, friends.size());
        return friends;
    }

    public Collection<UserDto> getCommonFriends(Long id, Long otherId) {
        log.debug("Запрос на получение общих друзей для пользователей id={} и id={}", id, otherId);
        checkUserExist(id);
        checkUserExist(otherId);

        Set<Long> idFriends = friendshipRepository.getFriends(id);
        Set<Long> otherIdFriends = friendshipRepository.getFriends(otherId);

        List<Long> commonFriendIds = idFriends.stream()
                .filter(otherIdFriends::contains)
                .sorted()
                .toList();

        if (commonFriendIds.isEmpty()) {
            log.info("У пользователей id={} и id={} нет общих друзей", id, otherId);
            return Collections.emptyList();
        }

        List<UserDto> commonFriends = commonFriendIds.stream()
                .map(userStorage::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toCollection(ArrayList::new));

        log.info("Найдено {} общих друзей для пользователей id={} и id={}", commonFriends.size(), id, otherId);
        return commonFriends;
    }

    private void checkUserExist(Long id) {
        log.debug("Проверка существования пользователя с id={}", id);
        if (userStorage.findById(id).isEmpty()) {
            log.warn("Пользователь с id={} не найден в хранилище", id);
            throw new NotFoundException(String.format("Пользователь с id %d не найден", id));
        }
    }
}
