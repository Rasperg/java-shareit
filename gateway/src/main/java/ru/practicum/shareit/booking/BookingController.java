package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.dto.StateOfBookingRequest;
import ru.practicum.shareit.validate.BookingTimeValidator;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@Validated
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingClient bookingClient;
    private final BookingTimeValidator bookingTimeValidator;

    @PostMapping
    public ResponseEntity<Object> createNewBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestBody final BookingShortDto bookingShortDto) {
        bookingTimeValidator.validateBookingTime(bookingShortDto.getStart(), bookingShortDto.getEnd());
        log.info("Пользователь id {} создал бронирование на вещь id {}", userId, bookingShortDto.getItemId());
        return bookingClient.addBooking(userId, bookingShortDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable Long bookingId,
                                                 @RequestParam Boolean approved) {
        if (approved) {
            log.info("Пользователь id {} подтвердил бронирование id {}", userId, bookingId);
        } else {
            log.info("Пользователь id {} отклонил бронирование id {}", userId, bookingId);
        }
        return bookingClient.approveBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@PathVariable final Long bookingId,
                                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Пользователем id {} получено бронирование id {}", userId, bookingId);
        return bookingClient.getBooking(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(value = "state", defaultValue = "ALL") String stateParam,
                                                  @RequestParam(value = "from", required = false, defaultValue = "0")
                                                  @PositiveOrZero(message = "Значение 'from' должно быть положительным") final Integer from,
                                                  @RequestParam(value = "size", required = false, defaultValue = "10")
                                                  @Positive(message = "Значение 'size' должно быть положительным") final Integer size) {

        StateOfBookingRequest state = StateOfBookingRequest.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Получены бронирования со статусом {} пользователя id {}", state, userId);
        return bookingClient.getUserBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsForUserItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                          @RequestParam(value = "state", defaultValue = "ALL") String stateParam,
                                                          @RequestParam(value = "from", required = false, defaultValue = "0") @PositiveOrZero(message = "Значение 'from' должно быть положительным") final Integer from,
                                                          @RequestParam(value = "size", required = false, defaultValue = "10")
                                                          @Positive(message = "Значение 'size' должно быть положительным") final Integer size) {
        StateOfBookingRequest state = StateOfBookingRequest.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Получены бронирования со статусом {} вещей пользователя id {}", state, userId);
        return bookingClient.getBookingsForUserItems(userId, state, from, size);
    }
}