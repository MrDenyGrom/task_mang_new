package com.example.taskmanagement.dto;

import com.example.taskmanagement.model.Priority;
import com.example.taskmanagement.model.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * <p><b>DTO: Создание Задачи</b></p>
 *
 * <p>
 *     Объект, содержащий все необходимые данные для создания новой задачи.
 *     Используется как тело запроса (request body).
 * </p>
 */
@Setter
@Getter
@RequiredArgsConstructor
@Schema(description = "Схема для создания новой задачи")
public class CreateTaskDTO {

    @Schema(description = "Заголовок задачи", example = "Подготовить презентацию для преподавателя по ОП", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Заголовок задачи не может быть пустым")
    @Size(max = 100, message = "Заголовок задачи должен быть короче 100 символов")
    private String title;

    @Schema(description = "Подробное описание задачи", example = "Собрать ключевые моменты за последний месяц.")
    @Size(max = 1000, message = "Описание задачи должно быть короче 1000 символов")
    private String description;

    @Schema(description = "Начальный статус задачи", example = "WAITING", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Статус задачи не может быть пустым")
    private Status status;

    @Schema(description = "Приоритет задачи", example = "HIGH", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Приоритет задачи не может быть пустым")
    private Priority priority;

    @Schema(description = "Email пользователя, которому назначается задача", example = "zamstarost4343@suai.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Имя пользователя не может быть пустым")
    private String executorUsername;

    @Schema(description = "Планируемая дата выполнения", example = "2025-07-20")
    @FutureOrPresent(message = "Дата выполнения должна быть в настоящем или будущем")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}