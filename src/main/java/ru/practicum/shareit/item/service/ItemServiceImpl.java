package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public Collection<ItemDto> findAll() {
        return itemRepository.findAll()
                .stream()
                .map(item -> ItemMapper.toItemDto(item, commentRepository.findByItemOrderByIdAsc(item), bookingRepository.findByItem(item)))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> getUserItems(Long userId) {
        User owner = userRepository.findById(userId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Пользователь id %s не найден", userId)));
        List<Item> userItem = itemRepository.findByOwner(owner);
        return userItem.stream().map(item ->
                        ItemMapper.toItemDto(item, commentRepository.findByItemOrderByIdAsc(item), bookingRepository.findByItem(item)))
                .sorted(this::compareBookingDates).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        Optional<User> userOptional = userRepository.findById(userId);
        User owner = userOptional.orElseThrow(() -> new ObjectNotFoundException(String.format("Пользователь id %s не найден", userId)));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        itemRepository.save(item);
        log.info("Пользователь с id {} добавил новую вещь", owner.getId());
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        Item item = itemOptional.orElseThrow(() -> new ObjectNotFoundException(String.format("Вещь id %s не найдена", itemId)));
        List<Comment> comments = commentRepository.findByItemOrderByIdAsc(item);
        List<Booking> bookings = bookingRepository.findByItem(item);
        if (item.getOwner().getId().equals(userId)) {
            return ItemMapper.toItemDto(item, comments, bookings);
        }
        log.info("Вещь с id {} получена", itemId);
        return ItemMapper.toItemDto(item, comments);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        Item itemForUpdate = itemOptional.orElseThrow(() -> new ObjectNotFoundException(String.format("Вещь id %s не найдена", itemId)));
        if (!itemForUpdate.getOwner().getId().equals(userId)) {
            throw new ValidationException("Обновить вещь может только её хозяин!");
        }
        Optional.ofNullable(itemDto.getName()).ifPresent(itemForUpdate::setName);
        Optional.ofNullable(itemDto.getDescription()).ifPresent(itemForUpdate::setDescription);
        Optional.ofNullable(itemDto.getAvailable()).ifPresent(itemForUpdate::setAvailable);

        log.info("Вещь id {} обновлена", itemId);
        itemRepository.save(itemForUpdate);
        return ItemMapper.toItemDto(itemForUpdate);
    }

    @Override
    public Collection<ItemDto> searchItem(String word) {
        return itemRepository.search(word).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    private int compareBookingDates(ItemDto itemDto1, ItemDto itemDto2) {
        if (itemDto1.getNextBooking() == null && itemDto2.getNextBooking() == null) return 0;
        if (itemDto1.getNextBooking() == null) return 1;
        if (itemDto2.getNextBooking() == null) return -1;
        return -itemDto1.getNextBooking().getStart().compareTo(itemDto2.getNextBooking().getStart());
    }
}