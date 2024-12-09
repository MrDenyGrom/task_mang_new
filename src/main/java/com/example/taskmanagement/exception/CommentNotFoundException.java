package com.example.taskmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое, когда комментарий не найден.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CommentNotFoundException extends RuntimeException {

    /**
     * Конструктор исключения.
     * @param message Сообщение об ошибке.
     */
    public CommentNotFoundException(String message) {
        super(message);
    }
}