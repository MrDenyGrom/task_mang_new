package com.example.taskmanagement.controller;

import com.example.taskmanagement.config.UserDetail;
import com.example.taskmanagement.dto.CommentDTO;
import com.example.taskmanagement.dto.CreateCommentDTO;
import com.example.taskmanagement.dto.UpdateCommentDTO;
import com.example.taskmanagement.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p><b>Контроллер для Управления Комментариями 💬</b></p>
 * <p>Предоставляет REST API эндпоинты для всех операций, связанных с комментариями к задачам.</p>
 * <p>
 *     Этот контроллер обеспечивает:
 *     <ul>
 *         <li>Создание новых комментариев к задачам.</li>
 *         <li>Обновление собственных комментариев.</li>
 *         <li>Удаление собственных комментариев или комментариев (для администраторов).</li>
 *         <li>Получение списка всех комментариев для конкретной задачи.</li>
 *     </ul>
 * </p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "3. 💬  Комментарии", description = """
    ### API для создания и управления комментариями к задачам.
    *   **Создание** новых комментариев к любой задаче ➕.
    *   **Редактирование** только своих комментариев ✏️.
    *   **Удаление** своих комментариев или любых комментариев (для `ADMIN`) 🗑️.
    *   **Получение** всех комментариев к конкретной задаче 📋.
    """)
public class CommentController {

    private final CommentService commentService;

    private static final String ERROR_400_INVALID_DATA_EXAMPLE = """
            {
                "timestamp": "%s",
                "status": 400,
                "error": "Bad Request",
                "message": "Ошибка валидации входных данных. Проверьте поля запроса.",
                "details": [
                    {
                        "field": "text",
                        "message": "Текст комментария не может быть пустым."
                    }
                ],
                "path": "/api/tasks/1/comments"
            }
            """;

    private static final String ERROR_401_UNAUTHORIZED_EXAMPLE = """
            {
                "timestamp": "%s",
                "status": 401,
                "error": "Unauthorized",
                "message": "AUTH-001: Для выполнения этого действия необходимо авторизоваться.",
                "path": "/api/tasks/1/comments"
            }
            """;

    private static final String ERROR_403_COMMENT_ACCESS_DENIED_EXAMPLE = """
            {
                "timestamp": "%s",
                "status": 403,
                "error": "Forbidden",
                "message": "CMT-002: Вы не можете редактировать чужие комментарии.",
                "path": "/api/comments/101"
            }
            """;

    private static final String ERROR_403_ADMIN_ACCESS_DENIED_EXAMPLE = """
            {
                "timestamp": "%s",
                "status": 403,
                "error": "Forbidden",
                "message": "Доступ запрещен: у вас недостаточно прав для выполнения этой операции.",
                "path": "/api/comments/101"
            }
            """;


    private static final String ERROR_404_TASK_NOT_FOUND_EXAMPLE = """
            {
                "timestamp": "%s",
                "status": 404,
                "error": "Not Found",
                "message": "TASK-001: Задача с ID 999 не найдена.",
                "path": "/api/tasks/999/comments"
            }
            """;

    private static final String ERROR_404_COMMENT_NOT_FOUND_EXAMPLE = """
            {
                "timestamp": "%s",
                "status": 404,
                "error": "Not Found",
                "message": "CMT-001: Комментарий с ID 999 не найден.",
                "path": "/api/comments/999"
            }
            """;

    private static final String ERROR_404_USER_NOT_FOUND_EXAMPLE = """
            {
                "timestamp": "%s",
                "status": 404,
                "error": "Not Found",
                "message": "USR-002: Аутентифицированный пользователь не найден.",
                "path": "/api/tasks/1/comments"
            }
            """;


    private static final String ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE = """
            {
                "timestamp": "%s",
                "status": 500,
                "error": "Internal Server Error",
                "message": "Произошла непредвиденная ошибка сервера. Пожалуйста, попробуйте позже или обратитесь в службу поддержки.",
                "path": "/api/tasks/1/comments"
            }
            """;


