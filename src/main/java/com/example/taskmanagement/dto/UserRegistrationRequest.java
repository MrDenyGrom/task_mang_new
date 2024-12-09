package com.example.taskmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для регистрации пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {

    /**
     * Пароль пользователя. Не может быть пустым.
     */
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;

    /**
     * Email пользователя. Не может быть пустым и должен соответствовать формату email.
     */
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат Email")
    private String email;
}