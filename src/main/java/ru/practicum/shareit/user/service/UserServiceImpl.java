package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Collection<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto addUser(UserDto userDto) {
        User saved = userRepository.save(UserMapper.toUser(userDto));
        log.info("Пользователь id {} добавлен", saved.getId());
        return UserMapper.toUserDto(saved);
    }

    @Override
    public UserDto getUserById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        User user = userOptional.orElseThrow(() -> new ObjectNotFoundException(String.format("Пользователь id %s не найден", userId)));
        log.info("Пользователь id {} возвращен", userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User userForUpdate = UserMapper.toUser(getUserById(id));
        User newUser = UserMapper.toUser(userDto);

        Optional.ofNullable(newUser.getName()).ifPresent(userForUpdate::setName);
        Optional.ofNullable(newUser.getEmail()).ifPresent(userForUpdate::setEmail);

        userRepository.save(userForUpdate);
        log.info("Пользователь id {} обновлен", id);
        return UserMapper.toUserDto(userForUpdate);
    }

    @Override
    @Transactional
    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }
}