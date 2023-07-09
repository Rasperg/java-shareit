package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    private final UserDto userDto = UserDto.builder().id(1L).name("UserName").email("user@email.ru").build();
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testCreateUser() {
        when(userRepository.save(any()))
                .thenReturn(UserMapper.toUser(userDto));
        UserDto savedUser = userService.addUser(userDto);

        assertEquals(savedUser, userDto);
        verify(userRepository).save(any());
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(UserMapper.toUser(userDto)));
        UserDto dto = userService.getUserById(anyLong());

        assertEquals(userDto, dto);
    }

    @Test
    void testGetByIdWithIncorrectParameter() {
        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> userService.getUserById(100L));

        verify(userRepository).findById(100L);
    }

    @Test
    void testFindAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(UserMapper.toUser(userDto)));

        Collection<UserDto> allDtoUsers = userService.findAll();

        assertFalse(allDtoUsers.isEmpty());
        assertEquals(1, allDtoUsers.size());
    }

    @Test
    void testFindAllUsersWithEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        Collection<UserDto> allDtoUsers = userService.findAll();

        assertTrue(allDtoUsers.isEmpty());
    }

    @Test
    void testUpdateUser() {
        userDto.setName("NameUpdated");
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(UserMapper.toUser(userDto)));

        UserDto updatedUserDto = userService.updateUser(userDto.getId(), userDto);

        assertEquals("NameUpdated", updatedUserDto.getName());
        assertEquals(userDto.getName(), updatedUserDto.getName());
        assertEquals(userDto.getEmail(), updatedUserDto.getEmail());
    }

    @Test
    void testUpdatedNotFoundUser() {
        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> userService.updateUser(100L, userDto));

        verify(userRepository).findById(100L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUser() {
        userService.deleteUserById(1L);

        verify(userRepository, times(1)).deleteById(1L);
        assertThrows(ObjectNotFoundException.class, () -> userService.getUserById(1L));
    }
}
