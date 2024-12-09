package com.example.taskmanagement.security;

import jakarta.validation.constraints.NotNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Представляет токен аутентификации JWT.
 * Этот класс расширяет {@link AbstractAuthenticationToken} и используется для хранения
 * принципала и учетных данных пользователя, аутентифицированного с помощью JWT.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final Object credentials;

    /**
     * Создает новый {@link JwtAuthenticationToken}.
     *
     * @param principal   Принципал аутентификации (обычно имя пользователя или идентификатор).
     * @param credentials Учетные данные аутентификации (обычно JWT).
     * @param authorities Коллекция предоставленных полномочий.
     */
    public JwtAuthenticationToken(@NotNull Object principal, @NotNull Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(authorities != null && !authorities.isEmpty());
    }

    /**
     * Возвращает учетные данные аутентификации.
     *
     * @return Учетные данные аутентификации.
     */
    @Override
    public Object getCredentials() {
        return credentials;
    }

    /**
     * Возвращает принципала аутентификации.
     *
     * @return Принципал аутентификации.
     */
    @Override
    public Object getPrincipal() {
        return principal;
    }
}