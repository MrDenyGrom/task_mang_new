package com.example.taskmanagement.service;

import com.example.taskmanagement.config.UserDetail;
import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Сервис для загрузки информации о пользователе для Spring Security.
 */
@Service
public class UserDetailService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailService.class);

    private final UserRepository userRepository;

    /**
     * Конструктор сервиса.
     * @param userRepository Репозиторий пользователей.
     */
    public UserDetailService(@NotNull UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Загружает информацию о пользователе по имени пользователя (email).
     * @param email Email пользователя.
     * @return UserDetails объект, содержащий информацию о пользователе.
     * @throws UsernameNotFoundException Если пользователь с указанным email не найден.
     */
    @Override
    public UserDetails loadUserByUsername(@NotNull String email) throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email '" + email + "' не найден"));
        log.debug("Найден пользователь: {}", user.getEmail());
        return new UserDetail(user);
    }
}