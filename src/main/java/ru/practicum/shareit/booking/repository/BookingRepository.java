package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByItem(Item item);

    List<Booking> findByBooker(User user);

    List<Booking> findByItem_Owner(User user);

    @Query("SELECT count(b) > 0 FROM Booking b " +
            "WHERE b.item = :item " +
            "AND b.booker = :booker " +
            "AND b.status = 'APPROVED' " +
            "AND b.start <= :start")
    boolean existsBookingByItemAndBookerAndStatusNotAndStart(Item item, User booker, LocalDateTime start);
}