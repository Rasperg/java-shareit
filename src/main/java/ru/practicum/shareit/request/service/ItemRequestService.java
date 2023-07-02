package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {

    ItemRequestDto addNewRequest(ItemRequestDto itemRequestDto, Long userId);

    Collection<ItemRequestDto> getUserRequests(Long userId);

    ItemRequestDto getRequestById(Long userId, Long requestId);

    Collection<ItemRequestDto> getAllRequestsForAllUsers(Long userId, Integer from, Integer size);
}
