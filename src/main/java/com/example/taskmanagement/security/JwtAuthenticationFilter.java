package com.example.taskmanagement.security;

import com.example.taskmanagement.config.UserDetail;
import com.example.taskmanagement.service.UserDetailService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Фильтр аутентификации JWT.
 * Этот фильтр перехватывает каждый запрос и проверяет наличие JWT в заголовке Authorization.
 * Если JWT найден, он извлекает адрес электронной почты пользователя из токена и загружает
 * сведения о пользователе из базы данных. Если пользователь найден, создается токен
 * аутентификации JWT и устанавливается в контекст безопасности Spring.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final UserDetailService userDetailsService;

    public JwtAuthenticationFilter(@NotNull JwtTokenProvider tokenProvider, @NotNull UserDetailService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Выполняет фильтрацию запроса.
     *
     * @param request     {@link HttpServletRequest}  HTTP-запрос.
     * @param response    {@link HttpServletResponse} HTTP-ответ.
     * @param filterChain {@link FilterChain}        Цепочка фильтров.
     * @throws ServletException Если возникает ошибка сервлета.
     * @throws IOException      Если возникает ошибка ввода-вывода.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = getJwtFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                String email = tokenProvider.getEmailFromJWT(token);
                UserDetail userDetails = (UserDetail) userDetailsService.loadUserByUsername(email);

                if (userDetails != null) {
                    JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Пользователь {} аутентифицирован с помощью JWT", email);
                }
            } catch (Exception ex) {
                log.warn("Ошибка аутентификации с помощью JWT: {}", ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Извлекает JWT из заголовка Authorization запроса.
     *
     * @param request {@link HttpServletRequest} HTTP-запрос.
     * @return JWT или null, если JWT не найден.
     */
    public static String getJwtFromRequest(@NotNull HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}