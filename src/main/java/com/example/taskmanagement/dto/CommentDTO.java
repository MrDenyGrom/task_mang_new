package com.example.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для комментария.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {

    /**
     * Текст комментария. Не может быть пустым и должен быть не длиннее 2000 символов.
     */
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 2000, message = "Текст комментария должен быть короче 2000 символов")
    private String text;
}