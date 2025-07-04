package com.example.taskmanagement.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * <p><b>Токен Аутентификации для JWT</b></p>
 *
 * <p>
 *     Специализированная реализация {@link AbstractAuthenticationToken}, предназначенная
 *     для представления аутентифицированного пользователя в контексте безопасности Spring.
 * </p>
 *
 * <p><b>Ключевые поля:</b></p>
 * <ul>
 *     <li><b>Principal:</b> Основной идентификатор пользователя. В данном случае это
 *     полноценный объект {@link UserDetails}, что позволяет легко получать доступ
 *     ко всей информации о пользователе.</li>
 *     <li><b>Credentials:</b> Учетные данные. Для JWT-аутентификации это поле обычно
 *     устанавливается в {@code null}, так как сам факт валидности токена уже
 *     подтверждает аутентификацию.</li>
 *     <li><b>Authorities:</b> Коллекция ролей и прав пользователя (например, "ROLE_ADMIN").</li>
 * </ul>
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private Object credentials;

    /**
     * <p><b>Конструктор для создания токена</b></p>
     *
     * @param principal   Объект, представляющий пользователя (обычно {@link UserDetails}).
     * @param credentials Учетные данные (обычно {@code null} после аутентификации).
     * @param authorities Коллекция прав доступа (ролей).
     */
    public JwtAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    /**
     * Переопределено для предотвращения случайного стирания учетных данных.
     * Учетные данные (credentials) обнуляются после успешной аутентификации
     * в стандартных реализациях, но для JWT это не всегда требуется.
     */
    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}