package com.example.taskmanagement.service;

import com.example.taskmanagement.config.UserDetail;
import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p><b>Сервис для Интеграции с Spring Security 🛡️</b></p>
 *
 * <p>
 *     Этот сервис является ключевым компонентом для механизма аутентификации.
 *     Он реализует интерфейс {@link UserDetailsService}, который Spring Security
 *     использует для получения данных о пользователе в процессе входа в систему.
 * </p>
 *
 * <p><b>Основная задача:</b></p>
 * <blockquote>
 *     Найти пользователя в базе данных по его имени (в данном случае, по email)
 *     и предоставить Spring Security объект {@link UserDetails}, содержащий
 *     всю необходимую информацию для проверки учетных данных и определения прав доступа (ролей).
 * </blockquote>
 *
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see com.example.taskmanagement.config.UserDetail
 * @see com.example.taskmanagement.model.AppUser
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * <p><b>Загрузка Данных Пользователя по Email 📧</b></p>
     * <p>
     *     Центральный метод, который вызывается Spring Security во время аутентификации.
     *     Он ищет пользователя в репозитории по предоставленному email.
     * </p>
     * <blockquote>
     *     <p><b>Важность транзакции:</b> Аннотация {@code @Transactional(readOnly = true)}
     *     гарантирует, что все операции с базой данных, включая ленивую загрузку
     *     связанных сущностей (например, ролей пользователя), будут выполнены в рамках
     *     одной активной сессии Hibernate. Это предотвращает {@code LazyInitializationException}.</p>
     * </blockquote>
     *
     * @param email Email пользователя, который пытается войти в систему.
     *              Spring Security передает сюда значение из поля "username" формы входа.
     *
     * @return {@link UserDetails} — объект-обертка {@link UserDetail},
     *         содержащий сущность {@link AppUser} и всю необходимую для Spring Security
     *         информацию (пароль, роли, статус аккаунта).
     *
     * @throws UsernameNotFoundException если пользователь с указанным email не был найден
     *         в базе данных. Это стандартное исключение Spring Security, которое
     *         корректно обрабатывается фреймворком.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@NotNull String email) throws UsernameNotFoundException {
        log.debug("📢 Поиск пользователя по email '{}' для аутентификации.", email);

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.debug("❌ Попытка аутентификации с несуществующим email: '{}'", email);
                    return new UsernameNotFoundException("Пользователь с email '" + email + "' не найден.");
                });

        log.debug("✅ Пользователь '{}' (ID: {}) успешно найден для аутентификации.", user.getEmail(), user.getId());
        return new UserDetail(user);
    }
}