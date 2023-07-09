package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentShortDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class CommentServiceImplTest {
    private final User user = new User(1L, "user", "user@mail.ru");
    private final Item item = Item.builder().id(1L).name("item2Name").description("item2Desc").available(true).owner(user)
            .requestId(1L).build();
    private final CommentShortDto commentShortDto = CommentShortDto.builder().id(1L).text("comment1").itemId(item.getId())
            .authorName(user.getName()).created(LocalDateTime.now()).build();
    private final Comment comment = Comment.builder().id(1L).text("comment1").item(item).author(user)
            .created(LocalDateTime.now()).build();
    private final CommentDto commentDto = CommentDto.builder().id(1L).text("comment1").item(ItemMapper.toItemDto(item))
            .authorName(user.getName()).created(comment.getCreated()).build();
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    public void testAddNewComment() {
        when(bookingRepository.existsBookingByItemAndBookerAndStatusNotAndStart(any(), any(), any())).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.save(any())).thenReturn(comment);

        CommentDto addedComment = commentService.addNewComment(commentShortDto, item.getId(), user.getId());

        assertEquals(comment.getText(), addedComment.getText());
        assertEquals(comment.getId(), addedComment.getId());
        assertEquals(comment.getAuthor().getName(), addedComment.getAuthorName());
        assertEquals(comment.getItem().getId(), addedComment.getItem().getId());

        verify(bookingRepository).existsBookingByItemAndBookerAndStatusNotAndStart(eq(item), eq(user), any());
        verify(commentRepository).save(any());
    }

    @Test
    public void testAddNewCommentWhenItemNotFound() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> commentService.addNewComment(commentShortDto, 100L, user.getId()));

        verify(itemRepository).findById(100L);
        verify(commentRepository, never()).save(any());
    }

    @Test
    public void testAddNewCommentWhenUserNotFound() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> commentService.addNewComment(commentShortDto, item.getId(), 100L));

        verify(userRepository).findById(100L);
        verify(commentRepository, never()).save(any());
    }

    @Test
    public void testAddNewCommentWithWrongBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.existsBookingByItemAndBookerAndStatusNotAndStart(any(), any(), any())).thenReturn(false);

        assertThrows(BadRequestException.class, () -> commentService.addNewComment(commentShortDto, item.getId(), user.getId()));

        verify(bookingRepository).existsBookingByItemAndBookerAndStatusNotAndStart(eq(item), eq(user), any());
        verify(commentRepository, never()).save(comment);
    }

    @Test
    public void testGetCommentById() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        CommentDto actualCommentDto = commentService.getCommentById(comment.getId());

        assertEquals(actualCommentDto, commentDto);
    }

    @Test
    public void testGetCommentWithWrongId() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> commentService.getCommentById(comment.getId()));
    }

    @Test
    void testCommentShortDto() {
        CommentShortDto convertedCommentDto = CommentMapper.toCommentShortDto(comment);

        assertNotNull(convertedCommentDto.getCreated());
        assertEquals(comment.getText(), convertedCommentDto.getText());
        assertEquals(comment.getAuthor().getName(), convertedCommentDto.getAuthorName());
        assertEquals(comment.getItem().getId(), convertedCommentDto.getItemId());
    }
}
