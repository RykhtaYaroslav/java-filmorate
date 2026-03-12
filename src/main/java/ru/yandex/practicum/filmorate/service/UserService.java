package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.FriendshipException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.FriendshipStatus;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserStorage storage;

    @Autowired
    public UserService(UserStorage storage) {
        this.storage = storage;
    }

    public User create(User user) {
        log.debug("Создание пользователя: {}", user);
        checkUserName(user); // Set email in name, when name was empty
        if (isEmailExist(user)) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }
        user.setId(getNextId());
        log.info("Пользователь создан с id = {}", user.getId());
        return storage.create(user);
    }

    public User update(User updUser) {
        log.debug("Обновление пользователя: {}", updUser.getId());
        checkUserName(updUser); // Set email in name, when name was empty
        findById(updUser.getId()); // Throw exception when no id or user with this id
        log.info("Данные пользователя с id = {} обновлены", updUser.getId());
        return storage.update(updUser);
    }

    public void delete(Long id) {
        log.debug("Удаляется пользователь с id = {}", id);
        findById(id); // Throw exception when no id or user with this id
        log.info("Пользователь с id = {} удалён", id);
        storage.delete(id);
    }

    public Collection<User> findAll() {
        log.info("Возвращается коллекция всех пользователей");
        return storage.getUsers();
    }

    public User findById(Long id) {
        log.debug("Выполняется поиск пользователя по id = {}", id);
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        Optional<User> optionalUser = storage.findById(id);

        if (optionalUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        log.debug("Найден пользователь с id = {}", id);
        return optionalUser.get();
    }

    private Optional<Friendship> findExistingFriendship(Long userId1, Long userId2) {
        Friendship direct = new Friendship(userId1, userId2);
        Friendship reverse = new Friendship(userId2, userId1);

        if (storage.getFriendships().contains(direct)) {
            return storage.findFriendship(direct);
        }
        if (storage.getFriendships().contains(reverse)) {
            return storage.findFriendship(reverse);
        }
        return Optional.empty();
    }

    public Friendship makeFriendship(Long fromUserId, Long toUserId) {
        log.debug("Пользователь id = {} отправляет заявку в друзья пользователю id = {}", fromUserId, toUserId);
        findById(fromUserId);
        findById(toUserId);

        Optional<Friendship> existing = findExistingFriendship(fromUserId, toUserId);

        if (existing.isPresent()) {
            Friendship friendship = existing.get();
            if (friendship.getStatus() == FriendshipStatus.CONFIRMED) {
                throw new FriendshipException(String.format("Пользователи id = %d и id = %d уже являются друзьями", fromUserId, toUserId));
            }
            if (friendship.getFromUserId().equals(fromUserId)) {
                throw new FriendshipException(String.format("Пользователь id = %d уже отправил заявку пользователю id = %d", fromUserId, toUserId));
            }
            // reverse unconfirmed — подтверждаем
            log.info("Пользователь id = {} принял заявку от пользователя id = {}", fromUserId, toUserId);
            return confirmFriendship(toUserId, fromUserId);
        }

        log.info("Пользователь id = {} отправил заявку в друзья пользователю id = {}", fromUserId, toUserId);
        return storage.makeFriendship(new Friendship(fromUserId, toUserId));
    }

    public Friendship confirmFriendship(Long fromUserId, Long toUserId) {
        log.debug("Пользователь id = {} пытается подтвердить дружбу с пользователем id = {}", fromUserId, toUserId);
        findById(fromUserId);
        findById(toUserId);

        Optional<Friendship> existing = findExistingFriendship(fromUserId, toUserId);

        if (existing.isEmpty()) {
            log.info("Заявки не найдено, создаётся новая от пользователя id = {}", fromUserId);
            return storage.makeFriendship(new Friendship(fromUserId, toUserId));
        }

        Friendship friendship = existing.get();

        if (friendship.getStatus() == FriendshipStatus.CONFIRMED) {
            throw new FriendshipException(String.format("Пользователи id = %d и id = %d уже являются друзьями", fromUserId, toUserId));
        }

        friendship.setStatus(FriendshipStatus.CONFIRMED);
        log.info("Дружба между пользователями id = {} и id = {} подтверждена", fromUserId, toUserId);
        return friendship;
    }

    public void deleteFriendship(Long userId, Long friendId) {
        log.debug("Пользователь id = {} хочет удалить из друзей пользователя id = {}", userId, friendId);
        findById(userId);
        findById(friendId);

        Optional<Friendship> existing = findExistingFriendship(userId, friendId);

        if (existing.isEmpty()) {
            throw new FriendshipException(String.format("Пользователи id = %d и id = %d не являются друзьями", userId, friendId));
        }

        log.info("Пользователи id = {} и id = {} больше не друзья", userId, friendId);
        storage.deleteFriendship(existing.get());
    }

    public Collection<User> getFriends(Long id) {
        log.debug("Поиск друзей пользователя id = {}", id);
        findById(id); // Throw exception when no id or user with this id
        return storage.getFriendships()
                .stream()
                .filter(f -> f.getFromUserId().equals(id) || f.getToUserId().equals(id))
                .map(f -> f.getFromUserId().equals(id) ? f.getToUserId() : f.getFromUserId())
                .map(this::findById)
                .toList();
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        log.debug("Поиск общих друзей пользователей id = {} и id = {}", id, otherId);
        findById(id); // Throw exception when no id or user with this id
        findById(otherId); // Throw exception when no id or user with this id

        Collection<User> idFriends = getFriends(id);
        Collection<User> otherIdFriends = getFriends(otherId);
        return idFriends.stream().filter(otherIdFriends::contains).toList();
    }

    // вспомогательный метод для генерации идентификатора
    private long getNextId() {
        long currentMaxId = storage.getUsers()
                .stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void checkUserName(User user) {
        log.debug("Проверка имени пользователя id = {}", user.getId());
        if (user.getName() == null || user.getName().isEmpty()) {
            log.debug("Имя пользователя было пустым, установлен логин вместо имени");
            user.setName(user.getLogin());
        }
    }

    private boolean isEmailExist(User user) {
        log.debug("Проверяется, не занят ли имейл {} пользователя id = {} другим пользователем", user.getEmail(), user.getId());
        return storage.getUsers().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()));
    }
}
