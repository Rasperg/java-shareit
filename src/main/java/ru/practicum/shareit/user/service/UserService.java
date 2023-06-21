package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    Collection<UserDto> findAll();

    UserDto addUser(UserDto userDto);

    UserDto getUserById(Long userId);

    UserDto updateUser(Long id, UserDto userDto);

    void deleteUserById(Long userId);
}