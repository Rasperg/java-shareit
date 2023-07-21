package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {

    Collection<ItemDto> getUserItems(Long userId, Integer from, Integer size);

    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto getItemById(Long itemId, Long userId);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    Collection<ItemDto> searchItem(String word, Integer from, Integer size);

    void deleteItem(Long itemId);
}
