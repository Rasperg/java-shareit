package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@Validated
public class ItemControllerTest {
    private final ItemDto itemDto = ItemDto.builder().id(1L).name("itemName").description("itemDesc").available(true).build();
    private final CommentDto commentDto = CommentDto.builder().id(1L).text("test comment").item(itemDto)
            .authorName("authorName").created(LocalDateTime.now()).build();
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemService itemService;
    @MockBean
    private CommentService commentService;

    @Test
    void createItem() throws Exception {
        when(itemService.createItem(anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()));

        verify(itemService).createItem(anyLong(), any(ItemDto.class));
    }

    @Test
    void createItemWithInvalidParameters() throws Exception {
        ItemDto itemDtoWithInvalidName = ItemDto.builder().id(1L).name(" ").description("desc").available(true).build();
        ItemDto itemDtoWithInvalidDescription = ItemDto.builder().id(1L).name("name").description("").available(true).build();
        ItemDto itemDtoWithInvalidAvailable = ItemDto.builder().id(1L).name("name").description("desc").available(null).build();

        when(itemService.createItem(anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemDtoWithInvalidName))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemDtoWithInvalidDescription))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemDtoWithInvalidAvailable))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).createItem(anyLong(), any(ItemDto.class));
    }

    @Test
    void updateItem() throws Exception {
        itemDto.setDescription("Updated Item Description");
        when(itemService.updateItem(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(itemDto);

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));

        assertEquals("Updated Item Description", itemDto.getDescription());
        verify(itemService).updateItem(anyLong(), anyLong(), any(ItemDto.class));
    }

    @Test
    void getItem() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(itemDto);

        mockMvc.perform(get("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())));

        verify(itemService).getItemById(anyLong(), anyLong());
    }

    @Test
    void getUserItems() throws Exception {
        when(itemService.getUserItems(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())));

        verify(itemService).getUserItems(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getUserItemsWithIncorrectPageParameters() throws Exception {
        int invalidPage = -1;
        int invalidSize = -10;

        when(itemService.getUserItems(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .param("page", String.valueOf(invalidPage))
                        .param("size", String.valueOf(invalidSize))
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(itemService, never()).getUserItems(anyLong(), eq(invalidPage), eq(invalidSize));
    }

    @Test
    void searchItem() throws Exception {
        ItemDto dtoForSearch = ItemDto.builder().id(2L).name("Щетка для обуви").description("Хорошо чистит").available(true)
                .build();
        when(itemService.searchItem(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(dtoForSearch));

        mockMvc.perform(get("/items/search")
                        .param("text", "щетка"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(dtoForSearch.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(dtoForSearch.getName())))
                .andExpect(jsonPath("$[0].description", is(dtoForSearch.getDescription())))
                .andExpect(jsonPath("$[0].available", is(dtoForSearch.getAvailable())));

        verify(itemService).searchItem(anyString(), anyInt(), anyInt());
    }

    @Test
    void searchItemWithIncorrectParameter() throws Exception {
        ItemDto dtoForSearch = ItemDto.builder().id(2L).name("Щетка для обуви").description("Хорошо чистит").available(true)
                .build();
        when(itemService.searchItem(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(dtoForSearch));

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(itemService, never()).searchItem(anyString(), anyInt(), anyInt());
    }

    @Test
    void searchItemWithIncorrectPageParameters() throws Exception {
        int invalidPage = -1;
        int invalidSize = -10;

        ItemDto dtoForSearch = ItemDto.builder().id(2L).name("Щетка для обуви").description("Хорошо чистит").available(true)
                .build();
        when(itemService.searchItem(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(dtoForSearch));

        mockMvc.perform(get("/items/search")
                        .param("text", "щетка")
                        .param("page", String.valueOf(invalidPage))
                        .param("size", String.valueOf(invalidSize)))
                .andExpect(status().isOk());

        verify(itemService, never()).searchItem(anyString(), eq(invalidPage), eq(invalidSize));
    }

    @Test
    void createItemComment() throws Exception {
        CommentShortDto shortDto = CommentShortDto.builder().id(1L).text("test comment").itemId(1L)
                .authorName("authorName").created(LocalDateTime.now()).build();
        CommentDto commentDto = CommentDto.builder().id(1L).text("test comment").item(itemDto)
                .authorName("authorName").created(shortDto.getCreated()).build();

        when(commentService.addNewComment(any(CommentShortDto.class), anyLong(), anyLong()))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(shortDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created", is(notNullValue())));

        verify(commentService).addNewComment(any(CommentShortDto.class), anyLong(), anyLong());
    }

    @Test
    void createItemCommentWithoutText() throws Exception {
        CommentShortDto shortInvalidDto = CommentShortDto.builder().text("").build();

        when(commentService.addNewComment(any(CommentShortDto.class), anyLong(), anyLong()))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(shortInvalidDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).addNewComment(any(CommentShortDto.class), anyLong(), anyLong());
    }
}