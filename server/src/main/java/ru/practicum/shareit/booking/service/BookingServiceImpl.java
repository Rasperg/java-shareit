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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public BookingDto createBooking(Long userId, BookingShortDto bookingShortDto) {
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

    @Transactional(readOnly = true)
    @Override
    public Collection<BookingDto> getAllBookingsByUser(Long userId, String state, Integer from, Integer size) {
        StateOfBookingRequest stateIn = getState(state);
        User user = userRepository.findById(userId).orElseThrow();
        PageRequest page = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now().plusHours(5);
        List<Booking> userBookings;

        switch (stateIn) {
            case ALL:
                userBookings = bookingRepository.findByBooker(user, page);
                break;
            case CURRENT:
                userBookings = bookingRepository.findByBookerAndStartIsBeforeAndEndIsAfter(user, now, now, page);
                break;
            case PAST:
                userBookings = bookingRepository.findByBookerAndEndIsBefore(user, now, page);
                break;
            case FUTURE:
                userBookings = bookingRepository.findByBookerAndStartIsAfter(user, now, page);
                break;
            case WAITING:
                userBookings = bookingRepository.findByBookerAndStatus(user, BookingStatus.WAITING, page);
                break;
            case REJECTED:
                userBookings = bookingRepository.findByBookerAndStatus(user, BookingStatus.REJECTED, page);
                break;
            default:
                userBookings = new ArrayList<>();
                break;
        }

        log.info("Список всех бронирований со статусом {} пользователя id {} получен", state, userId);
        System.out.println("Проверка" + userBookings);
        return userBookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public Collection<BookingDto> getBookingsForUserItems(Long userId, String state, Integer from, Integer size) {
        StateOfBookingRequest stateIn = getState(state);
        User user = userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException(String.format("Пользователь id %s не найден", userId)));
        PageRequest page = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now().plusHours(5);
        List<Booking> userBookings;

        switch (stateIn) {
            case ALL:
                userBookings = bookingRepository.findByItem_Owner(user, page);
                break;
            case CURRENT:
                userBookings = bookingRepository.findByItem_OwnerAndStartIsBeforeAndEndIsAfter(user, now, now, page);
                break;
            case PAST:
                userBookings = bookingRepository.findByItem_OwnerAndEndIsBefore(user, now, page);
                break;
            case FUTURE:
                userBookings = bookingRepository.findByItem_OwnerAndStartIsAfter(user, now, page);
                break;
            case WAITING:
                userBookings = bookingRepository.findByItem_OwnerAndStatus(user, BookingStatus.WAITING, page);
                break;
            case REJECTED:
                userBookings = bookingRepository.findByItem_OwnerAndStatus(user, BookingStatus.REJECTED, page);
                break;
            default:
                userBookings = new ArrayList<>();
                break;
        }

        log.info("Список бронирований со статусом {} для вещей пользователя id {} получен", state, userId);
        return userBookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    private StateOfBookingRequest getState(String state) {
        try {
            return StateOfBookingRequest.valueOf(state);
        } catch (Throwable e) {
            throw new BadRequestException("Unknown state: " + state);
        }
    }
}
