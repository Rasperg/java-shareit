package ru.practicum.shareit.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class JpaCommentRepositoryTest {
    private final User owner = User.builder().name("userName").email("user@mail.ru").build();
    private final User commentator = User.builder().name("user2Name").email("user2@mail.ru").build();
    private final Item item = Item.builder().name("Отвертка").description("Аккумуляторная отвертка").available(true)
            .owner(owner).build();
    private final Comment comment = Comment.builder().text("test comment").created(LocalDateTime.now()).item(item)
            .author(commentator).build();
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void addData() {
        userRepository.save(owner);
        userRepository.save(commentator);
        itemRepository.save(item);
        commentRepository.save(comment);
    }

    @AfterEach
    void deleteData() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        commentRepository.deleteAll();
    }

    @Test
    void findByItemOrderByIdAsc() {
        List<Comment> actualComments = commentRepository.findByItemOrderByIdAsc(item);

        assertFalse(actualComments.isEmpty());
        assertEquals(1, actualComments.size());

        Comment actualComment = actualComments.get(0);
        assertEquals("test comment", actualComment.getText());
        assertEquals("Отвертка", actualComment.getItem().getName());
        assertEquals("user2Name", actualComment.getAuthor().getName());
        assertNotNull(actualComment.getCreated());
    }
}