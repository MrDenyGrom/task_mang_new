package com.example.taskmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое, когда ресурс не найден.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Конструктор исключения.
     * @param message Сообщение об ошибке.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Конструктор исключения с сообщением по умолчанию.
     */
    public ResourceNotFoundException() {
        super("Ресурс не найден.");
    }
}