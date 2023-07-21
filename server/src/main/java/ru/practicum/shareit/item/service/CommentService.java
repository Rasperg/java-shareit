package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentShortDto;

public interface CommentService {

    CommentDto getCommentById(Long commentId);

    CommentDto addNewComment(CommentShortDto commentShortDto, Long itemId, Long userId);

}
