package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    private final LocalDateTime now = LocalDateTime.now();
    private final User owner = new User(1L, "user", "user@mail.ru");
    private final User booker = new User(2L, "userBooker", "booker@mail.ru");
    private final Item item = Item.builder().id(1L).name("item2Name").description("item2Desc")
            .available(true).owner(owner).requestId(1L).build();
    private final BookingShortDto bookingShortDto = BookingShortDto.builder().id(1L).start(now.plusHours(1))
            .end(now.plusHours(2)).itemId(1L).bookerId(2L).build();
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;
    private Booking booking;

    @BeforeEach
    void init() {
        booking = BookingMapper.toBooking(bookingShortDto);
        booking.setItem(item);
        booking.setBooker(booker);
    }

    @Test
    void testCreateBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto actualBookingDto = bookingService.createBooking(booker.getId(), bookingShortDto);

        assertEquals(bookingShortDto.getId(), actualBookingDto.getId());
        assertNotNull(actualBookingDto.getStart());
        assertNotNull(actualBookingDto.getEnd());
        assertEquals(bookingShortDto.getBookerId(), actualBookingDto.getBooker().getId());
        assertEquals(bookingShortDto.getItemId(), actualBookingDto.getItem().getId());
        assertEquals(actualBookingDto.getStatus(), BookingStatus.WAITING);

        verify(bookingRepository).save(any());
    }

    @Test
    void testCreateBookingWithWrongBookerId() {
        Long wrongBookerId = 100L;
        when(userRepository.findById(wrongBookerId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> bookingService.createBooking(wrongBookerId, bookingShortDto));

        verify(userRepository).findById(wrongBookerId);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testCreateBookingWithWrongItemId() {
        Long wrongItemId = 100L;
        bookingShortDto.setItemId(wrongItemId);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(wrongItemId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> bookingService.createBooking(anyLong(), bookingShortDto));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testCreateBookingWhenItemIsNotAvailable() {
        item.setAvailable(false);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(anyLong(), bookingShortDto));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testCreateBookingWhenOwnerIsEqualBooker() {
        item.setOwner(booker);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(ObjectNotFoundException.class, () -> bookingService.createBooking(booker.getId(), bookingShortDto));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testCreateBookingWithIncorrectBookingTime() {
        BookingShortDto bookingWithNullTime = BookingShortDto.builder().id(2L).start(null)
                .end(null).itemId(1L).bookerId(2L).build();
        BookingShortDto bookingWithIncorrectEnd = BookingShortDto.builder().id(2L).start(now.plusHours(1))
                .end(now.minusHours(5)).itemId(1L).bookerId(2L).build();
        BookingShortDto bookingWithIncorrectStart = BookingShortDto.builder().id(1L).start(now.minusHours(2))
                .end(now.plusHours(2)).itemId(1L).bookerId(2L).build();
        BookingShortDto bookingWithStartEqualsEnd = BookingShortDto.builder().id(1L).start(now.plusHours(1))
                .end(now.plusHours(1)).itemId(1L).bookerId(2L).build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(booker.getId(),
                bookingWithIncorrectEnd));
        verify(bookingRepository, never()).save(any(Booking.class));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(booker.getId(),
                bookingWithIncorrectStart));
        verify(bookingRepository, never()).save(any(Booking.class));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(booker.getId(),
                bookingWithStartEqualsEnd));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testApproveBooking() {
        Long bookingItemOwner = booking.getItem().getOwner().getId();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto actualDto = bookingService.approveBooking(booking.getId(), bookingItemOwner, true);

        assertEquals(booking.getId(), actualDto.getId());
        assertEquals(booking.getStart(), actualDto.getStart());
        assertEquals(booking.getEnd(), actualDto.getEnd());
        assertEquals(booking.getItem().getId(), actualDto.getItem().getId());
        assertEquals(booking.getBooker().getId(), actualDto.getBooker().getId());
        assertEquals(actualDto.getStatus(), BookingStatus.APPROVED);

        verify(bookingRepository).save(booking);
    }

    @Test
    void testApproveBookingWhenItemOwnerWantsReject() {
        booking.setItem(item);
        booking.setBooker(booker);

        Long bookingItemOwner = booking.getItem().getOwner().getId();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto actualDto = bookingService.approveBooking(booking.getId(), bookingItemOwner, false);

        assertEquals(booking.getId(), actualDto.getId());
        assertEquals(booking.getStart(), actualDto.getStart());
        assertEquals(booking.getEnd(), actualDto.getEnd());
        assertEquals(booking.getItem().getId(), actualDto.getItem().getId());
        assertEquals(booking.getBooker().getId(), actualDto.getBooker().getId());
        assertEquals(actualDto.getStatus(), BookingStatus.REJECTED);

        verify(bookingRepository).save(booking);
    }

    @Test
    void testApproveBookingWithWrongBookingId() {
        Long wrongBookingId = 100L;
        when(bookingRepository.findById(wrongBookingId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () ->
                bookingService.approveBooking(wrongBookingId, booking.getItem().getOwner().getId(), true));

        verify(bookingRepository).findById(wrongBookingId);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testApproveBookingWithWrongBookingStatus() {
        Long bookingItemOwner = booking.getItem().getOwner().getId();
        booking.setStatus(BookingStatus.REJECTED);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(BadRequestException.class, () ->
                bookingService.approveBooking(booking.getId(), bookingItemOwner, true));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testApproveBookingWithWrongItemOwner() {
        User notItemOwner = new User(3L, "User3", "user3@mail.ru");

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(ObjectNotFoundException.class, () ->
                bookingService.approveBooking(booking.getId(), notItemOwner.getId(), true));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testGetBookingByItemOwner() {
        Long bookingItemOwner = booking.getItem().getOwner().getId();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto actualDto = bookingService.getBooking(booking.getId(), bookingItemOwner);

        assertEquals(booking.getId(), actualDto.getId());
        assertEquals(booking.getStart(), actualDto.getStart());
        assertEquals(booking.getEnd(), actualDto.getEnd());
        assertEquals(booking.getItem().getId(), actualDto.getItem().getId());
        assertEquals(booking.getBooker().getId(), actualDto.getBooker().getId());
        assertEquals(actualDto.getStatus(), BookingStatus.WAITING);

        verify(bookingRepository).findById(booking.getId());
    }

    @Test
    void testGetBookingByBooker() {
        Long bookerId = booking.getBooker().getId();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto actualDto = bookingService.getBooking(booking.getId(), bookerId);

        assertEquals(booking.getId(), actualDto.getId());
        assertEquals(booking.getStart(), actualDto.getStart());
        assertEquals(booking.getEnd(), actualDto.getEnd());
        assertEquals(booking.getItem().getId(), actualDto.getItem().getId());
        assertEquals(booking.getBooker().getId(), actualDto.getBooker().getId());
        assertEquals(actualDto.getStatus(), BookingStatus.WAITING);

        verify(bookingRepository).findById(booking.getId());
    }

    @Test
    void testGetBookingByOtherUser() {
        User otherUser = new User(4L, "user4", "user4@mail.ru");

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(ObjectNotFoundException.class, () -> bookingService.getBooking(booking.getId(), otherUser.getId()));
    }

    @Test
    void testGetAllBookingsByUser() {
        Long userId = 1L;
        String state = "WAITING";
        Integer from = 0;
        Integer size = 10;

        User user = new User();
        user.setId(userId);

        List<Booking> userBookings = new ArrayList<>();
        userBookings.add(new Booking(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), new Item(), user, BookingStatus.WAITING));
        userBookings.add(new Booking(2L, LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(4), new Item(), user, BookingStatus.APPROVED));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerAndStatus(any(User.class), any(BookingStatus.class), any(Pageable.class))).thenReturn(Collections.singletonList(userBookings.get(0)));
        Collection<BookingDto> results = bookingService.getAllBookingsByUser(userId, state, from, size);

        assertEquals(1, results.size());
        assertEquals(BookingStatus.WAITING, results.iterator().next().getStatus());

    }

    @Test
    public void testGetBookingsForUserItemsWithWaitingStatus() {
        Long userId = 1L;
        String state = "WAITING";
        Integer from = 0;
        Integer size = 10;

        User user = new User();
        user.setId(userId);
        List<Booking> userBookings = new ArrayList<>();

        Item item1 = new Item();
        item1.setOwner(user);
        userBookings.add(new Booking(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), item1, user, BookingStatus.WAITING));

        Item item2 = new Item();
        item2.setOwner(user);
        userBookings.add(new Booking(2L, LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(4), item2, user, BookingStatus.APPROVED));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findByItem_OwnerAndStatus(eq(user), eq(BookingStatus.WAITING), any(Pageable.class))).thenReturn(userBookings);

        Collection<BookingDto> result = bookingService.getBookingsForUserItems(userId, state, from, size);

        assertEquals(userBookings.size(), result.size());
    }

    @Test
    public void testGetBookingsForUserItemsWithPastStatus() {
        Long userId = 1L;
        String state = "PAST";
        Integer from = 0;
        Integer size = 10;

        User user = new User();
        user.setId(userId);
        List<Booking> userBookings = new ArrayList<>();

        Item item1 = new Item();
        item1.setOwner(user);
        userBookings.add(new Booking(1L, LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1), item1, user, BookingStatus.CANCELED));

        Item item2 = new Item();
        item2.setOwner(user);
        userBookings.add(new Booking(2L, LocalDateTime.now().minusHours(4), LocalDateTime.now().minusHours(3), item2, user, BookingStatus.APPROVED));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findByItem_OwnerAndEndIsBefore(eq(user), any(LocalDateTime.class), any(Pageable.class))).thenReturn(userBookings);

        Collection<BookingDto> result = bookingService.getBookingsForUserItems(userId, state, from, size);

        assertEquals(userBookings.size(), result.size());
    }

    @Test
    public void testGetBookingsForUserItemsWithFutureStatus() {
        Long userId = 1L;
        String state = "FUTURE";
        Integer from = 0;
        Integer size = 10;

        User user = new User();
        user.setId(userId);
        List<Booking> userBookings = new ArrayList<>();

        Item item1 = new Item();
        item1.setOwner(user);
        userBookings.add(new Booking(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), item1, user, BookingStatus.FUTURE));

        Item item2 = new Item();
        item2.setOwner(user);
        userBookings.add(new Booking(2L, LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(4), item2, user, BookingStatus.APPROVED));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findByItem_OwnerAndStartIsAfter(eq(user), any(LocalDateTime.class), any(Pageable.class))).thenReturn(userBookings);

        Collection<BookingDto> result = bookingService.getBookingsForUserItems(userId, state, from, size);

        assertEquals(userBookings.size(), result.size());
    }

    @Test
    void testGetBookingsForUserItemsWithIncorrectStatus() {
        List<Booking> userBookings = new ArrayList<>();
        userBookings.add(new Booking(1L, now.minusDays(2), now.minusDays(1), item, booker, BookingStatus.REJECTED));

        assertThrows(BadRequestException.class, () -> bookingService.getBookingsForUserItems(owner.getId(),
                "INCORRECT", 0, 10));
    }

    @Test
    void testBookingShortDto() {
        BookingShortDto convertedDto = BookingMapper.toShortBookingDto(booking);

        assertNotNull(convertedDto.getStart());
        assertNotNull(convertedDto.getEnd());
        assertEquals(booking.getItem().getId(), convertedDto.getItemId());
        assertEquals(booking.getBooker().getId(), convertedDto.getBookerId());
        assertEquals(booking.getStatus().toString(), convertedDto.getStatus());
    }
}