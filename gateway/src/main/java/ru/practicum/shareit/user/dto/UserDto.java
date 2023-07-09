package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validate.Create;
import ru.practicum.shareit.validate.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class UserDto {
    Long id;
    @NotBlank(groups = {Create.class})
    @NotNull(groups = {Create.class})
    String name;
    @NotBlank(message = "Email пользователя должен быть задан", groups = {Create.class})
    @NotNull(message = "Email пользователя должен быть задан", groups = {Create.class})
    @Email(groups = {Create.class, Update.class})
    String email;
}
