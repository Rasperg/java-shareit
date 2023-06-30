package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.Collection;

public interface BookingService {
    BookingDto createBooking(Long userId, BookingShortDto bookingShortDtoDto);

    BookingDto approveBooking(Long bookingId, Long ownerId, Boolean isApproved);

    BookingDto getBooking(Long bookingId, Long userId);

    Collection<BookingDto> getAllBookingsByUser(Long userId, String state);

    Collection<BookingDto> getBookingsForUserItems(Long userId, String state);
}