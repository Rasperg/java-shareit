package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.common.Create;
import ru.practicum.shareit.common.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class UserDto {
    Long id;
    String name;
    @NotBlank(message = "Проверьте корректность Email", groups = {Create.class})
    @NotNull(message = "Проверьте корректность Email", groups = {Create.class})
    @Email(groups = {Create.class, Update.class})
    String email;
}