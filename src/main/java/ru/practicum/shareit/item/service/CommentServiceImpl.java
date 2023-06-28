package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentShortDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.repository.CommentRepository;

import java.time.LocalDateTime;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentDto getCommentById(Long commentId) {
        return CommentMapper.toCommentDto(commentRepository.findById(commentId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Комментарий id %s не найден", commentId))));
    }

    @Override
    @Transactional
    public CommentDto addNewComment(CommentShortDto commentDto, Long itemId, Long userId) {
        Comment comment = commentMapper.toComment(commentDto, itemId, userId);
        if (!bookingRepository.existsBookingByItemAndBookerAndStatusNotAndStart(comment.getItem(),
                comment.getAuthor(), LocalDateTime.now())) {
            throw new BadRequestException("Нельзя оставить комменатрий если вещь не была или не находится в аренде");
        }
        comment.setCreated(LocalDateTime.now());
        log.info("Пользователь id {} добавил комментарий id {} к вещи id {}",
                comment.getAuthor().getId(), comment.getId(), comment.getItem().getId());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }
}