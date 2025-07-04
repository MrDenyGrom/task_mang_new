package com.example.taskmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * <p><b>DTO: Обновление Пароля</b></p>
 *
 * <p>
 *     Инкапсулирует данные, необходимые для смены пароля аутентифицированного пользователя.
 *     Требует указания старого пароля для подтверждения личности.
 * </p>
 */
@Setter
@Getter
@RequiredArgsConstructor
@Schema(description = "Схема для смены пароля пользователя")
public class PasswordUpdateDto {

    @Schema(description = "Текущий (старый) пароль пользователя", example = "MyOldP@ssw0rd")
    @NotBlank(message = "Старый пароль не может быть пустым")
    private String oldPassword;

    @Schema(description = "Новый пароль (минимум 8 символов)", example = "MyNewSecureP@ssw0rd")
    @NotBlank(message = "Новый пароль не может быть пустым")
    @Size(min = 8, message = "Новый пароль должен содержать не менее 8 символов")
    private String newPassword;
}