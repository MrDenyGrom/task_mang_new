package com.example.taskmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * <p><b>DTO: Запрос на Обновление Токена</b></p>
 *
 * <p>
 *     Используется для запроса новой пары access/refresh токенов,
 *     предоставляя валидный, неистекший refresh токен.
 * </p>
 */
@Setter
@Getter
@RequiredArgsConstructor
@Schema(description = "Схема для запроса обновления JWT токенов")
public class RefreshTokenRequest {

    @Schema(description = "Действующий refresh токен, полученный при аутентификации",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIi...")
    @NotBlank(message = "RefreshToken не может быть пустым")
    private String refreshToken;
}