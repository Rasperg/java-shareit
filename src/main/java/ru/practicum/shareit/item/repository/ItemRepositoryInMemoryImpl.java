package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class ItemRepositoryInMemoryImpl implements ItemRepository {

    Map<Long, Item> items = new HashMap<>();
    private long generatorId = 0;

    @Override
    public List<Item> findAll() {
        log.info("Возращено " + items.size() + " вещей");
        return new ArrayList<>(items.values());
    }

    @Override
    public Item addItem(Item item) {
        if (!items.containsKey(item.getId())) {
            item.setId(++generatorId);
        }
        items.put(item.getId(), item);
        log.info(String.format("Вещь с идентификатором %d добавлена", item.getId()));
        return item;
    }

    @Override
    public Item getItemById(Long itemId) {
        if (!items.containsKey(itemId)) {
            log.error(String.format("Вещь с идентификатором %d не найдена", itemId));
            throw new ObjectNotFoundException("Вещь не найдена");
        }
        log.info(String.format("Вещь с идентификатором %d возвращена", itemId));
        return items.get(itemId);
    }

    @Override
    public void deleteItemById(Long id) {
        var item = items.remove(id);
        if (item == null) {
            log.warn(String.format("Вещь с идентификатором %d не найдена", id));
        } else {
            log.info(String.format("Вещь с идентификатором %d удалена", id));
        }
    }
}