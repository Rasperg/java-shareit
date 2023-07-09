package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentShortDto {
    private Long id;
    @NotNull
    @NotBlank
    private String text;
    private Long itemId;
    private String authorName;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime created;
}
