package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.CommentDTO;
import com.example.taskmanagement.exception.CommentNotFoundException;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.exception.TaskNotFoundException;
import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.model.Comment;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.repository.CommentRepository;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для управления комментариями.
 */
@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    /**
     * Создает новый комментарий.
     * @param taskId ID задачи, к которой относится комментарий.
     * @param commentDTO DTO комментария, содержащий текст.
     * @param authentication Аутентификация текущего пользователя.
     * @return Созданный комментарий.
     * @throws TaskNotFoundException Если задача с указанным ID не найдена.
     * @throws ResourceNotFoundException Если пользователь не найден.
     */
    public Comment createComment(long taskId, CommentDTO commentDTO, Authentication authentication) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена с ID: " + taskId));

        AppUser user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Comment comment = new Comment();
        comment.setText(commentDTO.getText());
        comment.setTask(task);
        comment.setAppUser(user);
        log.info("Комментарий '{}' создан для задачи {}", comment.getText(), taskId);
        return commentRepository.save(comment);
    }

    /**
     * Возвращает список комментариев по ID задачи.
     * @param taskId ID задачи.
     * @return Список комментариев.
     * @throws TaskNotFoundException Если задача с указанным ID не найдена.
     */
    public List<Comment> getCommentsByTaskId(long taskId) {
        taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException("Задача с ID " + taskId + " не найдена"));
        List<Comment> comments = commentRepository.findByTaskId(taskId);
        log.debug("Получено {} комментариев для задачи {}", comments.size(), taskId);
        return comments;
    }

    /**
     * Удаляет комментарий по ID.
     * @param commentId ID комментария.
     * @throws CommentNotFoundException Если комментарий с указанным ID не найден.
     */
    public void deleteComment(long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Комментарий с ID " + commentId + " не найден"));
        commentRepository.delete(comment);
        log.info("Комментарий с ID {} удален", commentId);
    }

    /**
     * Обновляет комментарий.
     * @param commentId ID комментария для обновления.
     * @param commentDTO DTO с новыми данными комментария.
     * @return Обновленный комментарий.
     * @throws CommentNotFoundException Если комментарий с указанным ID не найден.
     */
    public Comment updateComment(Long commentId, CommentDTO commentDTO) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Комментарий с ID " + commentId + " не найден"));
        comment.setText(commentDTO.getText());
        comment.setUpdatedAt(LocalDateTime.now());
        Comment updatedComment = commentRepository.save(comment);
        log.info("Комментарий с ID {} обновлен", commentId);
        return updatedComment;
    }

    public Comment getCommentById(Long commentId){
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Комментарий с ID " + commentId + " не найден"));
    }

    public boolean isCommentAuthor(Comment comment, String email) {
        return comment.getAppUser().getEmail().equals(email);
    }

    /**
     * Возвращает список комментариев по email пользователя.
     * @param email Email пользователя.
     * @return Список комментариев.
     */
    public List<Comment> getCommentsByEmail(String email) {
        List<Comment> comments = commentRepository.findByAppUserEmail(email);
        log.debug("Найдено {} комментариев для пользователя {}", comments.size(), email);
        return comments;
    }

    /**
     * Подсчитывает количество комментариев к задаче.
     * @param task Задача.
     * @return Количество комментариев.
     */
    public long countCommentsByTask(Task task) {
        long count = commentRepository.countByTask(task);
        log.debug("Найдено {} комментариев для задачи {}", count, task.getId());
        return count;
    }

    /**
     * Подсчитывает количество комментариев, оставленных пользователем.
     * @param user Пользователь.
     * @return Количество комментариев.
     */
    public long countCommentsByUser(AppUser user) {
        long count = commentRepository.countByAppUser(user);
        log.debug("Найдено {} комментариев для данного пользователя {}", count, user.getEmail());
        return count;
    }

    /**
     * Ищет комментарии, содержащие заданную подстроку.
     * @param keyword Подстрока для поиска.
     * @return Список найденных комментариев.
     */
    public List<Comment> searchComments(String keyword) {
        List<Comment> comments = commentRepository.findByTextContainingIgnoreCase(keyword);
        log.debug("Найдено {} комментариев, содержащих '{}'", comments.size(), keyword);
        return comments;
    }

}