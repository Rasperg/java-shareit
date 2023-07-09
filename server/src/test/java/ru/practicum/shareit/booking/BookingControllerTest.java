package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {
    private final UserDto userBookerDto = UserDto.builder().id(2L).name("testUser").email("user@email.ru").build();
    private final ItemDto itemDto = ItemDto.builder().id(1L).name("itemName").description("itemDesc")
            .available(true).build();
    private final BookingShortDto bookingShortDto = ru.practicum.shareit.booking.dto.BookingShortDto.builder().id(1L).start(LocalDateTime.now())
            .end(LocalDateTime.now().plusHours(2)).itemId(1L).bookerId(1L).build();
    private final BookingDto bookingDto = BookingDto.builder().id(1L).start(bookingShortDto.getStart())
            .end(bookingShortDto.getEnd()).item(itemDto).booker(userBookerDto).status(BookingStatus.WAITING).build();
    private final BookingDto approvedBooking = BookingDto.builder().id(2L).start(bookingShortDto.getStart())
            .end(bookingShortDto.getEnd()).item(itemDto).booker(userBookerDto).status(BookingStatus.APPROVED).build();
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingService bookingService;

    @Test
    void createBooking() throws Exception {
        when(bookingService.createBooking(anyLong(), any(BookingShortDto.class))).thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(bookingShortDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(notNullValue())))
                .andExpect(jsonPath("$.end", is(notNullValue())))
                .andExpect(jsonPath("$.status", is("WAITING")));

        verify(bookingService).createBooking(anyLong(), any(BookingShortDto.class));
    }

    @Test
    void approveBooking() throws Exception {
        when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean())).thenReturn(approvedBooking);

        mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(approvedBooking.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(notNullValue())))
                .andExpect(jsonPath("$.end", is(notNullValue())))
                .andExpect(jsonPath("$.status", is("APPROVED")));

        verify(bookingService).approveBooking(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void getBooking() throws Exception {
        when(bookingService.getBooking(anyLong(), anyLong())).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{bookingId}", 1)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(notNullValue())))
                .andExpect(jsonPath("$.end", is(notNullValue())))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingDto.getItem().getName())));

        verify(bookingService).getBooking(anyLong(), anyLong());
    }

    @Test
    void getUserBookings() throws Exception {
        when(bookingService.getAllBookingsByUser(anyLong(), eq("ALL"), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto, approvedBooking));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(notNullValue())))
                .andExpect(jsonPath("$[0].end", is(notNullValue())))
                .andExpect(jsonPath("$[0].status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$[0].booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingDto.getItem().getId()), Long.class))

                .andExpect(jsonPath("$[1].id", is(approvedBooking.getId()), Long.class))
                .andExpect(jsonPath("$[1].start", is(notNullValue())))
                .andExpect(jsonPath("$[1].end", is(notNullValue())))
                .andExpect(jsonPath("$[1].status", is(approvedBooking.getStatus().toString())))
                .andExpect(jsonPath("$[1].booker.id", is(approvedBooking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.id", is(approvedBooking.getItem().getId()), Long.class));

        verify(bookingService).getAllBookingsByUser(anyLong(), eq("ALL"), anyInt(), anyInt());
    }

    @Test
    void getBookingsForUserItems() throws Exception {
        when(bookingService.getBookingsForUserItems(anyLong(), eq("ALL"), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto, approvedBooking));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(notNullValue())))
                .andExpect(jsonPath("$[0].end", is(notNullValue())))
                .andExpect(jsonPath("$[0].status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$[0].booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingDto.getItem().getId()), Long.class))

                .andExpect(jsonPath("$[1].id", is(approvedBooking.getId()), Long.class))
                .andExpect(jsonPath("$[1].start", is(notNullValue())))
                .andExpect(jsonPath("$[1].end", is(notNullValue())))
                .andExpect(jsonPath("$[1].status", is(approvedBooking.getStatus().toString())))
                .andExpect(jsonPath("$[1].booker.id", is(approvedBooking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.id", is(approvedBooking.getItem().getId()), Long.class));

        verify(bookingService).getBookingsForUserItems(anyLong(), eq("ALL"), anyInt(), anyInt());
    }
}
