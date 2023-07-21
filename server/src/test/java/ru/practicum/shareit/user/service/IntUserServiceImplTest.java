package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IntUserServiceImplTest {
    private final User user = User.builder().name("User").email("user@mail.ru").build();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository.save(user);
    }

    @Test
    void testUpdateUser() {
        UserDto dtoForUserUpdate = UserDto.builder().name("UPD name").email("upd@mail.ru").build();

        UserDto updatedUser = userService.updateUser(user.getId(), dtoForUserUpdate);

        assertEquals(user.getName(), updatedUser.getName());
        assertEquals(user.getEmail(), updatedUser.getEmail());
    }
}