    @Operation(
            summary = "➕ Создать комментарий к задаче",
            description = """
            Создает новый комментарий к указанной задаче. Автором комментария становится текущий аутентифицированный пользователь.
            """,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "✅ Комментарий успешно создан. Возвращает данные созданного комментария.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommentDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Ошибка валидации: Неверный формат данных (например, пустой текст комментария).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_400_INVALID_DATA_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован: Отсутствует или неверный JWT токен. (Код ошибки: `AUTH-001`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Ресурс не найден: Задача с указанным ID не найдена, или аутентифицированный пользователь не найден. (Код ошибки: `TASK-001` или `USR-002`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = "Задача не найдена", value = ERROR_404_TASK_NOT_FOUND_EXAMPLE),
                            @ExampleObject(name = "Пользователь не найден", value = ERROR_404_USER_NOT_FOUND_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "🚫 Внутренняя ошибка сервера: Непредвиденная ошибка при обработке запроса.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE))
            )
    })
    @PostMapping("/tasks/{taskId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO> createComment(
            @Parameter(description = "Уникальный идентификатор задачи, к которой добавляется комментарий.", required = true, example = "1")
            @PathVariable long taskId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания комментария (текст).", required = true,
                    content = @Content(schema = @Schema(implementation = CreateCommentDTO.class))
            )
            @Valid @RequestBody CreateCommentDTO createCommentDTO,
            @AuthenticationPrincipal UserDetail userDetail
    ) {
        log.info("📢 Запрос на создание комментария к задаче ID: {} от пользователя '{}'.", taskId, userDetail.getUsername());
        CommentDTO createdComment = commentService.createComment(taskId, createCommentDTO.getText(), userDetail.getUsername());
        URI location = URI.create(String.format("/api/comments/%s", createdComment.getId())); // Пример URI для созданного комментария
        return ResponseEntity.created(location).body(createdComment);
    }

    @Operation(
            summary = "✏️ Обновить свой комментарий",
            description = """
            Обновляет текст существующего комментария.
            **Только автор комментария может его редактировать.**
            """,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Комментарий успешно обновлен. Возвращает обновленные данные комментария.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommentDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Ошибка валидации: Неверный формат данных (например, пустой новый текст комментария).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_400_INVALID_DATA_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "❌ Доступ запрещен: Текущий пользователь не является автором комментария. (Код ошибки: `CMT-002`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_403_COMMENT_ACCESS_DENIED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Ресурс не найден: Комментарий с указанным ID не найден. (Код ошибки: `CMT-001`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_404_COMMENT_NOT_FOUND_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "🚫 Внутренняя ошибка сервера.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE))
            )
    })
    @PutMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO> updateComment(
            @Parameter(description = "Уникальный идентификатор комментария для обновления.", required = true, example = "101")
            @PathVariable long commentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для обновления комментария (новый текст).", required = true,
                    content = @Content(schema = @Schema(implementation = UpdateCommentDTO.class))
            )
            @Valid @RequestBody UpdateCommentDTO updateCommentDTO,
            @AuthenticationPrincipal UserDetail userDetail
    ) {
        log.info("📢 Запрос на обновление комментария ID: {} от пользователя '{}'.", commentId, userDetail.getUsername());
        CommentDTO updatedComment = commentService.updateComment(commentId, updateCommentDTO.getText(), userDetail.getUsername());
        return ResponseEntity.ok(updatedComment);
    }

    @Operation(
            summary = "🗑️ Удалить комментарий",
            description = """
            Удаляет существующий комментарий по его ID.
            **Только автор комментария или пользователь с ролью `ADMIN` может удалить комментарий.**
            Действие необратимо.
            """,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "✅ Комментарий успешно удален. Тело ответа пустое."
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "❌ Доступ запрещен: Текущий пользователь не является автором комментария и не администратор. (Код ошибки: `CMT-002` или общий 403 от Spring Security).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = "Доступ к комментарию запрещен", value = ERROR_403_COMMENT_ACCESS_DENIED_EXAMPLE),
                            @ExampleObject(name = "Админ доступ запрещен", value = ERROR_403_ADMIN_ACCESS_DENIED_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Ресурс не найден: Комментарий с указанным ID не найден. (Код ошибки: `CMT-001`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_404_COMMENT_NOT_FOUND_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "🚫 Внутренняя ошибка сервера.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE))
            )
    })
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()") // Разрешаем всем аутентифицированным, а сервис внутри проверит права
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "Уникальный идентификатор комментария для удаления.", required = true, example = "101")
            @PathVariable long commentId,
            @AuthenticationPrincipal UserDetail userDetail
    ) {
        log.info("📢 Запрос на удаление комментария ID: {} от пользователя '{}'.", commentId, userDetail.getUsername());
        List<String> roles = userDetail.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority()) // Получаем строковое представление роли (напр., "ROLE_ADMIN")
                .collect(Collectors.toList());
        commentService.deleteComment(commentId, userDetail.getUsername(), roles);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "📋 Получить все комментарии к задаче",
            description = """
            Возвращает список всех комментариев, привязанных к конкретной задаче, отсортированных по дате создания.
            """,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Список комментариев успешно получен.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommentDTO.class, type = "array"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Ресурс не найден: Задача с указанным ID не найдена. (Код ошибки: `TASK-001`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_404_TASK_NOT_FOUND_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "🚫 Внутренняя ошибка сервера.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE))
            )
    })
    @GetMapping("/tasks/{taskId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommentDTO>> getCommentsForTask(
            @Parameter(description = "Уникальный идентификатор задачи, для которой необходимо получить комментарии.", required = true, example = "1")
            @PathVariable long taskId
    ) {
        log.info("📢 Запрос на получение комментариев для задачи ID: {}.", taskId);
        List<CommentDTO> comments = commentService.getCommentsByTaskId(taskId);
        return ResponseEntity.ok(comments);
    }
}