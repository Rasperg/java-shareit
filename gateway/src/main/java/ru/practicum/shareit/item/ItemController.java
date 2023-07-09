package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validate.Create;
import ru.practicum.shareit.validate.Update;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collections;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createNewItem(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                                @RequestBody @Validated(Create.class) ItemDto itemDto) {
        log.info("Пользователь id {} создал вещь {}", userId, itemDto.getName());
        return itemClient.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                             @Validated(Update.class) @RequestBody final ItemDto itemDto,
                                             @PathVariable final Long itemId) {
        log.info("Пользователь id {} обновил вещь id {}", userId, itemId);
        return itemClient.updateItem(userId, itemDto, itemId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@PathVariable final Long itemId,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Пользователем id {} получена вещь id {}", userId, itemId);
        return itemClient.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserItems(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                               @RequestParam(value = "from", required = false, defaultValue = "0")
                                               @PositiveOrZero(message = "Значение 'from' должно быть положительным") final Integer from,
                                               @RequestParam(value = "size", required = false, defaultValue = "10")
                                               @Positive(message = "Значение 'size' должно быть положительным") final Integer size) {
        log.info("Получены вещи пользотваеля id {}", userId);
        return itemClient.getUserItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                             @RequestParam("text") final String text,
                                             @RequestParam(value = "from", required = false, defaultValue = "0")
                                             @PositiveOrZero(message = "Значение 'from' должно быть положительным") final Integer from,
                                             @RequestParam(value = "size", required = false, defaultValue = "10")
                                             @Positive(message = "Значение 'size' должно быть положительным") final Integer size) {
        if (text == null || text.isBlank()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        log.info("Пользователь id {} выполнил поиск вещи {}", userId, text);
        return itemClient.searchItem(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createItemComment(@RequestBody @Valid final CommentShortDto commentShortDto,
                                                    @PathVariable final Long itemId,
                                                    @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Пользователь id {} добавил комментарий к вещи id {}", userId, itemId);
        return itemClient.createItemComment(commentShortDto, itemId, userId);
    }
}
