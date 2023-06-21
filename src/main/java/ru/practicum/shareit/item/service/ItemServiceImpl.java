package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Collection<ItemDto> findAll() {
        return itemRepository.findAll()
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> getUserItems(Long userId) {
        return itemRepository.findAll()
                .stream()
                .filter(item -> Objects.equals(item.getOwner().getId(), userId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

    }

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User owner = userRepository.getUserById(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        log.info("Пользователь с идентификатором {} добавил вещь", owner.getId());
        itemRepository.addItem(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        return ItemMapper.toItemDto(itemRepository.getItemById(itemId));
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item itemForUpdate = itemRepository.getItemById(itemId);

        if (!itemForUpdate.getOwner().getId().equals(userId)) {
            throw new ValidationException("Изменять можно только свои вещи.");
        }

        Optional.ofNullable(itemDto.getName()).ifPresent(itemForUpdate::setName);
        Optional.ofNullable(itemDto.getDescription()).ifPresent(itemForUpdate::setDescription);
        Optional.ofNullable(itemDto.getAvailable()).ifPresent(itemForUpdate::setAvailable);

        log.info("Вещь с идентификатором {} обновлена", userId);
        return ItemMapper.toItemDto(itemForUpdate);
    }

    @Override
    public Collection<ItemDto> searchItem(String word) {
        String wordInLowerCase = word.toLowerCase();
        return itemRepository.findAll().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(wordInLowerCase)
                        || item.getDescription().toLowerCase().contains(wordInLowerCase))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItem(Long itemId) {
        itemRepository.deleteItemById(itemId);
    }
}