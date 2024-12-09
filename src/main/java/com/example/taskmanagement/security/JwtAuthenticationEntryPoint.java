package com.example.taskmanagement.security;

import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Точка входа для аутентификации JWT.
 * Этот класс реализует интерфейс {@link AuthenticationEntryPoint} и используется для обработки
 * несанкционированных запросов, возвращая ответ об ошибке 401 Unauthorized.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Начинает процесс аутентификации.
     * Этот метод вызывается, когда неаутентифицированный пользователь пытается получить доступ к защищенному ресурсу.
     *
     * @param request       {@link HttpServletRequest}  HTTP-запрос.
     * @param response      {@link HttpServletResponse} HTTP-ответ.
     * @param authException {@link AuthenticationException} Исключение аутентификации.
     * @throws IOException Если возникает ошибка ввода-вывода при отправке ответа об ошибке.
     */
    @Override
    public void commence(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                         @NotNull AuthenticationException authException) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Неавторизованный доступ: Недействительные учетные данные или токен");
    }
}