package com.example.taskmanagement.dto;

import com.example.taskmanagement.model.Priority;
import com.example.taskmanagement.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * <p><b>DTO: Фильтр для Поиска Задач</b></p>
 *
 * <p>
 *     Агрегирует все возможные параметры для динамического поиска и фильтрации задач.
 *     Все поля являются необязательными. Сервер будет строить запрос на основе
 *     тех полей, которые были переданы клиентом.
 * </p>
 */
@Setter
@Getter
@RequiredArgsConstructor
@Schema(description = "Объект с необязательными параметрами для фильтрации и поиска задач")
public class TaskFilterDTO {

    @Schema(description = "Фильтр по частичному совпадению в заголовке", example = "Отчет")
    private String title;

    @Schema(description = "Фильтр по частичному совпадению в описании", example = "CRM")
    private String description;

    @Schema(description = "Фильтр по точному статусу задачи", example = "IN_PROGRESS")
    private Status status;

    @Schema(description = "Фильтр по точному приоритету задачи", example = "HIGH")
    private Priority priority;

    @Schema(description = "Фильтр по email автора задачи", example = "4343@suai.com")
    private String authorUsername;

    @Schema(description = "Фильтр по email исполнителя задачи", example = "4341@suai.com")
    private String executorUsername;

    @Schema(description = "Начальная дата для фильтрации по сроку выполнения (включительно)", example = "2025-06-30")
    private LocalDate startDate;

    @Schema(description = "Конечная дата для фильтрации по сроку выполнения (включительно)", example = "2025-07-29")
    private LocalDate endDate;
}