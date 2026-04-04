package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.user.FriendshipRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.user.UserExtractor;
import ru.yandex.practicum.filmorate.dal.mappers.user.UserRowMapper;
import ru.yandex.practicum.filmorate.dal.repositories.user.UserDbStorage;
import ru.yandex.practicum.filmorate.exceptions.DataConflictException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.FriendshipStatus;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class, UserExtractor.class, FriendshipRowMapper.class})
class UserDbStorageTest {
    private final UserDbStorage userStorage;
    @Test
    @DisplayName("Проверка сохранения и поиска пользователя по ID")
    void testCreateAndFindUser() {
        User newUser = TestDataGenerator.generateUser();
        User savedUser = userStorage.create(newUser);

        Optional<User> userOptional = userStorage.findById(savedUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", savedUser.getId());
                    assertThat(user).hasFieldOrPropertyWithValue("email", newUser.getEmail());
                });
    }

    @Test
    @DisplayName("Проверка обновления данных пользователя")
    void testUpdateUser() {
        User user = userStorage.create(TestDataGenerator.generateUser());
        user.setName("Updated Name");
        user.setLogin("updated_login");

        userStorage.update(user);
        User updatedUser = userStorage.findById(user.getId()).get();

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getLogin()).isEqualTo("updated_login");
    }

    @Test
    @DisplayName("Проверка получения списка всех пользователей")
    void testGetAllUsers() {
        userStorage.create(TestDataGenerator.generateUser());
        userStorage.create(TestDataGenerator.generateUser());

        Collection<User> users = userStorage.getUsers();

        assertThat(users).hasSize(2);
    }

    @Test
    @DisplayName("Проверка удаления пользователя")
    void testDeleteUser() {
        User user = userStorage.create(TestDataGenerator.generateUser());
        userStorage.delete(user.getId());

        Optional<User> userOptional = userStorage.findById(user.getId());

        assertThat(userOptional).isEmpty();
    }

    @Test
    @DisplayName("Проверка добавления в друзья и получения списка друзей")
    void testFriendshipLogic() {
        User user1 = userStorage.create(TestDataGenerator.generateUser());
        User user2 = userStorage.create(TestDataGenerator.generateUser());

        Friendship friendship = new Friendship(user1.getId(), user2.getId());
        userStorage.sendFriendshipRequest(friendship);

        Set<User> friends = userStorage.getUserFriends(user1.getId());

        assertThat(friends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(user2.getId());
    }

    @Test
    @DisplayName("Проверка подтверждения дружбы")
    void testConfirmFriendship() {
        User user1 = userStorage.create(TestDataGenerator.generateUser());
        User user2 = userStorage.create(TestDataGenerator.generateUser());

        Friendship friendship = new Friendship(user1.getId(), user2.getId());
        userStorage.sendFriendshipRequest(friendship);

        userStorage.confirmFriendship(friendship);
        Optional<Friendship> confirmed = userStorage.findFriendship(friendship);

        assertThat(confirmed).isPresent();
        assertThat(confirmed.get().getStatus()).isEqualTo(FriendshipStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Проверка удаления из друзей")
    void testDeleteFriendship() {
        User user1 = userStorage.create(TestDataGenerator.generateUser());
        User user2 = userStorage.create(TestDataGenerator.generateUser());
        Friendship friendship = new Friendship(user1.getId(), user2.getId());

        userStorage.sendFriendshipRequest(friendship);
        userStorage.deleteFriendship(friendship);

        Set<User> friends = userStorage.getUserFriends(user1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    @DisplayName("Проверка поиска общих друзей")
    void testFindCommonFriends() {
        User user = userStorage.create(TestDataGenerator.generateUser());
        User other = userStorage.create(TestDataGenerator.generateUser());
        User commonFriend = userStorage.create(TestDataGenerator.generateUser());

        // Оба дружат с commonFriend
        Friendship f1 = new Friendship(user.getId(), commonFriend.getId());
        f1.setStatus(FriendshipStatus.CONFIRMED);
        userStorage.sendFriendshipRequest(f1);

        Friendship f2 = new Friendship(other.getId(), commonFriend.getId());
        f2.setStatus(FriendshipStatus.CONFIRMED);
        userStorage.sendFriendshipRequest(f2);

        Set<User> common = userStorage.findCommonFriends(user.getId(), other.getId());

        assertThat(common)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(commonFriend.getId());
    }

    // Тесты на исключения

    @Test
    @DisplayName("Ошибка при поиске несуществующего пользователя")
    void testFindUserByIdNotFound() {
        Optional<User> userOptional = userStorage.findById(999L);

        assertThat(userOptional).isEmpty();
    }

    @Test
    @DisplayName("Ошибка при добавлении в друзья несуществующего пользователя")
    void testFriendshipWithNonExistentUser() {
        User user1 = userStorage.create(TestDataGenerator.generateUser());

        Friendship friendship = new Friendship(user1.getId(), 9999L);

        assertThrows(
                ru.yandex.practicum.filmorate.exceptions.NotFoundException.class,
                () -> userStorage.sendFriendshipRequest(friendship),
                "Должно быть выброшено NotFoundException из-за нарушения Foreign Key"
        );
    }

    @Test
    @DisplayName("Ошибка при повторной отправке запроса в друзья")
    void testDuplicateFriendshipRequest() {
        User user1 = userStorage.create(TestDataGenerator.generateUser());
        User user2 = userStorage.create(TestDataGenerator.generateUser());
        Friendship friendship = new Friendship(user1.getId(), user2.getId());

        userStorage.sendFriendshipRequest(friendship);

        assertThrows(
                ru.yandex.practicum.filmorate.exceptions.DataConflictException.class,
                () -> userStorage.sendFriendshipRequest(friendship),
                "Должно быть выброшено DataConflictException при дубликате PK"
        );
    }

    @Test
    @DisplayName("Ошибка подтверждения несуществующей дружбы")
    void testConfirmNonExistentFriendship() {
        User user1 = userStorage.create(TestDataGenerator.generateUser());
        User user2 = userStorage.create(TestDataGenerator.generateUser());
        Friendship friendship = new Friendship(user1.getId(), user2.getId());

        assertThrows(
                ru.yandex.practicum.filmorate.exceptions.NotFoundException.class,
                () -> userStorage.confirmFriendship(friendship),
                "Должно быть выброшено NotFoundException, если записи для подтверждения нет"
        );
    }

    @Test
    @DisplayName("Ошибка удаления пользователя, которого нет")
    void testDeleteNonExistentUser() {
        assertThrows(
                ru.yandex.practicum.filmorate.exceptions.NotFoundException.class,
                () -> userStorage.delete(9999L),
                String.format("Метод delete должен выбрасывать NotFoundException для несуществующего ID %d", 9999L)
        );
    }

    @Test
    @DisplayName("Ошибка: создание пользователя с уже занятой почтой")
    void testCreateUserWithDuplicateEmail() {
        User user1 = userStorage.create(TestDataGenerator.generateUser());

        User user2 = TestDataGenerator.generateUser();
        user2.setEmail(user1.getEmail());

        assertThrows(
                DataConflictException.class,
                () -> userStorage.create(user2),
                String.format("Ожидалось исключение DuplicateKeyException при создании пользователя с уже существующей почтой: %s",
                        user1.getEmail())
        );
    }

    @Test
    @DisplayName("Ошибка: обновление почты на уже занятую другим пользователем")
    void testUpdateUserWithDuplicateEmail() {
        User user1 = userStorage.create(TestDataGenerator.generateUser());
        User user2 = userStorage.create(TestDataGenerator.generateUser());

        String duplicateEmail = user1.getEmail();
        user2.setEmail(duplicateEmail);

        assertThrows(
                DataConflictException.class,
                () -> userStorage.update(user2),
                String.format("Ожидалось исключение DuplicateKeyException при обновлении пользователя %d почтой %s, которая уже принадлежит пользователю %d",
                        user2.getId(), duplicateEmail, user1.getId())
        );
    }
}