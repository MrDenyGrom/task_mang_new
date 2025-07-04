package com.example.taskmanagement.repository;

import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.model.Status;
import com.example.taskmanagement.model.Task;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

/**
 * <p><b>Репозиторий для Сущности {@link Task}</b></p>
 *
 * <p>
 *     Предоставляет методы для работы с задачами в базе данных.
 * </p>
 *
 * <p><b>Расширенные возможности:</b></p>
 * <blockquote>
 *     Наследование от {@link JpaSpecificationExecutor} позволяет конструировать
 *     сложные, динамические запросы с использованием Criteria API. Это особенно
 *     полезно для реализации гибких фильтров и поиска по множеству критериев.
 * </blockquote>
 *
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see org.springframework.data.jpa.repository.JpaSpecificationExecutor
 * @see com.example.taskmanagement.model.Task
 */
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /**
     * <p><b>Поиск Задач по Диапазону Дат Выполнения</b></p>
     *
     * @param startDate Начальная дата поиска (включительно).
     * @param endDate   Конечная дата поиска (включительно).
     * @return Список задач {@link Task}, у которых {@code dueDate} попадает в указанный диапазон.
     */
    List<Task> findByDueDateBetween(@NotNull LocalDate startDate, @NotNull LocalDate endDate);

    /**
     * <p><b>Поиск Задач по Автору или Исполнителю</b></p>
     *
     * @param author   Пользователь, который может быть автором.
     * @param executor Пользователь, который может быть исполнителем.
     * @return Список задач {@link Task}, где указанный пользователь является либо автором, либо исполнителем.
     */
    List<Task> findByAuthorOrExecutor(@NotNull AppUser author, @NotNull AppUser executor);

    /**
     * <p><b>Поиск Задач по Статусу</b></p>
     *
     * @param status Статус задачи для поиска.
     * @return Список задач {@link Task} с указанным статусом.
     */
    List<Task> findByStatus(@NotNull Status status);

    /**
     * <p><b>Поиск Задач по Исполнителю</b></p>
     *
     * @param executor Пользователь-исполнитель.
     * @return Список задач {@link Task}, назначенных данному исполнителю.
     */
    List<Task> findByExecutor(@NotNull AppUser executor);

    /**
     * <p><b>Получение Идентификаторов Всех Задач (Проекция)</b></p>
     *
     * <blockquote>
     *     <b>Оптимизация:</b> Этот метод является проекцией и извлекает из базы
     *     данных только идентификаторы задач. Он значительно эффективнее,
     *     чем получение полного списка сущностей {@code List<Task>}, когда
     *     требуются только их ID.
     * </blockquote>
     *
     * @return Список идентификаторов {@link Long} всех существующих задач.
     */
    @Query("SELECT t.id FROM Task t")
    List<Long> getAllTaskIds();

    /**
     * <p><b>Получение Задач с Комментариями (Eager Fetch)</b></p>
     *
     * <blockquote>
     *     <b>Решение проблемы "N+1":</b> Использование {@code LEFT JOIN FETCH}
     *     гарантирует, что связанные комментарии будут загружены в одном
     *     SQL-запросе вместе с задачами. Это предотвращает ленивую загрузку
     *     комментариев для каждой задачи по отдельности и решает проблему "N+1 запроса".
     * </blockquote>
     *
     * @return Список {@link Task} с полностью инициализированной коллекцией комментариев.
     */
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.comments")
    List<Task> findAllWithComments();
}