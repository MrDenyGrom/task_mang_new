package com.example.taskmanagement.repository;

import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.model.Comment;
import com.example.taskmanagement.model.Task;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * <p><b>Репозиторий для Сущности {@link Comment}</b></p>
 *
 * <p>
 *     Обеспечивает взаимодействие с базой данных для сущностей {@link Comment}.
 *     Предоставляет как стандартные CRUD-операции, так и специализированные
 *     методы для поиска и агрегации данных по комментариям.
 * </p>
 *
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see com.example.taskmanagement.model.Comment
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * <p><b>Поиск Комментариев по ID Задачи</b></p>
     *
     * @param taskId Идентификатор задачи, к которой относятся комментарии.
     * @return Список {@link Comment}. Будет пустым, если комментарии не найдены, но никогда не {@code null}.
     */
    List<Comment> findByTaskId(@NotNull Long taskId);

    /**
     * <p><b>Подсчет Комментариев для Задачи</b></p>
     *
     * <blockquote>
     *     <b>Оптимизация:</b> Этот метод выполняет прямой SQL-запрос {@code COUNT(*)},
     *     что гораздо производительнее, чем извлечение всех комментариев
     *     и последующий вызов {@code .size()} на коллекции.
     * </blockquote>
     *
     * @param task Сущность задачи, для которой нужно посчитать комментарии.
     * @return Общее количество комментариев (тип {@code long}).
     */
    long countByTask(@NotNull Task task);

    /**
     * <p><b>Подсчет Комментариев, Оставленных Пользователем</b></p>
     *
     * @param appUser Сущность пользователя, чьи комментарии нужно посчитать.
     * @return Общее количество комментариев (тип {@code long}).
     */
    long countByAppUser(@NotNull AppUser appUser);

    /**
     * <p><b>Поиск Комментариев по Ключевому Слову</b></p>
     *
     * @param keyword Подстрока для поиска в тексте комментария (без учета регистра).
     * @return Список {@link Comment}, текст которых содержит указанное слово.
     */
    List<Comment> findByTextContainingIgnoreCase(@NotNull String keyword);

    /**
     * <p><b>Поиск Комментариев по Id Пользователя (JPQL)</b></p>
     * <p>
     *     Находит все комментарии, оставленные пользователем с указанным id,
     *     используя кастомный JPQL-запрос.
     * </p>
     *
     * @param taskId ID пользователя для поиска. Аннотация {@link Param @Param} связывает
     *              этот параметр с именованным параметром {@code :email} в запросе.
     * @return Список {@link Comment}, оставленных указанным пользователем.
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.appUser WHERE c.task.id = :taskId ORDER BY c.createdAt ASC")
    List<Comment> findByTaskIdWithAuthor(@Param("taskId") Long taskId);
}