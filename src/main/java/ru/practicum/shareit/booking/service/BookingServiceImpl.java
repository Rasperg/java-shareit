package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto createBooking(Long userId, @Valid BookingShortDto bookingShortDto) {
        User booker = userRepository.findById(userId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Пользователь id %s не найден", userId)));
        Item item = itemRepository.findById(bookingShortDto.getItemId()).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Вещь id %s не найдена", bookingShortDto.getItemId())));
        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь недоступна для бронирования!");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new ObjectNotFoundException("Нельзя забронировать собственную вещь");
        }
        Booking booking = BookingMapper.toBooking(bookingShortDto);
        validateBookingTime(booking);
        booking.setBooker(booker);
        booking.setItem(item);
        bookingRepository.save(booking);
        log.info("Пользователь  id {} забронировал вещь id {}", booker.getId(), item.getId());
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long bookingId, Long ownerId, Boolean isApproved) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        Booking booking = bookingOptional.orElseThrow(() ->
                new ObjectNotFoundException(String.format("Бронирование id %s не найдено", bookingId)));
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new BadRequestException("Бронирование было подтверждено ранее или отменено");
        }
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ObjectNotFoundException(String.format("Пользователь id %s не является владельцем вещи с бронированием id %s", ownerId, bookingId));
        }
        booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepository.save(booking);
        log.info("Пользователь id {} подтвердил бронирование вещи id {}", ownerId, bookingId);

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getBooking(Long bookingId, Long userId) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        Booking booking = bookingOptional.orElseThrow(() -> new ObjectNotFoundException(String.format("Бронирование id %s не найдено", bookingId)));
        if (!booking.getItem().getOwner().getId().equals(userId) && !booking.getBooker().getId().equals(userId)) {
            throw new ObjectNotFoundException(String.format("Пользователь id %s не является хозяином вещи и не делал бронирование id %s", userId, bookingId));
        }
        log.info("Бронирование id {} получено пользователем id {}", bookingId, userId);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public Collection<BookingDto> getAllBookingsByUser(Long userId, String state, Integer from, Integer size) {
        StateOfBookingRequest stateIn = getState(state);
        User user = userRepository.findById(userId).orElseThrow();
        PageRequest page = PageRequest.of(from / size, size, Sort.by("start").descending());
        List<Booking> userBookings = bookingRepository.findByBooker(user, page);
        log.info("Список всех бронирований со статусом {} пользователя id {} получен", state, userId);
        return getBookingsByState(userBookings, stateIn).stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public Collection<BookingDto> getBookingsForUserItems(Long userId, String state, Integer from, Integer size) {
        StateOfBookingRequest stateIn = getState(state);
        User user = userRepository.findById(userId).orElseThrow();
        PageRequest page = PageRequest.of(from / size, size, Sort.by("start").descending());
        List<Booking> userBookings = bookingRepository.findByItem_Owner(user, page);
        log.info("Список бронирований со статусом {} для вещей пользователя id {} получен", state, userId);
        return getBookingsByState(userBookings, stateIn).stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    public void validateBookingTime(Booking booking) throws BadRequestException {
        LocalDateTime start = booking.getStart();
        LocalDateTime end = booking.getEnd();

        if (start.isBefore(LocalDateTime.now()) || start.isAfter(end)) {
            throw new BadRequestException("Время начала бронирования указано некорректно.");
        }

        if (start.equals(booking.getEnd())) {
            throw new BadRequestException("Время начала бронирования указано некорректно.");
        }
    }

    private Collection<Booking> getBookingsByState(List<Booking> allBookings, StateOfBookingRequest state) {
        Stream<Booking> bookingStream = allBookings.stream();
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case CURRENT:
                bookingStream = bookingStream.filter(booking -> booking.getStart().isBefore(now) &&
                        booking.getEnd().isAfter(now));
                break;
            case PAST:
                bookingStream = bookingStream.filter(booking -> booking.getEnd().isBefore(now));
                break;
            case FUTURE:
                bookingStream = bookingStream.filter(booking -> booking.getStart().isAfter(now));
                break;
            case WAITING:
                bookingStream = bookingStream.filter(booking -> booking.getStatus().equals(BookingStatus.WAITING));
                break;
            case REJECTED:
                bookingStream = bookingStream.filter(booking -> booking.getStatus().equals(BookingStatus.REJECTED));
                break;
        }
        return bookingStream.sorted(Comparator.comparing(Booking::getStart).reversed()).collect(Collectors.toList());
    }

    private StateOfBookingRequest getState(String state) {
        try {
            return StateOfBookingRequest.valueOf(state);
        } catch (Throwable e) {
            throw new BadRequestException("Unknown state: " + state);
        }
    }
}