package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.AllUserDTO;
import com.example.taskmanagement.dto.UpdateUserDTO;
import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p><b>Сервис для Управления Пользователями 👤</b></p>
 *
 * <p>
 *     Содержит основную бизнес-логику для всех операций, связанных с пользователями:
 *     регистрация, аутентификация, обновление профиля (смена пароля) и администрирование пользователей.
 * </p>
 *
 * <p><b>Подход к Обработке Ошибок:</b></p>
 * <blockquote>
 *     Сервис активно использует {@link ResponseStatusException} для обработки
 *     всех ожидаемых бизнес-ошибок (например, "пользователь не найден", "неверный пароль", "email уже занят").
 *     Это позволяет контроллерам оставаться "чистыми" и фокусироваться на маршрутизации запросов,
 *     делегируя формирование HTTP-ответов об ошибках фреймворку Spring (через {@code @ControllerAdvice}).
 *     Каждая бизнес-ошибка имеет уникальный код для удобства фронтенд-разработчиков и отладки.
 * </blockquote>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * <p><b>Регистрация Нового Пользователя</b></p>
     * <p>
     *     Создает новую учетную запись пользователя. Перед сохранением,
     *     проверяется уникальность email и хешируется пароль.
     * </p>
     *
     * @param newUser Объект {@link AppUser} с незахешированным паролем и другими данными пользователя.
     * @return Сохраненная сущность {@link AppUser} с сгенерированным ID и хешированным паролем.
     * @throws ResponseStatusException с кодом <b>409 CONFLICT</b> и сообщением `USR-001`,
     *         если пользователь с указанным email уже существует в базе данных.
     */
    @Transactional
    public AppUser registerUser(AppUser newUser) {
        if (userRepository.existsByEmail(newUser.getEmail())) {
            log.warn("🚨 Попытка регистрации с существующим email: {}", newUser.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "USR-001: Пользователь с таким email уже существует");
        }
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        AppUser savedUser = userRepository.save(newUser);
        log.info("✅ Пользователь '{}' успешно зарегистрирован с ID {}", savedUser.getEmail(), savedUser.getId());
        return savedUser;
    }

    /**
     * <p><b>Аутентификация Пользователя</b></p>
     * <p>
     *     Проверяет учетные данные пользователя (email и пароль) через механизм {@link AuthenticationManager}.
     *     В случае успешной проверки генерирует и возвращает JWT токен.
     * </p>
     *
     * @param email    Email пользователя, используемый для аутентификации.
     * @param password Пароль пользователя в открытом виде.
     * @return Сгенерированный JWT (String), который должен быть использован в последующих запросах.
     * @throws ResponseStatusException с кодом <b>401 UNAUTHORIZED</b> и сообщением `AUTH-001`,
     *         если предоставлены неверные учетные данные (email или пароль не совпадают).
     */
    public String authenticateUser(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            log.info("✅ Пользователь '{}' успешно аутентифицирован.", authentication.getName());
            return jwtTokenProvider.generateToken(email);
        } catch (AuthenticationException e) {
            log.warn("❌ Ошибка аутентификации для пользователя '{}': {}", email, e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "AUTH-001: Неверный email или пароль", e);
        }
    }

    /**
     * <p><b>Смена Пароля Пользователя</b></p>
     * <p>
     *     Позволяет пользователю обновить свой пароль. Требует текущего пароля для подтверждения
     *     и нового пароля. Пароль перед сохранением хешируется.
     * </p>
     *
     * @param email       Email текущего пользователя, чей пароль нужно сменить.
     * @param oldPassword Текущий (старый) пароль пользователя в открытом виде. Используется для проверки.
     * @param newPassword Новый пароль пользователя в открытом виде.
     * @throws ResponseStatusException
     *         <ul>
     *             <li><b>404 NOT_FOUND</b> (код ошибки: `USR-002`): если пользователь с указанным email не найден.</li>
     *             <li><b>400 BAD_REQUEST</b> (код ошибки: `USR-003`): если старый пароль указан неверно и не соответствует текущему.</li>
     *             <li><b>409 CONFLICT</b> (код ошибки: `USR-004`): если новый пароль совпадает с текущим (старым) паролем.</li>
     *         </ul>
     */
    @Transactional
    public void updatePassword(String email, String oldPassword, String newPassword) {
        if (oldPassword.equals(newPassword)) {
            log.warn("❌ Попытка смены пароля для пользователя '{}': новый пароль совпадает со старым.", email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "USR-004: Новый пароль не должен совпадать со старым");
        }

        AppUser user = getUserByEmail(email);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("❌ Попытка смены пароля для пользователя '{}': введен неверный старый пароль.", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "USR-003: Неверный старый пароль");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("✅ Пароль для пользователя '{}' успешно обновлен.", email);
    }

    /**
     * <p><b>Получение Пользователя по Email</b></p>
     * <p>
     *     Вспомогательный метод для получения сущности {@link AppUser} по её email.
     *     Используется другими методами сервиса для централизованной обработки случая "пользователь не найден".
     * </p>
     *
     * @param email Email для поиска пользователя.
     * @return Найденная сущность {@link AppUser}.
     * @throws ResponseStatusException с кодом <b>404 NOT_FOUND</b> и сообщением `USR-002`,
     *         если пользователь с указанным email не найден в базе данных.
     */
    @Transactional(readOnly = true)
    public AppUser getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("🔍 Пользователь с email '{}' не найден.", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "USR-002: Пользователь с email '" + email + "' не найден");
                });
    }

    /**
     * <p><b>Получение Списка Всех Пользователей (для админа)</b></p>
     * <p>
     *     Возвращает список всех зарегистрированных пользователей в системе.
     *     Возвращаются только публичные данные (ID, email, роль), без паролей.
     * </p>
     *
     * @return {@link List} DTO объектов {@link AllUserDTO}, представляющих публичную информацию о пользователях.
     */
    @Transactional(readOnly = true)
    public List<AllUserDTO> getAllUserEmailsAndIds() {
        return userRepository.findAll().stream()
                .map(user -> new AllUserDTO(user.getId(), user.getEmail(), user.getRole()))
                .collect(Collectors.toList());
    }

    /**
     * <p><b>Обновление Роли Пользователя (для админа)</b></p>
     * <p>
     *     Обновляет роль существующего пользователя по его ID.
     *     Этот метод предназначен для административных целей, позволяя `ADMIN` пользователям
     *     изменять привилегии других пользователей.
     * </p>
     *
     * @param userId        ID пользователя, чья роль будет обновлена.
     * @param updateUserDTO DTO {@link UpdateUserDTO}, содержащее новую роль пользователя.
     * @return Обновленная сущность {@link AppUser} с новой ролью.
     * @throws ResponseStatusException с кодом <b>404 NOT_FOUND</b> и сообщением `USR-002`,
     *         если пользователь с указанным ID не найден.
     */
    @Transactional
    public AppUser updateUser(Long userId, UpdateUserDTO updateUserDTO) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("❌ Попытка обновления несуществующего пользователя с ID: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "USR-002: Пользователь с ID " + userId + " не найден");
                });
        user.setRole(updateUserDTO.getRole());
        AppUser updatedUser = userRepository.save(user);
        log.info("✅ Роль пользователя с ID {} успешно обновлена на '{}'.", userId, updatedUser.getRole());
        return updatedUser;
    }

    /**
     * <p><b>Удаление Пользователя (для админа)</b></p>
     * <p>
     *     Полностью удаляет пользователя из системы по его уникальному ID.
     *     Действие необратимо.
     * </p>
     *
     * @param userId ID пользователя, которого необходимо удалить.
     * @throws ResponseStatusException с кодом <b>404 NOT_FOUND</b> и сообщением `USR-002`,
     *         если пользователь с указанным ID не найден и, следовательно, не может быть удален.
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("❌ Попытка удаления несуществующего пользователя с ID: {}", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "USR-002: Пользователь с ID " + userId + " не найден");
        }
        userRepository.deleteById(userId);
        log.info("✅ Пользователь с ID {} был успешно удален.", userId);
    }
}