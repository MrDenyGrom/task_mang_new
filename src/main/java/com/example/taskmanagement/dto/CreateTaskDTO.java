package com.example.taskmanagement.dto;

import com.example.taskmanagement.model.Priority;
import com.example.taskmanagement.model.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO для создания задачи.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskDTO {

    /**
     * Заголовок задачи.
     */
    @NotBlank(message = "Заголовок задачи не может быть пустым")
    @Size(max = 100, message = "Заголовок задачи должен быть короче 100 символов")
    private String head;

    /**
     * Описание задачи.
     */
    @Size(max = 1000, message = "Описание задачи должно быть короче 1000 символов")
    private String description;

    /**
     * Статус задачи.
     */
    @NotNull(message = "Статус задачи не может быть пустым")
    private Status status;

    /**
     * Приоритет задачи.
     */
    @NotNull(message = "Приоритет задачи не может быть пустым")
    private Priority priority;

    /**
     * Имя пользователя, которому назначена задача.
     */
    @NotBlank(message = "Имя пользователя не может быть пустым")
    private String executorUsername;

    /**
     * Дата выполнения задачи.
     */
    @FutureOrPresent(message = "Дата выполнения должна быть в настоящем или будущем")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}