package ru.practicum.shareit.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class JpaItemRequestRepositoryTest {
    private final User owner = User.builder().name("userName").email("user@mail.ru").build();
    private final User requestor = User.builder().name("user2Name").email("user2@mail.ru").build();
    private final ItemRequest request = ItemRequest.builder().description("Нужна отвертка").requestor(requestor)
            .created(LocalDateTime.now()).build();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository requestRepository;

    @BeforeEach
    void addData() {
        userRepository.save(owner);
        userRepository.save(requestor);
        requestRepository.save(request);
    }

    @AfterEach
    void deleteData() {
        userRepository.deleteAll();
        requestRepository.deleteAll();
    }

    @Test
    void findAllByRequestorOrderByCreated() {
        List<ItemRequest> actualRequests = requestRepository.findAllByRequestorOrderByCreated(requestor);

        assertFalse(actualRequests.isEmpty());
        assertEquals(1, actualRequests.size());

        ItemRequest actualRequest = actualRequests.get(0);
        assertEquals("Нужна отвертка", actualRequest.getDescription());
        assertEquals(requestor, actualRequest.getRequestor());
        assertNotNull(actualRequest.getCreated());
    }
}