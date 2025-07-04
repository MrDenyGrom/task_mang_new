package com.example.taskmanagement.dto;

import com.example.taskmanagement.model.Priority;
import com.example.taskmanagement.model.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p><b>DTO: Представление Задачи (Ответ)</b></p>
 *
 * <p>
 *     Стандартизированный и безопасный объект для передачи информации о задаче клиенту.
 *     Используется в качестве тела HTTP-ответа (response body).
 * </p>
 *
 * <blockquote>
 *     В отличие от сущности {@code Task}, этот DTO предоставляет "плоскую" структуру,
 *     заменяя полные объекты автора и исполнителя на их email-адреса,
 *     что упрощает использование на клиенте и уменьшает объем трафика.
 * </blockquote>
 */
@Setter
@Getter
@RequiredArgsConstructor
@Schema(description = "Схема для отображения полной информации о задаче")
public class TaskDTO {

    @Schema(description = "Уникальный идентификатор задачи", example = "42")
    private Long id;

    @Schema(description = "Заголовок задачи", example = "Аттестация")
    private String title;

    @Schema(description = "Подробное описание задачи", example = "Собрать аттестационные ведомости у всех преподавателей")
    private String description;

    @Schema(description = "Текущий статус задачи", example = "COMPLETED")
    private Status status;

    @Schema(description = "Приоритет задачи", example = "CRITICAL")
    private Priority priority;

    @Schema(description = "Email автора задачи", example = "starost4343@suai.com")
    private String author;

    @Schema(description = "Email исполнителя задачи", example = "zamstarost4343@suai.com")
    private String executor;

    @Schema(description = "Дата и время создания задачи", example = "2025-06-29 23:33:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Дата и время последнего обновления задачи", example = "2025-06-29 23:45:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "Планируемый срок выполнения задачи", example = "2025-07-20")
    private LocalDate dueDate;
}