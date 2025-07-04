package com.example.taskmanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Стандартизированный ответ об ошибке, возвращаемый при сбоях API")
public class ErrorResponseDto {

    @Schema(description = "HTTP статус код ошибки. Например, 400, 401, 403, 404, 409, 500.", example = "409")
    private int status;

    @Schema(description = "Краткое описание HTTP статуса. Например, 'Bad Request', 'Unauthorized', 'Conflict', 'Internal Server Error'.", example = "Conflict")
    private String error;

    @Schema(description = "Человекочитаемое сообщение об ошибке, часто с внутренним кодом ошибки для легкой идентификации. Например, 'USR-001: Пользователь с таким email уже существует'.", example = "USR-001: Пользователь с таким email уже существует")
    private String message;

    @Schema(description = "Полный путь URL, на котором произошла ошибка.", example = "/api/users/register")
    private String path;

    @Schema(description = "Временная метка, когда произошла ошибка. Формат: YYYY-MM-DDTHH:mm:ss.SSS.", example = "2025-07-05T10:00:00.123")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "Список детальных ошибок валидации (только для ошибок типа 400 Bad Request при неверных входных данных).", example = "[{\"field\": \"email\", \"message\": \"Неверный формат email\"}]")
    private List<ErrorDetail> details;

    /**
     * Вложенный класс для детализации ошибок валидации по полям.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "Детальная информация об ошибке валидации конкретного поля")
    public static class ErrorDetail {
        @Schema(description = "Имя поля, в котором произошла ошибка валидации.", example = "password")
        private String field;

        @Schema(description = "Сообщение об ошибке валидации для данного поля.", example = "Пароль должен содержать не менее 8 символов.")
        private String message;
    }
}