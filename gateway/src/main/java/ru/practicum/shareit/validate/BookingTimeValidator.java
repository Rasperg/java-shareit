package ru.practicum.shareit.validate;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BookingTimeValidator {
    public void validateBookingTime(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (end == null || start == null) {
            throw new IllegalArgumentException("Время начала и окончания бронирования должно быть задано");
        }
        if (end.isBefore(now) || end.isBefore(start)) {
            throw new IllegalArgumentException("Время конца бронирования указано некорректно.");
        }
        if (start.isBefore(now) || start.isAfter(end)) {
            throw new IllegalArgumentException("Время начала бронирования указано некорректно.");
        }
        if (start.equals(end)) {
            throw new IllegalArgumentException("Время начала бронирования указано некорректно.");
        }
    }
}
