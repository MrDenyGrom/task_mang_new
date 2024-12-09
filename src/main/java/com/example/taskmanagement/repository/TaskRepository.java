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
 * Репозиторий для работы с задачами.
 */
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /**
     * Находит все задачи, срок выполнения которых находится в заданном диапазоне дат.
     * @param startDate Начальная дата диапазона.
     * @param endDate Конечная дата диапазона.
     * @return Список задач.
     */
    List<Task> findByDueDateBetween(@NotNull LocalDate startDate, @NotNull LocalDate endDate);

    /**
     * Находит все задачи, созданные или назначенные указанному пользователю.
     * @param author Автор задачи.
     * @param executor Исполнитель задачи.
     * @return Список задач.
     */
    List<Task> findByAuthorOrExecutor(@NotNull AppUser author, @NotNull AppUser executor);


    /**
     * Находит все задачи с заданным статусом.
     * @param status Статус задачи.
     * @return Список задач.
     */
    List<Task> findByStatus(@NotNull Status status);

    /**
     * Находит все задачи, назначенные указанному исполнителю.
     * @param executor Исполнитель задачи.
     * @return Список задач.
     */
    List<Task> findByExecutor(@NotNull AppUser executor);

    /**
     * Возвращает список идентификаторов всех задач.
     * @return Список идентификаторов задач.
     */
    @Query("SELECT t.id FROM Task t")
    List<Long> getAllTaskIds();

    /**
     * Возвращает список всех задач с загруженными комментариями.
     * @return Список задач с комментариями.
     */
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.comments")
    List<Task> findAllWithComments();
}
