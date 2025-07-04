package com.example.taskmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * <p><b>DTO: Запрос на Регистрацию Пользователя</b></p>
 *
 * <p>
 *     Инкапсулирует данные, необходимые для создания новой учетной записи пользователя.
 *     Используется в качестве тела HTTP-запроса (request body) на эндпоинте регистрации.
 * </p>
 */
@Setter
@Getter
@RequiredArgsConstructor
@Schema(description = "Схема для регистрации нового пользователя")
public class UserRegistrationRequest {

    @Schema(description = "Пароль для новой учетной записи", example = "MyStrongP@ssw0rd")
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;

    @Schema(description = "Уникальный email пользователя, который будет использоваться для входа", example = "KarinaKrutih4343@suai.com")
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат Email")
    private String email;
}