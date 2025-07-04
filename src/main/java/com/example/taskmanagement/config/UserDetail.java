package com.example.taskmanagement.config;

import com.example.taskmanagement.model.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * <p><b>Адаптер Пользователя для Spring Security</b></p>
 *
 * <p>
 *     Представляет собой неизменяемую (immutable) оболочку (wrapper) над доменной моделью {@link AppUser},
 *     адаптируя ее под контракт интерфейса {@link UserDetails} из Spring Security.
 *     Использование {@code java.lang.Record} обеспечивает лаконичность и потокобезопасность.
 * </p>
 *
 * <p>
 *     Этот класс служит мостом между бизнес-логикой ({@code AppUser}) и фреймворком
 *     безопасности, предоставляя последнему необходимую информацию о пользователе
 *     в стандартизированном виде.
 * </p>
 *
 * @param appUser Оригинальный объект сущности пользователя. Не может быть {@code null}.
 *
 * @see org.springframework.security.core.userdetails.UserDetails
 * @see com.example.taskmanagement.model.AppUser
 */
public record UserDetail(AppUser appUser) implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";

    public UserDetail {
        Objects.requireNonNull(appUser, "Объект AppUser не может быть null");
    }

    /**
     * <p><b>Полномочия (Роли) Пользователя</b></p>
     *
     * <p>
     *     Возвращает коллекцию ролей, предоставленных пользователю. Роль из
     *     {@link AppUser} преобразуется в {@link SimpleGrantedAuthority}
     *     с обязательным префиксом {@code ROLE_}, как того требует Spring Security.
     * </p>
     *
     * @return Коллекция из одного полномочия или пустая коллекция, если роль не задана. Никогда не {@code null}.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Optional.ofNullable(appUser.getRole())
                .map(role -> ROLE_PREFIX + role.name())
                .map(SimpleGrantedAuthority::new)
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

    /**
     * <p><b>Пароль Пользователя</b></p>
     *
     * @return Хешированный пароль пользователя из объекта {@link AppUser}.
     */
    @Override
    public String getPassword() {
        return appUser.getPassword();
    }

    /**
     * <p><b>Имя Пользователя (Логин)</b></p>
     *
     * <p>В качестве уникального идентификатора для аутентификации используется email.</p>
     *
     * @return Email пользователя.
     */
    @Override
    public String getUsername() {
        return appUser.getEmail();
    }

    /**
     * <p><b>Статус: Срок Действия Учетной Записи</b></p>
     *
     * @return {@code true}, так как в данной реализации учетные записи бессрочны.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * <p><b>Статус: Блокировка Учетной Записи</b></p>
     *
     * @return {@code true}, если учетная запись не заблокирована.
     */
    @Override
    public boolean isAccountNonLocked() {
        return !appUser.isLocked();
    }

    /**
     * <p><b>Статус: Срок Действия Учетных Данных</b></p>
     *
     * @return {@code true}, так как в данной реализации пароли бессрочны.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * <p><b>Статус: Активация Учетной Записи</b></p>
     *
     * @return {@code true}, если учетная запись активирована (например, через email).
     */
    @Override
    public boolean isEnabled() {
        return appUser.isEnabled();
    }
}