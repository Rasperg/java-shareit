package ru.practicum.shareit.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class JpaItemRepositoryTest {
    private final User owner = User.builder().name("userName").email("user@mail.ru").build();
    private final Item item = Item.builder().name("Отвертка").description("Аккумуляторная отвертка").available(true)
            .owner(owner).build();
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository requestRepository;

    @BeforeEach
    void addData() {
        userRepository.save(owner);
        itemRepository.save(item);
    }

    @AfterEach
    void deleteData() {
        requestRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void search() {
        List<Item> actualItems = itemRepository.search("аккумуляторная", PageRequest.of(0, 10));

        assertFalse(actualItems.isEmpty());
        assertEquals(1, actualItems.size());

        Item actualItem = actualItems.get(0);
        assertEquals("Отвертка", actualItem.getName());
        assertEquals("Аккумуляторная отвертка", actualItem.getDescription());
        assertTrue(actualItem.getAvailable());
        assertNotNull(actualItem.getOwner());
        assertEquals("userName", actualItem.getOwner().getName());
        assertEquals("user@mail.ru", actualItem.getOwner().getEmail());
    }

    @Test
    void findByOwner() {
        List<Item> actualItems = itemRepository.findByOwner(owner, PageRequest.of(0, 10));

        assertFalse(actualItems.isEmpty());
        assertEquals(1, actualItems.size());

        Item actualItem = actualItems.get(0);
        assertEquals("Отвертка", actualItem.getName());
        assertEquals("Аккумуляторная отвертка", actualItem.getDescription());
        assertTrue(actualItem.getAvailable());
        assertNotNull(actualItem.getOwner());
        assertEquals("userName", actualItem.getOwner().getName());
        assertEquals("user@mail.ru", actualItem.getOwner().getEmail());
    }

    @Test
    void findAllByRequestIdIn() {
        User requestor = User.builder().name("requestor").email("req@mail.ru").build();
        userRepository.save(requestor);

        ItemRequest request = ItemRequest.builder().description("нужен шуруповерт").requestor(requestor).build();
        requestRepository.save(request);

        Item itemWithRequest = Item.builder().name("Шуруповерт").description("Обычный шуруповерт").available(true)
                .owner(owner).requestId(request.getId()).build();
        itemRepository.save(itemWithRequest);

        List<Item> actualItems = itemRepository.findAllByRequestIdIn(List.of(request.getId()));

        assertFalse(actualItems.isEmpty());
        assertEquals(1, actualItems.size());

        Item actualItem = actualItems.get(0);
        assertEquals("Шуруповерт", actualItem.getName());
        assertEquals("Обычный шуруповерт", actualItem.getDescription());
        assertTrue(actualItem.getAvailable());
        assertNotNull(actualItem.getOwner());
        assertEquals("userName", actualItem.getOwner().getName());
        assertEquals("user@mail.ru", actualItem.getOwner().getEmail());
        assertEquals(request.getId(), actualItem.getRequestId());
    }
}
