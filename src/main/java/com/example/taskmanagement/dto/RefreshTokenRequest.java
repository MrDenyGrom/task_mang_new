package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса обновления refresh токена.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    /**
     * Refresh токен.
     */
    @NotBlank(message = "RefreshToken не может быть пустым")
    private String refreshToken;
}