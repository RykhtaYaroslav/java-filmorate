package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Тестирование UserController")
class UserControllerTest {

    private final UserController userController = new UserController();

    @BeforeEach
    void setUp() {
        // Очищаем состояние контроллера перед каждым тестом
        userController.clearData();
    }

    @Test
    @DisplayName("Создание корректного пользователя должно возвращать пользователя с ID")
    void create_ValidUser_ReturnsUserWithId() {
        User user = User.builder()
                .email("test@example.com")
                .login("login")
                .name("Имя")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userController.create(user);

        assertNotNull(createdUser.getId());
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals("login", createdUser.getLogin());
        assertEquals("Имя", createdUser.getName());
        assertEquals(1, userController.findAll().size());
    }

    @Test
    @DisplayName("Обновление существующего пользователя должно сохранять изменения")
    void update_ExistingUser_UpdatesUser() {
        // Сначала создаем пользователя
        User user = User.builder()
                .email("old@example.com")
                .login("oldlogin")
                .name("Старое имя")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User createdUser = userController.create(user);
        Long userId = createdUser.getId();

        // Обновляем пользователя
        User updatedUser = User.builder()
                .id(userId)
                .email("new@example.com")
                .login("newlogin")
                .name("Новое имя")
                .birthday(LocalDate.of(1991, 2, 2))
                .build();

        User result = userController.update(updatedUser);

        assertEquals(userId, result.getId());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("newlogin", result.getLogin());
        assertEquals("Новое имя", result.getName());
    }

    @Test
    @DisplayName("Обновление пользователя без ID должно выбрасывать ConditionsNotMetException")
    void update_UserWithoutId_ThrowsConditionsNotMetException() {
        User user = User.builder()
                .email("test@example.com")
                .login("login")
                .name("Имя")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(ConditionsNotMetException.class, () -> userController.update(user));
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя должно выбрасывать NotFoundException")
    void update_NonExistentUser_ThrowsNotFoundException() {
        User user = User.builder()
                .id(999L)
                .email("test@example.com")
                .login("login")
                .name("Имя")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(NotFoundException.class, () -> userController.update(user));
    }

    @Test
    @DisplayName("Пользователь с null именем должен получать логин в качестве имени при создании")
    void create_UserWithNullName_SetsLoginAsName() {
        User user = User.builder()
                .email("test@example.com")
                .login("login")
                .name(null)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userController.create(user);

        assertEquals("login", createdUser.getName());
    }

    @Test
    @DisplayName("Получение всех пользователей должно возвращать пустой список при отсутствии пользователей")
    void findAll_NoUsers_ReturnsEmptyList() {
        assertTrue(userController.findAll().isEmpty());
    }
}