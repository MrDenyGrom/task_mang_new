package com.example.taskmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * <p><b>DTO: Запрос на Аутентификацию</b></p>
 *
 * <p>
 *     Инкапсулирует учетные данные пользователя (email и пароль)
 *     для входа в систему и получения JWT токенов.
 * </p>
 */
@Setter
@Getter
@RequiredArgsConstructor
@Schema(description = "Схема для аутентификации пользователя (логина)")
public class AuthRequest {

    @Schema(description = "Email пользователя", example = "starost4343@suai.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат Email")
    private String email;

    @Schema(description = "Пароль пользователя", example = "MyStrongP@ssw0rd", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}