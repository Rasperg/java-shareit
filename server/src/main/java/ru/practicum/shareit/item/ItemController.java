package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

@RestController
@Validated
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final CommentService commentService;

    @PostMapping
    public ItemDto createNewItem(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                 @RequestBody ItemDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") final Long userId,
                              @RequestBody final ItemDto itemDto,
                              @PathVariable final Long itemId) {
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItem(@PathVariable final Long itemId,
                                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok(itemService.getItemById(itemId, userId));
    }

    @GetMapping
    public Collection<ItemDto> getUserItems(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                            @RequestParam(value = "from", required = false, defaultValue = "0") final Integer from,
                                            @RequestParam(value = "size", required = false, defaultValue = "10") final Integer size) {
        return itemService.getUserItems(userId, from, size);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItem(@RequestParam("text") final String text,
                                          @RequestParam(value = "from", required = false, defaultValue = "0") final Integer from,
                                          @RequestParam(value = "size", required = false, defaultValue = "10") final Integer size) {
        return itemService.searchItem(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createItemComment(@RequestBody final CommentShortDto commentShortDto,
                                        @PathVariable final Long itemId,
                                        @RequestHeader("X-Sharer-User-Id") Long userId) {
        return commentService.addNewComment(commentShortDto, itemId, userId);
    }
}
