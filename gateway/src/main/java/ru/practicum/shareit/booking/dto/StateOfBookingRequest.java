package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum StateOfBookingRequest {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static Optional<StateOfBookingRequest> from(String stringState) {
        for (StateOfBookingRequest state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
