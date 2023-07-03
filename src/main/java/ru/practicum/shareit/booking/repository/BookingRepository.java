package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByItem(Item item);

    List<Booking> findByItemIdIn(List<Long> items);

    List<Booking> findByBooker(User user, Pageable pageable);

    List<Booking> findByBookerAndStatus(User user, BookingStatus status);

    List<Booking> findByBookerAndEndIsBefore(User user, LocalDateTime end);

    List<Booking> findByBookerAndStartIsAfter(User user, LocalDateTime start);

    List<Booking> findByBookerAndStartIsBeforeAndEndIsAfter(User user, LocalDateTime start, LocalDateTime end);

    List<Booking> findByItem_Owner(User user, Pageable pageable);

    List<Booking> findByItem_OwnerAndStatus(User user, BookingStatus status);

    List<Booking> findByItem_OwnerAndEndIsBefore(User user, LocalDateTime end);

    List<Booking> findByItem_OwnerAndStartIsAfter(User user, LocalDateTime start);

    List<Booking> findByItem_OwnerAndStartIsBeforeAndEndIsAfter(User user, LocalDateTime start, LocalDateTime end);

    @Query("SELECT count(b) > 0 FROM Booking b " +
            "WHERE b.item = :item " +
            "AND b.booker = :booker " +
            "AND b.status = 'APPROVED' " +
            "AND b.start <= :start")
    boolean existsBookingByItemAndBookerAndStatusNotAndStart(Item item, User booker, LocalDateTime start);
}