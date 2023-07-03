package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IntBookingServiceImplTest {
    @Autowired
    private final BookingRepository bookingRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final ItemRepository itemRepository;
    @Autowired
    private final BookingServiceImpl bookingService;
    private final User user = User.builder().name("user").email("user@mail.ru").build();
    private final User booker = User.builder().name("user2").email("user2@mail.ru").build();
    private final Item item = Item.builder().name("itemName").description("item1Desc").available(true)
            .owner(user).build();
    private final Booking booking = Booking.builder().start(LocalDateTime.now()).end(LocalDateTime.now().plusHours(2))
            .item(item).booker(booker).status(BookingStatus.WAITING).build();
    private final Booking secondBooking = Booking.builder().start(LocalDateTime.now())
            .end(LocalDateTime.now().plusHours(4)).item(item).booker(booker).status(BookingStatus.WAITING).build();
    private final Item secondItem = Item.builder().name("item2Name").description("item2Desc").available(true)
            .owner(user).build();

    @BeforeEach
    void setUp() {
        userRepository.save(user);
        userRepository.save(booker);

        itemRepository.save(item);
        itemRepository.save(secondItem);

        bookingRepository.save(booking);
        bookingRepository.save(secondBooking);
    }

/*    @Test
    void testGetAllBookingsByUser() {
        Collection<BookingDto> userBookings = bookingService.getAllBookingsByUser(booker.getId(), "CURRENT", 0, 10);

        assertNotNull(userBookings);
        assertEquals(2, userBookings.size());

        for (BookingDto dto : userBookings) {
            assertNotNull(dto.getId());
            assertNotNull(dto.getStart());
            assertNotNull(dto.getEnd());
            assertNotNull(dto.getItem());
            assertNotNull(dto.getBooker());
            assertNotNull(dto.getStatus());
        }
    }*/

    @Test
    void testGetAllBookingsForUserItems() {
        Collection<BookingDto> userBookings = bookingService.getBookingsForUserItems(user.getId(), "CURRENT", 0, 10);

        assertNotNull(userBookings);
        assertEquals(2, userBookings.size());

        for (BookingDto dto : userBookings) {
            assertNotNull(dto.getId());
            assertNotNull(dto.getStart());
            assertNotNull(dto.getEnd());
            assertNotNull(dto.getItem());
            assertNotNull(dto.getBooker());
            assertNotNull(dto.getStatus());
        }
    }
}