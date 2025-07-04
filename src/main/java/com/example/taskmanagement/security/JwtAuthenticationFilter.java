package com.example.taskmanagement.security;

import com.example.taskmanagement.config.UserDetail;
import com.example.taskmanagement.service.UserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * <p><b>Центральный Фильтр JWT Аутентификации</b></p>
 *
 * <p>
 *     Этот фильтр является ядром механизма аутентификации по JWT. Он выполняется
 *     <b>один раз для каждого входящего запроса</b> и отвечает за проверку,
 *     валидацию и установку аутентификации пользователя в контекст безопасности Spring.
 * </p>
 *
 * <p><b>Алгоритм работы:</b></p>
 * <ol>
 *     <li>Извлекает токен из заголовка {@code Authorization: Bearer <token>}.</li>
 *     <li>Если токен найден, валидирует его с помощью {@link JwtTokenProvider}.</li>
 *     <li>Извлекает email пользователя из валидного токена.</li>
 *     <li>Загружает данные пользователя ({@link UserDetail}) из базы данных через {@link UserDetailService}.</li>
 *     <li>Создает объект аутентификации ({@link JwtAuthenticationToken}) и помещает его
 *     в {@link SecurityContextHolder}.</li>
 * </ol>
 *
 * <p>
 *     После успешного выполнения этих шагов, пользователь считается аутентифицированным
 *     для текущего запроса, и последующие компоненты Spring Security (например, для
 *     проверки ролей через {@code @PreAuthorize}) могут корректно работать.
 * </p>
 *
 * @see org.springframework.web.filter.OncePerRequestFilter
 * @see org.springframework.security.core.context.SecurityContextHolder
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailService userDetailsService;

    /**
     * <p><b>Основная Логика Фильтрации</b></p>
     *
     * <p>
     *     Применяет логику JWT-аутентификации к входящему запросу.
     * </p>
     *
     * @param request     HTTP-запрос.
     * @param response    HTTP-ответ.
     * @param filterChain Цепочка фильтров для передачи запроса дальше.
     * @throws ServletException при ошибках сервлета.
     * @throws IOException      при ошибках ввода-вывода.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = getJwtFromRequest(request);

            if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
                String email = tokenProvider.getEmailFromJWT(token);

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetail userDetails = (UserDetail) userDetailsService.loadUserByUsername(email);

                    JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Пользователь '{}' успешно аутентифицирован по JWT.", email);
                }
            }
        } catch (Exception ex) {
            log.warn("Не удалось установить аутентификацию пользователя в контексте безопасности: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * <p><b>Извлечение JWT из Запроса</b></p>
     *
     * <p>
     *     Парсит заголовок {@code Authorization} и извлекает из него "чистый" токен,
     *     удаляя префикс "Bearer ".
     * </p>
     *
     * @param request HTTP-запрос.
     * @return Строка с JWT или {@code null}, если заголовок отсутствует или имеет неверный формат.
     */
    private String getJwtFromRequest(@NotNull HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}