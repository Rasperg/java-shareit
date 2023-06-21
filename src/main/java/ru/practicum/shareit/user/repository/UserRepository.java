package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {

    Collection<User> findAll();

    User addUser(User user);

    User getUserById(Long userId);

    User updateUserById(Long userId, User user);

    void deleteUserById(Long userId);
}