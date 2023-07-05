package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IntItemRequestServiceImplTest {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final ItemRequestRepository requestRepository;
    @Autowired
    private final ItemRequestServiceImpl requestService;
    private final User requestor = User.builder().name("user2").email("user2@mail.ru").build();
    private final ItemRequest request = ItemRequest.builder().description("Нужна отвертка").requestor(requestor)
            .created(LocalDateTime.now()).build();

    @BeforeEach
    void setUp() {
        userRepository.save(requestor);
        requestRepository.save(request);
    }

    @AfterEach
    void deleteData() {
        userRepository.deleteAll();
        requestRepository.deleteAll();
    }

    @Test
    void testGetUserRequests() {
        Collection<ItemRequestDto> actualRequests = requestService.getUserRequests(requestor.getId());

        assertFalse(actualRequests.isEmpty());
        assertEquals(1, actualRequests.size());

        List<ItemRequestDto> requests = new ArrayList<>(actualRequests);

        ItemRequestDto actualRequest = requests.get(0);
        assertEquals("Нужна отвертка", actualRequest.getDescription());
        assertEquals(requestor.getId(), actualRequest.getRequestorId());
        assertNotNull(actualRequest.getCreated());
    }

    @Test
    void testGetAllRequestsForAllUsers() {
        User user = User.builder().name("user").email("user@mail.ru").build();
        userRepository.save(user);
        Collection<ItemRequestDto> actualRequests = requestService.getAllRequestsForAllUsers(user.getId(), 0, 10);

        assertFalse(actualRequests.isEmpty());
        assertEquals(1, actualRequests.size());

        List<ItemRequestDto> requests = new ArrayList<>(actualRequests);

        ItemRequestDto actualRequest = requests.get(0);
        assertEquals("Нужна отвертка", actualRequest.getDescription());
        assertEquals(requestor.getId(), actualRequest.getRequestorId());
        assertNotNull(actualRequest.getCreated());
    }
}