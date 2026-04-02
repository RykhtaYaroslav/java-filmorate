package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.user.UserCreateRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.dto.user.UserUpdateRequest;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@Slf4j
@Validated
public class UserController {
    private final UserService service;

    @Autowired
    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto findById(@PathVariable @Positive Long id) {
        return service.findById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<UserDto> findAll() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        return service.create(userCreateRequest);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public UserDto update(@Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        return service.update(userUpdateRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public Friendship makeFriendship(@PathVariable @Positive Long id, @PathVariable @Positive Long friendId) {
        return service.makeFriendship(id, friendId);
    }

    @PutMapping("/{id}/friends/{friendId}/confirm")
    @ResponseStatus(HttpStatus.OK)
    public Friendship confirmFriendship(@PathVariable @Positive Long id, @PathVariable @Positive Long friendId) {
        return service.confirmFriendship(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFriendship(@PathVariable @Positive Long id, @PathVariable @Positive Long friendId) {
        service.deleteFriendship(id, friendId);
    }

    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public Collection<UserDto> getUserFriends(@PathVariable @Positive Long id) {
        return service.getUserFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public Collection<UserDto> getCommonFriends(@PathVariable @Positive Long id, @PathVariable @Positive Long otherId) {
        return service.getCommonFriends(id, otherId);
    }
}
