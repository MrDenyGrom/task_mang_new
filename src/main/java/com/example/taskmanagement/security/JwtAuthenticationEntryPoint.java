package com.example.taskmanagement.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.security.config.annotation.web.builders.HttpSecurity.*;

import java.io.IOException;

/**
 * <p><b>Точка Входа для Обработки Ошибок Аутентификации</b></p>
 *
 * <p>
 *     Компонент, который перехватывает запросы к защищенным ресурсам от
 *     <b>неаутентифицированных</b> пользователей (то есть тех, кто не предоставил
 *     валидный токен или не предоставил его вовсе).
 * </p>
 *
 * <p><b>Основная задача:</b></p>
 * <blockquote>
 *     Прервать стандартное поведение Spring Security (которое обычно перенаправляет
 *     на страницу логина) и вместо этого вернуть клиенту четкий и понятный
 *     ответ с кодом <b>401 Unauthorized</b>. Это является стандартом для stateless REST API.
 * </blockquote>
 *
 * @see org.springframework.security.web.AuthenticationEntryPoint
 * @see org.springframework.security.config.annotation.web.builders.HttpSecurity#exceptionHandling()
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * <p><b>Обработка Запроса без Аутентификации</b></p>
     *
     * <p>
     *     Этот метод вызывается Spring Security каждый раз, когда {@link AuthenticationException}
     *     возникает в процессе обработки запроса к защищенному эндпоинту.
     * </p>
     *
     * @param request       Входящий HTTP-запрос.
     * @param response      HTTP-ответ, в который будет записана ошибка.
     * @param authException Исключение, вызвавшее ошибку аутентификации.
     * @throws IOException в случае проблем с записью в {@code response}.
     */
    @Override
    public void commence(@NotNull HttpServletRequest request,
                         @NotNull HttpServletResponse response,
                         @NotNull AuthenticationException authException) throws IOException {
        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Ошибка аутентификации: доступ запрещен. Требуется валидный JWT токен."
        );
    }
}