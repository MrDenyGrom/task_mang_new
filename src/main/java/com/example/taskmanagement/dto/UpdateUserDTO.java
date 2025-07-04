package com.example.taskmanagement.dto;

import com.example.taskmanagement.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * <p><b>DTO: Обновление Роли Пользователя</b></p>
 *
 * <p>
 *     Используется для изменения роли существующего пользователя.
 *     Как правило, доступ к такой операции имеет только администратор.
 * </p>
 */
@Setter
@Getter
@RequiredArgsConstructor
@Schema(description = "Схема для обновления роли пользователя")
public class UpdateUserDTO {

    @Schema(description = "Новая роль для пользователя", example = "MODERATOR")
    @NotNull(message = "Роль не может быть пустой")
    private Role role;
}