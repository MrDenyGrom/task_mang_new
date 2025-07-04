package com.example.taskmanagement.dto;

import com.example.taskmanagement.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * <p><b>DTO: Представление Пользователя (Ответ)</b></p>
 *
 * <p>
 *     Безопасный объект для передачи публичной информации о пользователе.
 *     Используется, например, в списках пользователей, доступных администратору.
 *     Не содержит конфиденциальных данных, таких как пароль.
 * </p>
 */
@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Schema(description = "Схема для отображения публичной информации о пользователе")
public class AllUserDTO {

    @Schema(description = "Уникальный идентификатор пользователя", example = "101")
    private Long id;

    @Schema(description = "Email пользователя", example = "zamstarost4343@suai.com")
    private String email;

    @Schema(description = "Роль пользователя в системе", example = "USER")
    private Role role;
}