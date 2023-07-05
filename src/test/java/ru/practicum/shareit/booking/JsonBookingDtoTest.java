package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class JsonBookingDtoTest {
    @Autowired
    JacksonTester<BookingDto> jacksonTester;

    @Autowired
    JacksonTester<BookingShortDto> jacksonTesterForShort;


    @Test
    void bookingDtoTest() throws Exception {
        UserDto booker = UserDto.builder().id(1L).name("user1").email("user@mail.ru").build();

        ItemDto item = ItemDto.builder().id(1L).name("ItemName").description("ItemDescription").available(true).build();

        BookingDto booking = BookingDto.builder().id(1L)
                .start(LocalDateTime.of(2023, 6, 2, 12, 0, 0))
                .end(LocalDateTime.of(2023, 6, 5, 12, 0, 0))
                .item(item).booker(booker).status(BookingStatus.WAITING).build();

        JsonContent<BookingDto> actualDto = jacksonTester.write(booking);

        assertThat(actualDto).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(actualDto).extractingJsonPathStringValue("$.start").isEqualTo("2023-06-02T12:00:00");
        assertThat(actualDto).extractingJsonPathStringValue("$.end").isEqualTo("2023-06-05T12:00:00");
        assertThat(actualDto).extractingJsonPathStringValue("$.item.name").isEqualTo("ItemName");
        assertThat(actualDto).extractingJsonPathStringValue("$.item.description").isEqualTo("ItemDescription");
        assertThat(actualDto).extractingJsonPathBooleanValue("$.item.available").isEqualTo(true);
        assertThat(actualDto).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(actualDto).extractingJsonPathStringValue("$.booker.name").isEqualTo("user1");
        assertThat(actualDto).extractingJsonPathStringValue("$.status").isEqualTo(BookingStatus.WAITING.toString());
    }

    @Test
    void bookingShortDtoTest() throws Exception {
        UserDto booker = UserDto.builder().id(1L).name("user1").email("user@mail.ru").build();

        ItemDto item = ItemDto.builder().id(1L).name("ItemName").description("ItemDescription").available(true).build();

        BookingShortDto bookingShort = BookingShortDto.builder().id(1L)
                .start(LocalDateTime.of(2023, 6, 2, 12, 0, 0))
                .end(LocalDateTime.of(2023, 6, 5, 12, 0, 0))
                .itemId(item.getId()).bookerId(booker.getId()).status(BookingStatus.WAITING.toString()).build();

        JsonContent<BookingShortDto> actualBookingShort = jacksonTesterForShort.write(bookingShort);

        assertThat(actualBookingShort).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(actualBookingShort).extractingJsonPathStringValue("$.start").isEqualTo("2023-06-02T12:00:00");
        assertThat(actualBookingShort).extractingJsonPathStringValue("$.end").isEqualTo("2023-06-05T12:00:00");
        assertThat(actualBookingShort).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(actualBookingShort).extractingJsonPathNumberValue("$.bookerId").isEqualTo(1);
        assertThat(actualBookingShort).extractingJsonPathStringValue("$.status").isEqualTo(BookingStatus.WAITING.toString());
    }
}
