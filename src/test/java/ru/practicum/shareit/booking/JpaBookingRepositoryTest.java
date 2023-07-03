package ru.practicum.shareit.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class JpaBookingRepositoryTest {
    private final User owner = User.builder().name("userName").email("user@mail.ru").build();
    private final User booker = User.builder().name("user2Name").email("user2@mail.ru").build();
    private final Item item = Item.builder().name("Отвертка").description("Аккумуляторная отвертка").available(true)
            .owner(owner).build();
    private final Booking booking = Booking.builder().start(LocalDateTime.now()).end(LocalDateTime.now().plusHours(2))
            .item(item).booker(booker).status(BookingStatus.WAITING).build();
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void addData() {
        userRepository.save(owner);
        userRepository.save(booker);
        itemRepository.save(item);
        bookingRepository.save(booking);
    }

    @AfterEach
    void deleteData() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    void findByItem() {
        List<Booking> actualBookings = bookingRepository.findByItem(item);

        assertFalse(actualBookings.isEmpty());
        assertEquals(1, actualBookings.size());

        Booking actualBooking = actualBookings.get(0);
        assertNotNull(actualBooking.getStart());
        assertNotNull(actualBooking.getEnd());
        assertEquals(item, actualBooking.getItem());
        assertEquals(booker, actualBooking.getBooker());
        assertEquals(BookingStatus.WAITING, actualBooking.getStatus());
    }

 /*   @Test
    void findByBooker() {
        List<Booking> actualBookings = bookingRepository.findByBooker(booker, PageRequest.of(0, 10));

        assertFalse(actualBookings.isEmpty());
        assertEquals(1, actualBookings.size());

        Booking actualBooking = actualBookings.get(0);
        assertNotNull(actualBooking.getStart());
        assertNotNull(actualBooking.getEnd());
        assertEquals(item, actualBooking.getItem());
        assertEquals(booker, actualBooking.getBooker());
        assertEquals(BookingStatus.WAITING, actualBooking.getStatus());
    }*/

/*    @Test
    void findByItem_Owner() {
        List<Booking> actualBookings = bookingRepository.findByItem_Owner(owner, PageRequest.of(0, 10));

        assertFalse(actualBookings.isEmpty());
        assertEquals(1, actualBookings.size());

        Booking actualBooking = actualBookings.get(0);
        assertNotNull(actualBooking.getStart());
        assertNotNull(actualBooking.getEnd());
        assertEquals(item, actualBooking.getItem());
        assertEquals(booker, actualBooking.getBooker());
        assertEquals(BookingStatus.WAITING, actualBooking.getStatus());
    }*/
}
