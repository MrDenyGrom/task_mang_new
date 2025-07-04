package com.example.taskmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
@Schema(description = "Запрос на смену пароля пользователя")
public class UpdatePasswordRequest{
        @Schema(description = "Текущий (старый) пароль пользователя.", example = "OldSecurePassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Старый пароль не может быть пустым")
        String oldPassword;

        @Schema(description = "Новый пароль пользователя. Должен быть надежным.", example = "NewSecurePassword456!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Новый пароль не может быть пустым")
        @Size(min = 8, message = "Новый пароль должен быть не менее 8 символов")
        String newPassword;
}