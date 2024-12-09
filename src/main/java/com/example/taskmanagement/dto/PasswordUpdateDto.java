package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для обновления пароля.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordUpdateDto {

    /**
     * Старый пароль пользователя.
     */
    @NotBlank(message = "Старый пароль не может быть пустым")
    private String oldPassword;

    /**
     * Новый пароль пользователя.
     */
    @NotBlank(message = "Новый пароль не может быть пустым")
    @Size(min = 8, message = "Новый пароль должен содержать не менее 8 символов")
    private String newPassword;

}