package com.example.taskmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое, когда пользователь с таким именем уже существует.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Конструктор исключения.
     * @param message Сообщение об ошибке.
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}