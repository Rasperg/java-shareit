package ru.practicum.shareit.item.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .build();
    }

    public static ItemDto toItemDto(Item item, List<Comment> itemComments) {
        List<CommentShortDto> commentsShortDto = itemComments.stream().map(CommentMapper::toCommentShortDto).collect(Collectors.toList());
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(commentsShortDto)
                .requestId(item.getRequestId())
                .build();
    }

    public static Item toItem(ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .requestId(itemDto.getRequestId())
                .build();
    }

    public static ItemDto toItemDto(Item item, List<Comment> itemComments, List<Booking> bookings) {
        ItemDto itemDto = toItemDto(item, itemComments);

        LocalDateTime now = LocalDateTime.now();
        for (Booking booking : bookings) {
            if (booking.getStart().isAfter(now) &&
                    (booking.getStatus() == BookingStatus.WAITING ||
                            booking.getStatus() == BookingStatus.APPROVED)) {
                itemDto.setNextBooking(BookingMapper.toShortBookingDto(booking));
            }
            if (booking.getStart().isBefore(now) &&
                    (booking.getStatus() == BookingStatus.WAITING ||
                            booking.getStatus() == BookingStatus.APPROVED)) {
                itemDto.setLastBooking(BookingMapper.toShortBookingDto(booking));
            }
        }
        return itemDto;
    }

    public static Set<ItemDto> toItemDtos(Set<Item> items) {
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toSet());
    }
}
