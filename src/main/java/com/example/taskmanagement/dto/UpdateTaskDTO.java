package com.example.taskmanagement.dto;

import com.example.taskmanagement.model.Priority;
import com.example.taskmanagement.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * <p><b>DTO: Обновление Задачи</b></p>
 *
 * <p>
 *     Объект для передачи данных при обновлении существующей задачи.
 *     Позволяет клиенту изменять одно или несколько полей задачи.
 * </p>
 */
@Setter
@Getter
@RequiredArgsConstructor
@Schema(description = "Схема для обновления существующей задачи")
public class UpdateTaskDTO {

    @Schema(description = "Новый заголовок задачи", example = "Проверить отчет за Q3")
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(max = 100, message = "Заголовок должен быть короче 100 символов")
    private String title;

    @Schema(description = "Новое описание задачи", example = "Детально сверить все цифры с данными из CRM.")
    @Size(max = 1000, message = "Описание должно быть короче 1000 символов")
    private String description;

    @Schema(description = "Новый статус задачи", example = "IN_PROGRESS")
    @NotNull(message = "Статус не может быть пустым")
    private Status status;

    @Schema(description = "Новый приоритет задачи", example = "HIGH")
    @NotNull(message = "Приоритет не может быть пустым")
    private Priority priority;

    @Schema(description = "Email нового исполнителя задачи", example = "4341@suai.com")
    private String executorUsername;

    @Schema(description = "Новый срок выполнения задачи", example = "2025-06-30")
    private LocalDate dueDate;
}