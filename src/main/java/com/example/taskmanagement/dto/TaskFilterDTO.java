package com.example.taskmanagement.dto;

import com.example.taskmanagement.model.Priority;
import com.example.taskmanagement.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO для фильтрации задач.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskFilterDTO {

    /**
     * Заголовок задачи.
     */
    private String head;

    /**
     * Описание задачи.
     */
    private String description;

    /**
     * Статус задачи.
     */
    private Status status;

    /**
     * Приоритет задачи.
     */
    private Priority priority;

    /**
     * Имя пользователя автора задачи.
     */
    private String authorUsername;

    /**
     * Имя пользователя исполнителя задачи.
     */
    private String executorUsername;

    /**
     * Дата начала периода для фильтрации.
     */
    private LocalDate startDate;

    /**
     * Дата окончания периода для фильтрации.
     */
    private LocalDate endDate;
}