package com.example.taskmanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * <p><b>Сущность: Пользователь Приложения (AppUser)</b></p>
 *
 * <p>
 *     Представляет собой учетную запись пользователя в системе. Эта сущность
 *     содержит всю необходимую информацию для процессов аутентификации (проверки
 *     личности) и авторизации (определения прав доступа).
 * </p>
 *
 * <p><b>Ключевые архитектурные аспекты:</b></p>
 * <ul>
 *     <li><b>Безопасность:</b> Поле пароля никогда не сериализуется в JSON ({@link JsonIgnore})
 *     и исключено из метода {@code toString()} для предотвращения утечек.</li>
 *     <li><b>Управление доступом:</b> Роль ({@link Role}) является центральным элементом
 *     для реализации Role-Based Access Control (RBAC).</li>
 *     <li><b>Жизненный цикл аккаунта:</b> Флаги {@code enabled} и {@code locked} позволяют
 *     гибко управлять состоянием учетной записи.</li>
 * </ul>
 *
 * @see com.example.taskmanagement.model.Role
 * @see org.springframework.security.core.userdetails.UserDetails
 */
@Entity
@Table(name = "app_users")
@Getter
@Setter
@RequiredArgsConstructor
@ToString(exclude = "password")
@EqualsAndHashCode(of = "id")
public class AppUser {

    /**
     * <p><b>Уникальный Идентификатор</b></p>
     * <p>Первичный ключ, генерируемый базой данных.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * <p><b>Email (Логин)</b></p>
     * <p>
     *     Уникальный адрес электронной почты пользователя. Используется в качестве
     *     основного идентификатора для входа в систему (username).
     * </p>
     */
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат Email")
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * <p><b>Хешированный Пароль</b></p>
     * <p>Пароль пользователя, хранящийся в виде безопасного хеша (например, BCrypt).</p>
     * <blockquote>
     *     <b>Безопасность:</b> Аннотация {@link JsonIgnore} критически важна.
     *     Она гарантирует, что хеш пароля никогда не будет отправлен клиенту
     *     в составе ответа API.
     * </blockquote>
     */
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 8, message = "Пароль должен содержать не менее 8 символов")
    @Column(nullable = false, length = 100)
    @JsonIgnore
    private String password;

    /**
     * <p><b>Статус Активации</b></p>
     * <p>
     *     Флаг, указывающий, активна ли учетная запись. Может использоваться, например,
     *     для механизма подтверждения по email. Пользователи с {@code enabled = false}
     *     не могут пройти аутентификацию.
     * </p>
     */
    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = true;

    /**
     * <p><b>Статус Блокировки</b></p>
     * <p>
     *     Флаг, указывающий, была ли учетная запись заблокирована администратором.
     *     Заблокированные пользователи не могут пройти аутентификацию.
     * </p>
     */
    @Column(name = "is_locked", nullable = false)
    private boolean locked = false;

    /**
     * <p><b>Роль Пользователя</b></p>
     * <p>
     *     Роль в системе (например, USER, ADMIN), которая определяет уровень
     *     доступа пользователя к различным ресурсам и операциям.
     * </p>
     */
    @NotNull(message = "Роль не может быть null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    /**
     * <p><b>Конструктор для Создания Пользователя</b></p>
     * <p>
     *     Удобный конструктор для инстанцирования нового пользователя
     *     с основными учетными данными.
     * </p>
     *
     * @param email    Email пользователя.
     * @param password Хешированный пароль.
     * @param role     Роль пользователя в системе.
     */
    public AppUser(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }
}