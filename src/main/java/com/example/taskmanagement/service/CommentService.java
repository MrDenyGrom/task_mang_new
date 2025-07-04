package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.CommentDTO;
import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.model.Comment;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.repository.CommentRepository;
import com.example.taskmanagement.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p><b>Сервис для Управления Комментариями 💬</b></p>
 *
 * <p>
 *     Содержит бизнес-логику для всех операций, связанных с комментариями:
 *     создание, чтение, обновление и удаление.
 * </p>
 *
 * <p><b>Ключевые принципы:</b></p>
 * <ul>
 *     <li><b>Инкапсуляция:</b> Вся логика, включая поиск сущностей и проверку прав,
 *     сосредоточена внутри сервиса.</li>
 *     <li><b>Безопасность:</b> Сервис отвечает за проверку того, что пользователь
 *     имеет право выполнять операции с комментарием (например, редактировать или удалять только свои).</li>
 *     <li><b>DTO-ориентированность:</b> Все публичные методы возвращают DTO, а не сущности,
 *     чтобы избежать проблем с сериализацией и скрыть внутреннюю структуру данных.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    private static final String TASK_NOT_FOUND_CODE = "TASK-001";
    private static final String COMMENT_NOT_FOUND_CODE = "CMT-001";
    private static final String COMMENT_ACCESS_DENIED_CODE = "CMT-002";

    /**
     * <p><b>Создает новый комментарий к задаче ➕</b></p>
     *
     * @param taskId ID задачи, к которой добавляется комментарий.
     * @param commentText Текст комментария.
     * @param authorEmail Email автора комментария (текущего аутентифицированного пользователя).
     * @return DTO созданного комментария.
     * @throws ResponseStatusException
     *         <ul>
     *             <li><b>404 NOT_FOUND</b> (`TASK-001`): Если задача не найдена.</li>
     *             <li><b>404 NOT_FOUND</b> (`USR-002`): Если автор комментария не найден.</li>
     *         </ul>
     */
    @Transactional
    public CommentDTO createComment(long taskId, String commentText, String authorEmail) {
        log.info("📢 Попытка создания комментария к задаче ID: {} пользователем '{}'", taskId, authorEmail);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("❌ Задача с ID '{}' не найдена для создания комментария.", taskId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, TASK_NOT_FOUND_CODE + ": Задача с ID " + taskId + " не найдена.");
                });

        AppUser author = userService.getUserByEmail(authorEmail);

        Comment comment = new Comment();
        comment.setText(commentText);
        comment.setTask(task);
        comment.setAppUser(author);

        Comment savedComment = commentRepository.save(comment);
        log.info("✅ Комментарий с ID: {} успешно создан для задачи ID: {} пользователем '{}'.", savedComment.getId(), taskId, authorEmail);

        return convertToDTO(savedComment);
    }

    /**
     * <p><b>Обновляет текст существующего комментария ✏️</b></p>
     * <p>Только автор комментария может его редактировать.</p>
     *
     * @param commentId ID комментария для обновления.
     * @param newText Новый текст комментария.
     * @param editorEmail Email пользователя, пытающегося выполнить обновление.
     * @return DTO обновленного комментария.
     * @throws ResponseStatusException
     *         <ul>
     *             <li><b>404 NOT_FOUND</b> (`CMT-001`): Если комментарий не найден.</li>
     *             <li><b>403 FORBIDDEN</b> (`CMT-002`): Если пользователь не является автором комментария.</li>
     *         </ul>
     */
    @Transactional
    public CommentDTO updateComment(long commentId, String newText, String editorEmail) {
        log.info("📢 Попытка обновления комментария ID: {} пользователем '{}'.", commentId, editorEmail);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("❌ Комментарий с ID '{}' не найден для обновления.", commentId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, COMMENT_NOT_FOUND_CODE + ": Комментарий с ID " + commentId + " не найден.");
                });

        if (!comment.getAppUser().getEmail().equals(editorEmail)) {
            log.warn("🚫 Пользователь '{}' пытался редактировать чужой комментарий (ID: {}).", editorEmail, commentId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, COMMENT_ACCESS_DENIED_CODE + ": Вы не можете редактировать чужие комментарии.");
        }

        comment.setText(newText);
        Comment updatedComment = commentRepository.save(comment);
        log.info("✅ Комментарий ID: {} успешно обновлен.", updatedComment.getId());

        return convertToDTO(updatedComment);
    }

    /**
     * <p><b>Удаляет комментарий 🗑️</b></p>
     * <p>Только автор комментария или администратор могут его удалить.</p>
     *
     * @param commentId ID комментария для удаления.
     * @param userEmail Email пользователя, выполняющего удаление.
     * @param userRoles Роли пользователя для проверки прав администратора (например, "ROLE_ADMIN").
     * @throws ResponseStatusException
     *         <ul>
     *             <li><b>404 NOT_FOUND</b> (`CMT-001`): Если комментарий не найден.</li>
     *             <li><b>403 FORBIDDEN</b> (`CMT-002`): Если пользователь не является автором комментария и не администратор.</li>
     *         </ul>
     */
    @Transactional
    public void deleteComment(long commentId, String userEmail, List<String> userRoles) {
        log.info("📢 Попытка удаления комментария ID: {} пользователем '{}'.", commentId, userEmail);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.debug("❌ Комментарий с ID '{}' не найден для удаления.", commentId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, COMMENT_NOT_FOUND_CODE + ": Комментарий с ID " + commentId + " не найден.");
                });

        boolean isAuthor = comment.getAppUser().getEmail().equals(userEmail);
        boolean isAdmin = userRoles.contains("ROLE_ADMIN");

        if (!isAuthor && !isAdmin) {
            log.debug("🚫 Пользователь '{}' пытался удалить чужой комментарий (ID: {}) без прав администратора.", userEmail, commentId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, COMMENT_ACCESS_DENIED_CODE + ": Вы не можете удалять чужие комментарии.");
        }

        commentRepository.delete(comment);
        log.info("✅ Комментарий ID: {} успешно удален.", commentId);
    }

    /**
     * <p><b>Возвращает все комментарии для указанной задачи 📋</b></p>
     *
     * @param taskId ID задачи.
     * @return Список DTO комментариев.
     * @throws ResponseStatusException
     *         <ul>
     *             <li><b>404 NOT_FOUND</b> (`TASK-001`): Если задача не найдена.</li>
     *         </ul>
     */
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByTaskId(long taskId) {
        log.debug("📢 Получение всех комментариев для задачи ID: {}.", taskId);
        if (!taskRepository.existsById(taskId)) {
            log.debug("❌ Задача с ID '{}' не найдена при попытке получить комментарии.", taskId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TASK_NOT_FOUND_CODE + ": Задача с ID " + taskId + " не найдена.");
        }
        List<Comment> comments = commentRepository.findByTaskIdWithAuthor(taskId);
        log.info("✅ Найдено {} комментариев для задачи ID: {}.", comments.size(), taskId);
        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * <p><b>Вспомогательный метод: Конвертирует сущность комментария в DTO ↔️</b></p>
     * <p>Использует {@link ModelMapper} для преобразования {@link Comment} в {@link CommentDTO},
     * а также маппит связанные сущности `AppUser` и `Task` в их DTO-представления.</p>
     *
     * @param comment Сущность {@link Comment}.
     * @return {@link CommentDTO} представление комментария.
     */
    private CommentDTO convertToDTO(Comment comment) {
        return modelMapper.map(comment, CommentDTO.class);
    }
}