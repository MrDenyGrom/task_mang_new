package com.example.taskmanagement.config;

import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.model.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Реализация интерфейса {@link UserDetails} для использования с Spring Security.
 *  Этот класс инкапсулирует {@link AppUser} и предоставляет информацию о пользователе,
 *  необходимую для аутентификации и авторизации.
 */
public record UserDetail(AppUser appUser) implements UserDetails {

    /**
     * Получение списка ролей пользователя.
     *
     * @return Коллекция полномочий пользователя.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role userRole = appUser.getRole();
        String roleName = "ROLE_" + userRole.name();
        return List.of(new SimpleGrantedAuthority(roleName));
    }

    /**
     * Получение пароля пользователя.
     *
     * @return Пароль пользователя.
     */
    @Override
    public String getPassword() {
        return appUser.getPassword();
    }

    /**
     * Получение имени пользователя.
     *
     * @return почта пользователя.
     */
    @Override
    public String getUsername() {
        return appUser.getEmail();
    }

    /**
     * Указывает, не истек ли срок действия учетной записи пользователя.
     *
     * @return {@code true} если учетная запись не истекла, {@code false} в противном случае.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Указывает, не заблокирована ли учетная запись пользователя.
     *
     * @return {@code true} если учетная запись не заблокирована, {@code false} в противном случае.
     */
    @Override
    public boolean isAccountNonLocked() {
        return !appUser.isLocked(); }


    /**
     * Указывает, не истек ли срок действия учетных данных пользователя (например, пароля).
     *
     * @return {@code true} если учетные данные не истекли, {@code false} в противном случае.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Указывает, включена ли учетная запись пользователя.
     *
     * @return {@code true} если учетная запись включена, {@code false} в противном случае.
     */
    @Override
    public boolean isEnabled() {
        return appUser.isEnabled();
    }
}