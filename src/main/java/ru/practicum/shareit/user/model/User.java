package ru.practicum.shareit.user.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class User {
    private Long id;
    private String name;
    @NotNull(message = "Проверьте корректность Email")
    @NotBlank(message = "Проверьте корректность Email")
    @Email
    private String email;
}