package com.example.taskmanagement.dto;

import com.example.taskmanagement.model.Priority;
import com.example.taskmanagement.model.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO для задачи.
 */
@Data
public class TaskDTO {

    /**
     * Идентификатор задачи.
     */
    private Long id;

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
     * Email автора задачи.
     */
    private String authorEmail;

    /**
     * Email исполнителя задачи.
     */
    private String executorEmail;

    /**
     * Дата и время создания задачи.
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления задачи.
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Дата выполнения задачи.
     */
    private LocalDate dueDate;
}