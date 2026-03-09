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
        log.debug("Обновление пользователя: {}", updUser);
        checkUserName(updUser); // Set email in name, when name was empty
        findById(updUser.getId()); // Throw exception when no id or user with this id
        return storage.update(updUser);
    }

    public void delete(Long id) {
        log.info("Пользователь с id = {} удалён", id);
        findById(id); // Throw exception when no id or user with this id
        storage.delete(id);
    }

    public Collection<User> findAll() {
        return storage.getUsers();
    }

    public User findById(Long id) {
        // Although "Return value of the method is never used", it may be useful in upcoming updates
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        Optional<User> optionalUser = storage.findById(id);

        if (optionalUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return optionalUser.get();
    }

    public Friendship makeFriendship(Long userId, Long friendId) {
        findById(userId); // Throw exception when no id or user with this id
        findById(friendId); // Throw exception when no id or user with this id

        Friendship friendship = new Friendship(userId, friendId);

        if (storage.getFriendships().contains(friendship)) {
            throw new FriendshipException(String.format("Пользователи с id = %d и id = %d уже друзья", userId, friendId));
        }
        log.info("Пользователи id = {} и id = {} теперь друзья", userId, friendId);
        return storage.makeFriendship(friendship);
    }

    public void deleteFriendship(Long userId, Long friendId) {
        findById(userId); // Throw exception when no id or user with this id
        findById(friendId); // Throw exception when no id or user with this id

        Friendship friendship = new Friendship(userId, friendId);

        log.info("Пользователи id = {} и id = {} больше не друзья", userId, friendId);
        storage.deleteFriendship(friendship);
    }

    public Collection<User> getFriends(Long id) {
        findById(id); // Throw exception when no id or user with this id
        return storage.getFriendships()
                .stream()
                .filter(f -> f.getFirstUserId().equals(id) || f.getSecondUserId().equals(id))
                .map(f -> f.getFirstUserId().equals(id) ? f.getSecondUserId() : f.getFirstUserId())
                .map(this::findById)
                .toList();
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
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
        if (user.getName() == null || user.getName().isEmpty()) {
            log.debug("Имя пользователя было пустым, установлен логин вместо имени");
            user.setName(user.getLogin());
        }
    }

    private boolean isEmailExist(User user) {
        return storage.getUsers().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()));
    }
}
