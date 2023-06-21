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
    public Collection<User> findAll() {
        log.info("Возращено {} пользователей.", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public User addUser(User user) {
        checkUserEmail(user.getEmail());
        user.setId(++generatorId);
        users.put(user.getId(), user);
        log.info("Пользователь с идентификатором {} добавлен", user.getId());
        return user;
    }

    @Override
    public User getUserById(Long userId) {
        if (!users.containsKey(userId)) {
            log.error("Пользователь с идентификатором {} не найден", userId);
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        log.info("Пользователь с идентификатором {} возвращен", userId);
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
        log.info("Пользователь с идентификатором {} обновлен", userId);
        return userForUpdate;
    }

    @Override
    public void deleteUserById(Long userId) {
        var user = users.remove(userId);
        if (user == null) {
            log.warn("Пользователь с идентификатором {} не найден", userId);
        } else {
            log.info("Пользователь с идентификатором {} удален", userId);
        }
    }

    private void checkUserEmail(String email) {
        if (users.values().stream().anyMatch(user -> user.getEmail().equals(email))) {
            throw new EmailAlreadyExistsException("Указанный адрес уже существует!");
        }
    }
}