package com.example.taskmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * <p><b>DTO: Обновление Комментария</b></p>
 *
 * <p>
 *     Простой объект для передачи текста комментария при его редактировании.
 * </p>
 */
@Getter
@Setter
@Schema(description = "Запрос на обновление существующего комментария")
public class UpdateCommentDTO {

    @Schema(description = "Новый текст комментария. Не может быть пустым и должен быть не менее 3 символов.",
            example = "Работа над задачей успешно завершена!")
    @NotBlank(message = "Текст комментария не может быть пустым.")
    @Size(min = 3, max = 1000, message = "Текст комментария должен быть от 3 до 1000 символов.")
    private String text;
}