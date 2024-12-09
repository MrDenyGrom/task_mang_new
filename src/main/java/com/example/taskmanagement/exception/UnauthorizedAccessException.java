package com.example.taskmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при попытке несанкционированного доступа.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedAccessException extends AuthenticationException {

    /**
     * Конструктор исключения.
     * @param message Сообщение об ошибке.
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
