package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p><b>Глобальный Обработчик Исключений</b></p>
 * <p>
 *     Единая точка для централизованной обработки всех исключений, возникающих в приложении.
 *     Гарантирует, что клиенты всегда получают стандартизированный и информативный ответ об ошибке в формате {@link ErrorResponseDto}.
 *     Логирует все ошибки для дальнейшего анализа.
 * </p>
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * <p><b>Обработка бизнес-исключений ({@link ResponseStatusException}) 🚦</b></p>
     * <p>
     *     Ловит исключения, брошенные из сервисного слоя (наша бизнес-логика),
     *     например, "пользователь не найден", "email уже занят", "неверный пароль".
     *     Эти исключения содержат HTTP статус и сообщение, которые непосредственно
     *     передаются в ответ клиенту.
     * </p>
     *
     * @param ex {@link ResponseStatusException} - исключение, содержащее HTTP статус и причину.
     * @param request {@link HttpServletRequest} - текущий HTTP запрос.
     * @return {@link ResponseEntity} с {@link ErrorResponseDto} и соответствующим HTTP статусом.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = (HttpStatus) ex.getStatusCode();
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getReason())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.warn("🚨 Бизнес-исключение перехвачено: Статус={} - '{}', Сообщение='{}', Путь='{}'",
                status.value(), status.getReasonPhrase(), ex.getReason(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * <p><b>Обработка ошибок валидации ({@link MethodArgumentNotValidException}) 📝</b></p>
     * <p>
     *     Срабатывает, когда входящие запросы не проходят валидацию (например, из-за аннотаций `@Valid`, `@NotBlank`, `@Size`).
     *     Возвращает статус 400 Bad Request и подробный список всех полей, вызвавших ошибку,
     *     чтобы клиент мог точно понять, что нужно исправить.
     * </p>
     *
     * @param ex {@link MethodArgumentNotValidException} - исключение, содержащее ошибки валидации.
     * @param request {@link HttpServletRequest} - текущий HTTP запрос.
     * @return {@link ResponseEntity} с {@link ErrorResponseDto} и списком {@link ErrorResponseDto.ErrorDetail}.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<ErrorResponseDto.ErrorDetail> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = (error instanceof FieldError) ? ((FieldError) error).getField() : error.getObjectName();
                    return ErrorResponseDto.ErrorDetail.builder()
                            .field(fieldName)
                            .message(error.getDefaultMessage())
                            .build();
                })
                .collect(Collectors.toList());

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message("Ошибка валидации входных данных. Проверьте поля запроса.")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .details(errors)
                .build();

        log.warn("⚠️ Ошибка валидации данных запроса: Статус={} - '{}', Количество ошибок={}. Путь='{}'",
                status.value(), status.getReasonPhrase(), errors.size(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * <p><b>Обработка ошибок целостности данных ({@link DataIntegrityViolationException}) 🔗</b></p>
     * <p>
     *     Перехватывает исключения, связанные с нарушением ограничений базы данных,
     *     например, попытка вставить запись с неуникальным значением в уникальное поле
     *     (часто это происходит при попытке зарегистрировать пользователя с уже существующим email).
     *     Возвращает статус 409 Conflict.
     * </p>
     *
     * @param ex {@link DataIntegrityViolationException} - исключение о нарушении целостности данных.
     * @param request {@link HttpServletRequest} - текущий HTTP запрос.
     * @return {@link ResponseEntity} с {@link ErrorResponseDto} и HTTP статусом 409 Conflict.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        String errorMessage = "Конфликт данных: Запись с такими уникальными данными уже существует или нарушено ограничение целостности.";

        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            String rootCauseMessage = ex.getCause().getMessage();
            if (rootCauseMessage.contains("duplicate key") || rootCauseMessage.contains("unique constraint")) {
                errorMessage = "Конфликт: Элемент с такими данными уже существует. " + rootCauseMessage.substring(0, Math.min(rootCauseMessage.length(), 100)) + "...";
            }
        }

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(errorMessage)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("💥 Ошибка целостности данных: Статус={} - '{}', Сообщение='{}', Путь='{}', Причина='{}'",
                status.value(), status.getReasonPhrase(), errorMessage, request.getRequestURI(), ex.getMessage());
        return new ResponseEntity<>(errorResponse, status);
    }


    /**
     * <p><b>Универсальный обработчик для всех остальных исключений ({@link Exception}) 🐞</b></p>
     * <p>
     *     Это "запасной" обработчик, который перехватывает любые другие непредвиденные ошибки,
     *     которые не были пойманы более специфичными обработчиками.
     *     Возвращает статус 500 Internal Server Error, скрывая внутренние детали для клиента,
     *     но логируя полный стек вызовов для отладки.
     * </p>
     *
     * @param ex {@link Exception} - любое другое необработанное исключение.
     * @param request {@link HttpServletRequest} - текущий HTTP запрос.
     * @return {@link ResponseEntity} с {@link ErrorResponseDto} и HTTP статусом 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAllUncaughtExceptions(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message("Произошла непредвиденная ошибка сервера. Пожалуйста, попробуйте позже или обратитесь в службу поддержки.")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("❌ Непредвиденная ошибка сервера: Статус={} - '{}', Путь='{}'",
                status.value(), status.getReasonPhrase(), request.getRequestURI(), ex);
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * <p><b>Обработка ошибок доступа ({@link AccessDeniedException}) 🚫</b></p>
     * <p>
     *     Перехватывает исключения Spring Security, возникающие, когда аутентифицированный пользователь
     *     (имеющий действительный токен) пытается получить доступ к ресурсу, на который у него
     *     нет достаточных прав (например, эндпоинты, защищенные `@PreAuthorize("hasRole('ADMIN')")`,
     *     если пользователь не является администратором).
     *     Возвращает HTTP статус **403 Forbidden**.
     * </p>
     *
     * @param ex {@link AccessDeniedException} - исключение, указывающее на отказ в доступе.
     * @param request {@link HttpServletRequest} - текущий HTTP запрос.
     * @return {@link ResponseEntity} с {@link ErrorResponseDto} и HTTP статусом 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message("Доступ запрещен: у вас недостаточно прав для выполнения этой операции.")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.warn("🚫 Доступ запрещен: Статус={} - '{}', Сообщение='{}', Путь='{}'",
                status.value(), status.getReasonPhrase(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, status);
    }
}