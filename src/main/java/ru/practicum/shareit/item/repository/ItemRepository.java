package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    List<Item> findAll();

    Item addItem(Item item);

    Item getItemById(Long id);

    void deleteItemById(Long id);
}