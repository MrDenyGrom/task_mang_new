package com.example.taskmanagement.dto;

import com.example.taskmanagement.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object (DTO) для обновления данных пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDTO {

    /**
     * Роль пользователя.
     */
    @NotNull(message = "Роль не может быть пустой")
    private Role role;
}