package com.example.taskmanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * <p><b>DTO: Представление Комментария (Ответ)</b></p>
 *
 * <p>
 *     Безопасный объект для передачи публичной информации о комментарии.
 *     Используется, например, в списках комментариев к задаче, доступных пользователям.
 *     Не содержит конфиденциальных данных.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Представление комментария к задаче")
public class CommentDTO {

    @Schema(description = "Уникальный идентификатор комментария", example = "101")
    private Long id;

    @Schema(description = "ID задачи, к которой относится комментарий", example = "1")
    private Long taskId;

    @Schema(description = "Текст комментария", example = "Замечательный прогресс!")
    private String text;

    @Schema(description = "Информация об авторе комментария")
    private String author;

    @Schema(description = "Дата и время создания комментария", example = "2025-07-04T15:00:00.123")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @Schema(description = "Дата и время последнего обновления комментария", example = "2025-07-04T16:00:00.456")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
}