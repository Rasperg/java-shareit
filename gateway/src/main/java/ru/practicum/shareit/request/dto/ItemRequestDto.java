package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class ItemRequestDto {
    private Long id;
    @NotBlank
    @NotNull
    private String description;
    private Long requestorId;
    private LocalDateTime created;
    private Set<ItemDto> items;
}
