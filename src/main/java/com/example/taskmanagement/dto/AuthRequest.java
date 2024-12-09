package com.example.taskmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса аутентификации.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {

    /**
     * Email пользователя. Не может быть пустым и должен соответствовать формату email.
     */
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат Email")
    private String email;

    /**
     * Пароль пользователя. Не может быть пустым.
     */
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}