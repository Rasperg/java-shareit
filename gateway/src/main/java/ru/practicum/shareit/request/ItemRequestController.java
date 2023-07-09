package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;


@RestController
@Validated
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> createNewRequest(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                                   @RequestBody @Valid ItemRequestDto itemRequestDto) {
        log.info("Создан новый запрос с описанием {}", itemRequestDto.getDescription());
        return requestClient.addNewRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(@RequestHeader("X-Sharer-User-Id") final Long userId) {
        log.info("Получены запросы пользователя id {}", userId);
        return requestClient.getUserRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                                 @RequestParam(value = "from", defaultValue = "0")
                                                 @PositiveOrZero(message = "Значение 'from' должно быть положительным")
                                                 final Integer from,
                                                 @RequestParam(value = "size", defaultValue = "10")
                                                     @Positive(message = "Значение 'size' должно быть положительным")
                                                     final Integer size) {
        log.info("Получены все запросы пользователем id {}", userId);
        return requestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") final Long userId,
                                                 @PathVariable final Long requestId) {
        log.info("Получен запрос id {} пользователем id {}", requestId, userId);
        return requestClient.getRequestById(userId, requestId);
    }
}
