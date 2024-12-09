package com.example.taskmanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Сущность, представляющий пользователя приложения.
 */
@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class AppUser {

    /**
     * Идентификатор пользователя.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email пользователя. Должен быть уникальным и не пустым.
     */
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат Email")
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Пароль пользователя. Не может быть пустым.
     */
    @NotBlank(message = "Пароль не может быть пустым")
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    /**
     * Флаг, указывающий, активен ли пользователь. По умолчанию {@code true}.
     */
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Флаг, указывающий, заблокирован ли пользователь. По умолчанию {@code false}.
     */
    @Column(nullable = false)
    private boolean locked = false;

    /**
     * Роль пользователя. По умолчанию {@link Role#USER}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

}