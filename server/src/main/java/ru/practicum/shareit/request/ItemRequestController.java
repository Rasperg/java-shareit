package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemNewRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto createNewRequest(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                           @RequestBody ItemNewRequestDto itemNewRequestDto) {
        return requestService.addNewRequest(itemNewRequestDto, userId);
    }

    @GetMapping
    public Collection<ItemRequestDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") final Long userId) {
        return requestService.getUserRequests(userId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                                     @RequestParam(value = "from", defaultValue = "0") final Integer from,
                                                     @RequestParam(value = "size", defaultValue = "10") final Integer size) {
        return requestService.getAllRequestsForAllUsers(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                         @PathVariable final Long requestId) {
        return requestService.getRequestById(userId, requestId);
    }
}
