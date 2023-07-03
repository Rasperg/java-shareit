package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.request.dto.ItemNewRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class ItemRequestServiceImplTest {
    private final User requestor = User.builder().id(2L).name("User2").email("user2@email.ru").build();

    private final ItemNewRequestDto requestDto = ItemNewRequestDto.builder().id(1L).description("reqDesc").created(LocalDateTime.now()).build();

    @Mock
    private UserRepository userRepository;
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