package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class ItemRequestServiceImplTest {
    private final User requestor = User.builder().id(2L).name("User2").email("user2@email.ru").build();
    private final User owner = User.builder().id(1L).name("User1").email("user@email.ru").build();
    private final ItemRequestDto requestDto = ItemRequestDto.builder().id(1L).description("reqDesc").requestorId(2L)
            .created(LocalDateTime.now()).build();
    private final ItemRequest request = ItemRequest.builder().id(1L).description("reqDesc").requestor(requestor)
            .created(LocalDateTime.now()).build();
    private final ItemRequest secondRequest = ItemRequest.builder().id(2L).description("req2Desc").requestor(requestor)
            .created(LocalDateTime.now()).build();
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestRepository requestRepository;
    @InjectMocks
    private ItemRequestServiceImpl requestService;

    @Test
    void testAddNewRequest() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(requestor));

        ItemRequestDto createdRequestDto = requestService.addNewRequest(requestDto, requestor.getId());

        assertNotNull(createdRequestDto);
        assertNotNull(createdRequestDto.getId());
        assertNotNull(createdRequestDto.getDescription());
        assertNotNull(createdRequestDto.getRequestorId());
        assertNotNull(createdRequestDto.getCreated());

        assertEquals(requestDto.getDescription(), createdRequestDto.getDescription());
        assertEquals(requestor.getId(), createdRequestDto.getRequestorId());

        verify(userRepository).findById(anyLong());
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void testAddNewRequestWithWrongUser() {
        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> requestService.addNewRequest(requestDto, 100L));
        verify(userRepository).findById(100L);
        verify(requestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void testAddNewRequestWithIncorrectUserId() {
        when(userRepository.findById(anyLong())).thenThrow(new BadRequestException("Некорректный ввод id пользователя"));

        assertThrows(BadRequestException.class, () -> requestService.addNewRequest(requestDto, -1L));
        verify(userRepository).findById(-1L);
        verify(requestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void testGetUserRequests() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(requestor));

        Collection<ItemRequestDto> userRequests = requestService.getUserRequests(requestor.getId());

        assertNotNull(userRequests);

        for (ItemRequestDto requestDto : userRequests) {
            assertNotNull(requestDto.getId());
            assertNotNull(requestDto.getDescription());
            assertNotNull(requestDto.getRequestorId());
            assertNotNull(requestDto.getRequestorId());
            assertNotNull(requestDto.getCreated());

            assertNotNull(requestDto.getItems());
        }

        verify(userRepository).findById(requestor.getId());
        verify(requestRepository).findAllByRequestorOrderByCreated(requestor);
    }

    @Test
    void testGetUserRequestsWithWrongUser() {
        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> requestService.getUserRequests(100L));

        verify(userRepository).findById(100L);
        verify(requestRepository, never()).findAllByRequestorOrderByCreated(any(User.class));
    }

    @Test
    void testGetUserRequestsWithIncorrectUserId() {
        when(userRepository.findById(anyLong())).thenThrow(new BadRequestException("Некорректный ввод id пользователя"));

        assertThrows(BadRequestException.class, () -> requestService.getUserRequests(-1L));
        verify(userRepository).findById(-1L);
        verify(requestRepository, never()).findAllByRequestorOrderByCreated(any(User.class));

    }

    @Test
    void testGetRequestById() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(requestor));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        ItemRequestDto actualRequestDto = requestService.getRequestById(requestor.getId(), request.getId());

        assertNotNull(actualRequestDto);
        assertEquals(request.getId(), actualRequestDto.getId());
        assertEquals(request.getDescription(), actualRequestDto.getDescription());
        assertEquals(request.getRequestor().getId(), actualRequestDto.getRequestorId());
        assertEquals(request.getCreated(), actualRequestDto.getCreated());
        assertNotNull(actualRequestDto.getItems());

        verify(userRepository).findById(requestor.getId());
        verify(requestRepository).findById(request.getId());
    }

    @Test
    void testGetRequestByIdWithWrongUser() {
        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> requestService.getRequestById(100L, anyLong()));

        verify(userRepository).findById(100L);
        verify(requestRepository, never()).findById(anyLong());
    }

    @Test
    void testGetRequestByIdWithIncorrectUserId() {
        when(userRepository.findById(anyLong())).thenThrow(new BadRequestException("Некорректный ввод id пользователя"));

        assertThrows(BadRequestException.class, () -> requestService.getRequestById(-1L, anyLong()));
        verify(userRepository).findById(-1L);
        verify(requestRepository, never()).findById(anyLong());
    }

    @Test
    void testGetRequestByIdWithWrongRequestId() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(requestor));
        when(requestRepository.findById(100L)).thenThrow(new ObjectNotFoundException("Запрос не найден"));

        assertThrows(ObjectNotFoundException.class, () -> requestService.getRequestById(2L, 100L));

        verify(userRepository).findById(anyLong());
        verify(requestRepository).findById(100L);
    }

    @Test
    void testGetRequestByIdWithIncorrectRequestId() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requestor));
        when(requestRepository.findById(anyLong())).thenThrow(new BadRequestException("Некорректный ввод id запроса"));

        assertThrows(BadRequestException.class, () -> requestService.getRequestById(anyLong(), -1L));

        verify(userRepository).findById(anyLong());
        verify(requestRepository).findById(-1L);
    }

    @Test
    void testGetAllRequestsForAllUsers() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(requestRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(request, secondRequest)));

        Collection<ItemRequestDto> allRequests = requestService.getAllRequestsForAllUsers(owner.getId(), 0, 10);

        assertNotNull(allRequests);

        for (ItemRequestDto requestDto : allRequests) {
            assertNotNull(requestDto.getId());
            assertNotNull(requestDto.getDescription());
            assertNotNull(requestDto.getRequestorId());
            assertNotNull(requestDto.getRequestorId());
            assertNotNull(requestDto.getCreated());

            assertNotNull(requestDto.getItems());
        }

        verify(userRepository).findById(owner.getId());
        verify(requestRepository).findAll(any(PageRequest.class));
    }

    @Test
    void testGetAllRequestsForAllUsersWithWrongUser() {
        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> requestService.getAllRequestsForAllUsers(100L, 0, 10));

        verify(userRepository).findById(100L);
        verify(requestRepository, never()).findAll(any(PageRequest.class));
    }

    @Test
    void testGetAllRequestsForAllUsersWithIncorrectUserId() {
        when(userRepository.findById(anyLong())).thenThrow(new BadRequestException("Некорректный ввод id пользователя"));

        assertThrows(BadRequestException.class, () -> requestService.getAllRequestsForAllUsers(-1L, 0, 10));

        verify(userRepository).findById(-1L);
        verify(requestRepository, never()).findAll(any(PageRequest.class));
    }
}