package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {

    List<User> findAll();

    User addUser(User user);

    User getUserById(Long userId);

    User updateUserById(Long userId, User user);

    void deleteUserById(Long userId);
}