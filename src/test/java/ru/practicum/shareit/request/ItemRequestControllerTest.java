package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {
    private final ItemDto itemDto = ItemDto.builder().id(1L).name("itemName").description("itemDesc")
            .available(true).build();
    private final ItemRequestDto requestDto = ItemRequestDto.builder().id(1L).description("desc")
            .created(LocalDateTime.now()).requestorId(1L).items(Set.of(itemDto)).build();
    private final ItemRequestDto userRequest = ItemRequestDto.builder().description("descr").build();
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemRequestService requestService;

    @Test
    void createNewRequest() throws Exception {
        when(requestService.addNewRequest(any(ItemRequestDto.class), anyLong()))
                .thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(userRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestDto.getId()))
                .andExpect(jsonPath("$.description").value(requestDto.getDescription()))
                .andExpect(jsonPath("$.created").isNotEmpty());

        verify(requestService).addNewRequest(any(ItemRequestDto.class), anyLong());
    }

    @Test
    void getUserRequests() throws Exception {
        when(requestService.getUserRequests(anyLong())).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$[0].items.size()", notNullValue()));

        verify(requestService).getUserRequests(anyLong());
    }

    @Test
    void getAllRequests() throws Exception {
        when(requestService.getAllRequestsForAllUsers(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$[0].items.size()", notNullValue()));

        verify(requestService).getAllRequestsForAllUsers(anyLong(), anyInt(), anyInt());
    }


    @Test
    void getRequestById() throws Exception {
        when(requestService.getRequestById(anyLong(), anyLong()))
                .thenReturn(requestDto);

        mockMvc.perform(get("/requests/{requestId}", requestDto.getId())
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.items.size()", notNullValue()));

        verify(requestService).getRequestById(anyLong(), anyLong());
    }
}