package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto createNewBooking(@Valid @RequestHeader("X-Sharer-User-Id") Long userId,
                                       @Valid @RequestBody final BookingShortDto bookingShortDto) {
        return bookingService.createBooking(userId, bookingShortDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@Valid @RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable @Valid Long bookingId,
                                     @RequestParam @Valid Boolean approved) {
        return bookingService.approveBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@PathVariable @Valid Long bookingId,
                                 @Valid @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getBooking(bookingId, userId);
    }

    @GetMapping
    public Collection<BookingDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(defaultValue = "ALL") String state,
                                                  @RequestParam(value = "from", required = false, defaultValue = "0")
                                                  @PositiveOrZero(message = "Значение 'from' должно быть положительным") final Integer from,
                                                  @RequestParam(value = "size", required = false, defaultValue = "10")
                                                  @Positive(message = "Значение 'size' должно быть положительным") final Integer size) {
        return bookingService.getAllBookingsByUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public Collection<BookingDto> getBookingsForUserItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                          @RequestParam(defaultValue = "ALL") String state,
                                                          @RequestParam(value = "from", required = false, defaultValue = "0")
                                                          @PositiveOrZero(message = "Значение 'from' должно быть положительным") final Integer from,
                                                          @RequestParam(value = "size", required = false, defaultValue = "10")
                                                          @Positive(message = "Значение 'size' должно быть положительным") final Integer size) {
        return bookingService.getBookingsForUserItems(userId, state, from, size);
    }
}