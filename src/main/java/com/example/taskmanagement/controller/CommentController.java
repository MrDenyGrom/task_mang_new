package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.CommentDTO;
import com.example.taskmanagement.exception.CommentNotFoundException;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.exception.TaskNotFoundException;
import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.model.Comment;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.service.CommentService;
import com.example.taskmanagement.repository.TaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления комментариями.
 * Предоставляет REST API endpoints для создания, получения, обновления и удаления комментариев.
 */
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CommentController(CommentService commentService, TaskRepository taskRepository, UserRepository userRepository) {
        this.commentService = commentService;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    /**
     * Создание комментария к задаче.
     *
     * @param taskId      ID задачи, к которой добавляется комментарий.
     * @param commentDTO  DTO, содержащий данные комментария.
     * @param authentication  Объект аутентификации Spring Security.
     * @return ResponseEntity с созданным комментарием и статусом CREATED (201) в случае успеха,
     *         или с соответствующим статусом ошибки:
     *         NOT_FOUND (404), если задача не найдена,
     *         UNAUTHORIZED (401) при ошибке аутентификации,
     *         FORBIDDEN (403), если доступ запрещен,
     *         BAD_REQUEST (400) при некорректных данных,
     *         INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @PostMapping("/{taskId}/create")
    @Operation(summary = "Создать комментарий к задаче")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createComment(@PathVariable long taskId, @Valid @RequestBody CommentDTO commentDTO, Authentication authentication) {
        try {
            Comment createdComment = commentService.createComment(taskId, commentDTO, authentication);
            return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
        } catch (TaskNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Получение всех комментариев к задаче.
     *
     * @param taskId ID задачи.
     * @return ResponseEntity со списком комментариев и статусом OK (200) в случае успеха,
     *         или с сообщением об ошибке и статусом NOT_FOUND (404), если задача не найдена,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @GetMapping("/{taskId}/getAllComments")
    @Operation(summary = "Получить все комментарии к задаче")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllCommentsForTask(@PathVariable long taskId) {
        try {
            List<Comment> comments = commentService.getCommentsByTaskId(taskId);
            return new ResponseEntity<>(comments, HttpStatus.OK);
        } catch (TaskNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Удаление комментария по его ID.
     *
     * @param commentId ID комментария для удаления.
     * @return ResponseEntity со статусом OK (200) в случае успеха,
     *         или с соответствующим статусом ошибки:
     *         NOT_FOUND (404), если комментарий не найден,
     *         UNAUTHORIZED (401) при ошибке аутентификации,
     *         FORBIDDEN (403), если доступ запрещен,
     *         INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @DeleteMapping("/delete/{commentId}")
    @Operation(summary = "Удалить комментарий")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteComment(@PathVariable long commentId) {
        try {
            commentService.deleteComment(commentId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CommentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Обновление комментария.
     *
     * @param commentId ID комментария для обновления.
     * @param commentDTO DTO, содержащий обновленные данные комментария.
     * @param authentication Объект аутентификации Spring Security.
     * @return ResponseEntity с обновленным комментарием и статусом OK (200) в случае успеха,
     *         или с соответствующим статусом ошибки:
     *         NOT_FOUND (404), если комментарий не найден,
     *         UNAUTHORIZED (401) при ошибке аутентификации,
     *         FORBIDDEN (403), если доступ запрещен,
     *         BAD_REQUEST (400) при некорректных данных,
     *         INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @PutMapping("/update{commentId}")
    @Operation(summary = "Обновить комментарий")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateComment(@PathVariable long commentId, @Valid @RequestBody CommentDTO commentDTO, Authentication authentication) {
        try {
            Comment comment = commentService.getCommentById(commentId);

            if (!commentService.isCommentAuthor(comment, authentication.getName())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            Comment updatedComment = commentService.updateComment(commentId, commentDTO);
            return new ResponseEntity<>(updatedComment, HttpStatus.OK);
        } catch (CommentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Получение списка комментариев пользователя.
     *
     * @param authentication Объект аутентификации Spring Security.
     * @return ResponseEntity со списком комментариев и статусом OK (200) в случае успеха,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @GetMapping("/my")
    @Operation(summary = "Получить мои комментарии")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyComments(Authentication authentication) {
        try {
            List<Comment> comments = commentService.getCommentsByEmail(authentication.getName());
            return new ResponseEntity<>(comments, HttpStatus.OK);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Поиск комментариев по ключевому слову.
     *
     * @param keyword Ключевое слово для поиска.
     * @return ResponseEntity со списком найденных комментариев и статусом OK (200) в случае успеха,
     *         или со статусом BAD_REQUEST (400) при некорректном запросе,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @GetMapping("/search")
    @Operation(summary = "Поиск комментариев по тексту")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Comment>> searchComments(@RequestParam String keyword) {
        try {
            List<Comment> comments = commentService.searchComments(keyword);
            return new ResponseEntity<>(comments, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }  catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Получение количества комментариев к задаче.
     *
     * @param taskId ID задачи.
     * @return ResponseEntity с количеством комментариев и статусом OK (200) в случае успеха,
     *         или со статусом NOT_FOUND (404), если задача не найдена,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @GetMapping("/{taskId}/count")
    @Operation(summary = "Получить количество комментариев к задаче")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countCommentsByTask(@PathVariable long taskId) {
        try {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new TaskNotFoundException("Задача не найдена с ID: " + taskId));
            long count = commentService.countCommentsByTask(task);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (TaskNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Получение количества комментариев у пользователя.
     *
     * @param authentication Объект аутентификации Spring Security.
     * @return ResponseEntity с количеством комментариев и статусом OK (200) в случае успеха,
     *         или со статусом NOT_FOUND (404), если пользователь не найден,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @GetMapping("/my/count")
    @Operation(summary = "Получить количество моих комментариев")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countMyComments(Authentication authentication) {
        try {
            AppUser user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
            long count = commentService.countCommentsByUser(user);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}