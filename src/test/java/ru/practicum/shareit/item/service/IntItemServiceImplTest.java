package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IntItemServiceImplTest {

    @Autowired
    private final ItemRepository itemRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final ItemServiceImpl itemService;
    private final User user = User.builder().name("user").email("user@mail.ru").build();
    private final Item item = Item.builder().name("itemName").description("item1Desc").available(true).owner(user).build();
    private final Item secondItem = Item.builder().name("item2Name").description("item2Desc").available(true).owner(user).build();

    @BeforeEach
    void setUp() {
        userRepository.save(user);
        itemRepository.save(item);
        itemRepository.save(secondItem);
    }

    @Test
    void testGetUserItems() {
        Collection<ItemDto> userItems = itemService.getUserItems(user.getId(), 0, 10);

        assertNotNull(userItems);
        assertEquals(2, userItems.size());

        ItemDto firstItem = userItems.stream().findFirst().orElse(null);
        assertNotNull(firstItem);
        assertNotNull(firstItem.getId());
        assertNotNull(firstItem.getName());
        assertNotNull(firstItem.getDescription());
        assertNotNull(firstItem.getAvailable());

        ItemDto secondItem = userItems.stream().skip(1).findFirst().orElse(null);
        assertNotNull(secondItem);
        assertNotNull(secondItem.getId());
        assertNotNull(secondItem.getName());
        assertNotNull(secondItem.getDescription());
        assertNotNull(secondItem.getAvailable());
    }
}