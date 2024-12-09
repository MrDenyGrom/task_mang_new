package com.example.taskmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое, когда задача не найдена.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TaskNotFoundException extends RuntimeException {

    /**
     * Конструктор исключения.
     * @param message Сообщение об ошибке.
     */
    public TaskNotFoundException(String message) {
        super(message);
    }
}