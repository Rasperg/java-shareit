package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    private final UserDto userDto = UserDto.builder().id(1L).name("testUser").email("user@email.ru").build();
    private final UserDto userSecondDto = UserDto.builder().id(2L).name("testUser2").email("user2@email.ru").build();
    private final List<UserDto> usersDto = List.of(userDto, userSecondDto);
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;

    @Test
    void getAllUsers() throws Exception {
        when(userService.findAll()).thenReturn(new ArrayList<>(usersDto));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(usersDto.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(usersDto.get(0).getName())))
                .andExpect(jsonPath("$[0].email", is(usersDto.get(0).getEmail())));

        verify(userService).findAll();
    }

    @Test
    void createNewUser() throws Exception {
        when(userService.addUser(any())).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue(), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService).addUser(userDto);
    }

    @Test
    void createNewUserWithInvalidNameOrEmail() throws Exception {
        UserDto userWithoutName = UserDto.builder().id(1L).name(null).email("user@name.ru").build();
        UserDto userWithBlankEmail = UserDto.builder().id(1L).name("userName").email("").build();

        when(userService.addUser(any(UserDto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userWithoutName))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).addUser(userWithoutName);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userWithBlankEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).addUser(userWithBlankEmail);

    }

    @Test
    void updateUser() throws Exception {
        UserDto userWithUpdatedName = UserDto.builder().id(1L).name("nameUpdated").build();
        UserDto userWithUpdatedEmail = UserDto.builder().id(2L).email("updated@email.ru").build();

        when(userService.updateUser(any(), any())).thenReturn(usersDto.get(0));

        mockMvc.perform(patch("/users/1")
                        .content(objectMapper.writeValueAsString(userWithUpdatedName))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue(), Long.class))
                .andExpect(jsonPath("$.name", notNullValue()))
                .andExpect(jsonPath("$.email", notNullValue()));

        verify(userService).updateUser(1L, userWithUpdatedName);

        mockMvc.perform(patch("/users/2")
                        .content(objectMapper.writeValueAsString(userWithUpdatedEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue(), Long.class))
                .andExpect(jsonPath("$.name", notNullValue()))
                .andExpect(jsonPath("$.email", notNullValue()));

        verify(userService).updateUser(2L, userWithUpdatedEmail);
    }

    @Test
    void updateUserWithInvalidEmail() throws Exception {
        UserDto userWithInvalidEmail = UserDto.builder().name("testUser").email("email.email").build();

        mockMvc.perform(patch("/users/1")
                        .content(objectMapper.writeValueAsString(userWithInvalidEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(1L, userWithInvalidEmail);
    }

    @Test
    void getUserById() throws Exception {
        int userIndex = 0;
        when(userService.getUserById(anyLong())).thenReturn(usersDto.get(userIndex));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(usersDto.get(userIndex).getId()), Long.class))
                .andExpect(jsonPath("$.name", is(usersDto.get(userIndex).getName())))
                .andExpect(jsonPath("$.email", is(usersDto.get(userIndex).getEmail())));
    }

    @Test
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}