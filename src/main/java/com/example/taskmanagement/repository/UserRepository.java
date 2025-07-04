package com.example.taskmanagement.repository;

import com.example.taskmanagement.model.AppUser;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * <p><b>Репозиторий для Сущности {@link AppUser}</b></p>
 *
 * <p>
 *     Интерфейс Spring Data JPA для выполнения операций с базой данных,
 *     связанных с сущностями {@link AppUser}. Предоставляет стандартные
 *     CRUD-операции и возможность определять кастомные методы поиска.
 * </p>
 *
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see com.example.taskmanagement.model.AppUser
 */
public interface UserRepository extends JpaRepository<AppUser, Long> {

    /**
     * <p><b>Поиск Пользователя по Email</b></p>
     *
     * <p>
     *     Извлекает пользователя по его уникальному адресу электронной почты.
     * </p>
     *
     * <blockquote>
     *     <b>Рекомендация:</b> Использование {@link Optional} является предпочтительным
     *     подходом. Это явно указывает на то, что результат может отсутствовать,
     *     и заставляет вызывающий код обрабатывать этот случай, предотвращая
     *     {@link NullPointerException}.
     * </blockquote>
     *
     * @param email Email для поиска. Не должен быть {@code null}.
     * @return Объект {@link Optional}, содержащий {@link AppUser} если он найден,
     *         иначе пустой {@link Optional}.
     */
    Optional<AppUser> findByEmail(@NotNull String email);

    /**
     * <p><b>Проверка Существования Пользователя по Email</b></p>
     *
     * <p>
     *     Определяет, существует ли в базе данных пользователь с указанным email.
     * </p>
     *
     * <blockquote>
     *     <b>Оптимизация:</b> Этот метод более производителен, чем вызов
     *     <code>findByEmail(...).isPresent()</code>, так как он может быть
     *     преобразован в более легковесный SQL-запрос (например, <code>SELECT 1 ...</code>),
     *     который не требует извлечения всей сущности.
     * </blockquote>
     *
     * @param email Email для проверки. Не должен быть {@code null}.
     * @return {@code true}, если пользователь существует, иначе {@code false}.
     */
    boolean existsByEmail(@NotNull String email);
}