package com.example.taskmanagement.dto;

import com.example.taskmanagement.model.Priority;
import com.example.taskmanagement.model.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO для обновления задачи.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskDTO {

    /**
     * Заголовок задачи. Не может быть пустым и должен быть короче 100 символов.
     */
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(max = 100, message = "Заголовок должен быть короче 100 символов")
    private String head;

    /**
     * Описание задачи. Должно быть короче 1000 символов.
     */
    @Size(max = 1000, message = "Описание должно быть короче 1000 символов")
    private String description;

    /**
     * Статус задачи. Не может быть пустым.
     */
    @NotNull(message = "Статус не может быть пустым")
    private Status status;

    /**
     * Приоритет задачи. Не может быть пустым.
     */
    @NotNull(message = "Приоритет не может быть пустым")
    private Priority priority;

    /**
     * Имя пользователя исполнителя задачи.
     */
    private String executorUsername;

    /**
     * Срок выполнения задачи.
     */
    private LocalDate dueDate;
}