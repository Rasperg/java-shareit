package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
@Slf4j
public class ItemRepositoryInMemoryImpl implements ItemRepository {

    Map<Long, Item> items = new HashMap<>();
    private long generatorId = 0;

    @Override
    public Collection<Item> findAll() {
        log.info("Возращено {} вещей.", items.size());
        return items.values();
    }

    @Override
    public Item addItem(Item item) {
        if (!items.containsKey(item.getId())) {
            item.setId(++generatorId);
        } else throw new ValidationException("Элемент уже существует");
        items.put(item.getId(), item);
        log.info("Вещь с идентификатором {} добавлена", item.getId());
        return item;
    }

    @Override
    public Item getItemById(Long itemId) {
        if (!items.containsKey(itemId)) {
            log.error("Вещь с идентификатором {} не найдена", itemId);
            throw new ObjectNotFoundException("Вещь не найдена");
        }
        log.info("Вещь с идентификатором {} возвращена", itemId);
        return items.get(itemId);
    }

    @Override
    public void deleteItemById(Long id) {
        var item = items.remove(id);
        if (item == null) {
            log.warn("Вещь с идентификатором {} не найдена", id);
        } else {
            log.info("Вещь с идентификатором {} удалена", id);
        }
    }
}