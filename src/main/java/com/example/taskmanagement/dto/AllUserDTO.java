package com.example.taskmanagement.dto;

import com.example.taskmanagement.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи информации о пользователе.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllUserDTO {

    /**
     * ID пользователя.
     */
    private Long id;

    /**
     * Email пользователя.
     */
    private String email;

    /**
     * Роль пользователя.
     */
    private Role role;
}
