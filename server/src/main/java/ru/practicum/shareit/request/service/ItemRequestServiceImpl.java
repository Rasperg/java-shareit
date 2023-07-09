package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemNewRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    @Transactional
    public ItemRequestDto addNewRequest(ItemNewRequestDto dtoFromUser, Long userId) {
        User requestor = userRepository.findById(userId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Пользователь id %s не найден", userId)));
        ItemRequest request = ItemRequestMapper.toItemRequest(dtoFromUser);
        request.setRequestor(requestor);
        requestRepository.save(request);
        request.setCreated(LocalDateTime.now().plusHours(5));
        log.info("Пользователь id {} добавил запрос id {}", requestor.getId(), request.getId());
        return ItemRequestMapper.toItemRequestDto(request);
    }

    @Override
    public Collection<ItemRequestDto> getUserRequests(Long userId) {
        User requestor = userRepository.findById(userId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Пользователь id %s не найден", userId)));
        List<ItemRequest> requests = requestRepository.findAllByRequestorOrderByCreated(requestor);
        fillItemsByRequests(requests);
        log.info("Запросы пользователя id {} получены", userId);
        return ItemRequestMapper.toItemRequestDtoListWithItems(requests);
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userRepository.findById(userId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Пользователь id %s не найден", userId)));
        ItemRequest request = requestRepository.findById(requestId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Запрос id %s не найден", requestId)));
        fillItemsByRequests(List.of(request));
        log.info("Запрос id {} получен пользователем id {}", requestId, userId);
        return ItemRequestMapper.toItemRequestDto(request);
    }

    @Override
    public Collection<ItemRequestDto> getAllRequestsForAllUsers(Long userId, Integer from, Integer size) {
        User requestor = userRepository.findById(userId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Пользователь id %s не найден", userId)));
        PageRequest page = PageRequest.of(from / size, size, Sort.by("created").descending());
        List<ItemRequest> requests = requestRepository.findAll(page).stream()
                .filter(request -> !request.getRequestor().equals(requestor))
                .collect(Collectors.toList());
        fillItemsByRequests(requests);
        log.info("Список всех запросов получен пользователем id {}", userId);
        return ItemRequestMapper.toItemRequestDtoListWithItems(requests);
    }

    private void fillItemsByRequests(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());
        Map<Long, Set<Item>> items = itemRepository.findAllByRequestIdIn(requestIds).stream()
                .collect(groupingBy(Item::getRequestId, toSet()));

        requests.forEach(itemRequest -> itemRequest.setItems(items.getOrDefault(itemRequest.getId(), Collections.emptySet())));
    }
}
