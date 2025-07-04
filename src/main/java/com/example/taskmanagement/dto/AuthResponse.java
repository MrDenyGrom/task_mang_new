package com.example.taskmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <p>DTO для ответа, содержащего JWT токен после успешной аутентификации.</p>
 */
@Data
@AllArgsConstructor
@Schema(description = "Ответ с JWT токеном для аутентификации")
public class AuthResponse {

    @Schema(description = "Токен доступа типа Bearer. Используйте его в заголовке `Authorization: Bearer <token>`.",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjQ1NjczODAwLCJleHAiOjE2NDU2NzU2MDB9.signature...")
    private String token;
}