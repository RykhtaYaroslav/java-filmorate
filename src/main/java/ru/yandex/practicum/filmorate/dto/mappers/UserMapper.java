package ru.yandex.practicum.filmorate.dto.mappers;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.dto.user.UserCreateRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.dto.user.UserUpdateRequest;
import ru.yandex.practicum.filmorate.model.User;

@UtilityClass
public final class UserMapper {

    public User mapToUser(UserCreateRequest request) {
        return User.builder()
                .email(request.getEmail())
                .login(request.getLogin())
                .name((request.getName() == null || request.getName().isBlank()) ? request.getLogin() : request.getName())
                .birthday(request.getBirthday())
                .build();
    }

    public User mapToUser(UserUpdateRequest request) {
        return User.builder()
                .id(request.getId())
                .email(request.getEmail())
                .login(request.getLogin())
                .name(request.getName())
                .birthday(request.getBirthday())
                .build();
    }

    public UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setBirthday(user.getBirthday());

        return dto;
    }
}