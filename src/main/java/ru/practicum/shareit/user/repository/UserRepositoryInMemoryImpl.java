package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
@Slf4j
public class UserRepositoryInMemoryImpl implements UserRepository {
    Map<Long, User> users = new HashMap<>();
    private long generatorId = 0;

    @Override
    public List<User> findAll() {
        log.info("Возращено " + users.size() + " пользователей.");
        return new ArrayList<>(users.values());
    }

    @Override
    public User addUser(User user) {
        checkUserEmail(user.getEmail());
        user.setId(++generatorId);
        users.put(user.getId(), user);
        log.info(String.format("Пользователь с идентификатором %d добавлен", user.getId()));
        return user;
    }

    @Override
    public User getUserById(Long userId) {
        if (!users.containsKey(userId)) {
            log.error(String.format("Пользователь с идентификатором %d не найден", userId));
            throw new ObjectNotFoundException(String.format("Пользователь не найден", userId));
        }
        log.info(String.format("Пользователь с идентификатором %d возвращен", userId));
        return users.get(userId);
    }

    @Override
    public User updateUserById(Long userId, User user) {
        User userForUpdate = getUserById(userId);

        Optional.ofNullable(user.getName()).ifPresent(userForUpdate::setName);
        Optional.ofNullable(user.getEmail()).ifPresent(email -> {
            if (!email.equals(userForUpdate.getEmail())) {
                checkUserEmail(email);
            }
            userForUpdate.setEmail(email);
        });
        log.info(String.format("Пользователь с идентификатором %d обновлен", userId));
        return userForUpdate;
    }

    @Override
    public void deleteUserById(Long userId) {
        var user = users.remove(userId);
        if (user == null) {
            log.warn(String.format("Пользователь с идентификатором %d не найден", userId));
        } else {
            log.info(String.format("Пользователь с идентификатором %d удален", userId));
        }
    }

    private void checkUserEmail(String email) {
        if (users.values().stream().anyMatch(user -> user.getEmail().equals(email))) {
            throw new EmailAlreadyExistsException("Указанный адрес уже существует!");
        }
    }
}