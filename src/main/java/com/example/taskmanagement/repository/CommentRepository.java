package com.example.taskmanagement.repository;

import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.model.Comment;
import com.example.taskmanagement.model.Task;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с комментариями.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Находит все комментарии, связанные с задачей.
     * @param taskId Идентификатор задачи.
     * @return Список комментариев.
     */
    List<Comment> findByTaskId(@NotNull Long taskId);

    /**
     * Подсчитывает количество комментариев, связанных с задачей.
     * @param task Задача.
     * @return Количество комментариев.
     */
    long countByTask(@NotNull Task task);

    /**
     * Подсчитывает количество комментариев, оставленных пользователем.
     * @param appUser Пользователь.
     * @return Количество комментариев.
     */
    long countByAppUser(@NotNull AppUser appUser);

    /**
     * Находит комментарии, текст которых содержит заданную подстроку (без учета регистра).
     * @param keyword Ключевое слово для поиска.
     * @return Список комментариев.
     */
    List<Comment> findByTextContainingIgnoreCase(@NotNull String keyword);

    /**
     * Находит комментарии, оставленные пользователем с заданным email.
     * @param email Email пользователя.
     * @return Список комментариев.
     */
    @Query("SELECT c FROM Comment c WHERE c.appUser.email = :email")
    List<Comment> findByAppUserEmail(@Param("email") @NotNull String email);
}