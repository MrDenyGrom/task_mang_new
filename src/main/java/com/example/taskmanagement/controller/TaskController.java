package com.example.taskmanagement.controller;

import com.example.taskmanagement.config.UserDetail;
import com.example.taskmanagement.dto.CreateTaskDTO;
import com.example.taskmanagement.dto.TaskDTO;
import com.example.taskmanagement.dto.UpdateTaskDTO;
import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.model.Status;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * <p><b>Контроллер для Управления Задачами 🎯</b></p>
 * <p>Предоставляет REST API эндпоинты для всех операций, связанных с жизненным циклом задач.</p>
 * <p>
 *     Этот контроллер обеспечивает:
 *     <ul>
 *         <li>Создание, редактирование и удаление задач.</li>
 *         <li>Назначение задач исполнителям.</li>
 *         <li>Изменение статуса задач.</li>
 *         <li>Получение задач по различным критериям (ID, статус, диапазон дат, по пользователю, по фильтру).</li>
 *         <li>Административные операции по управлению задачами.</li>
 *     </ul>
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tasks")
@Slf4j
@Tag(name = "2. 🎯 Задачи", description = """
    ### API для управления задачами и их статусами.
    *   **Создание** и **управление** собственными задачами ➕✏️🗑️.
    *   **Назначение** задач другим пользователям ➡️.
    *   **Изменение статуса** задач 🔄.
    *   **Просмотр** задач по различным фильтрам 🔎.
    *   **Администрирование** любых задач (только для роли `ADMIN`) 👑.
    """)
public class TaskController {

    private final TaskService taskService;

    private static final String ERROR_400_INVALID_DATA_EXAMPLE = """
            {
                "timestamp": "%s",
                "status": 400,
                "error": "Bad Request",
                "message": "Ошибка валидации входных данных. Проверьте поля запроса.",
                "details": [
                    {
                        "field": "title",
                        "message": "Заголовок задачи не может быть пустым"
                    }
                ],
                "path": "/api/tasks/create"
            }
            """;

    private static final String ERROR_400_INVALID_DATE_RANGE_EXAMPLE = """
            {
                "timestamp": "%s",
                "status": 400,
                "error": "Bad Request",
                "message": "TASK-003: Начальная дата не может быть позже конечной даты.",
                "path": "/api/tasks/between-dates"
            }
            """;

    private static final String ERROR_401_UNAUTHORIZED_EXAMPLE = """
            {
                "timestamp": "%s",
                "status": 401,
                "error": "Unauthorized",
                "message": "AUTH-001: Для выполнения этого действия необходимо авторизоваться.",
                "path": "/api/tasks/create"
            }
            """;

    private static final String ERROR_403_ACCESS_DENIED_EXAMPLE = """
            {
                "timestamp": "%s",
                "status": 403,
                "error": "Forbidden",
                "message": "Доступ запрещен: у вас недостаточно прав для выполнения этой операции.",
                "path": "/api/tasks/edit/1"
            }
            """;

    private static final String ERROR_403_TASK_SPECIFIC_ACCESS_DENIED_EXAMPLE = """
            {
                "timestamp": "%s",
                "status": 403,
                "error": "Forbidden",
                "message": "TASK-002: Вы не имеете прав на редактирование этой задачи.",
                "path": "/api/tasks/edit/1"
            }
            """;

    private static final String ERROR_404_TASK_NOT_FOUND_EXAMPLE = """
            {
                "timestamp": "%s",
                "status": 404,
                "error": "Not Found",
                "message": "TASK-001: Задача с ID 999 не найдена.",
                "path": "/api/tasks/getById/999"
            }
            """;

    private static final String ERROR_404_USER_NOT_FOUND_EXAMPLE = """
            {
                "timestamp": "%s",
                "status": 404,
                "error": "Not Found",
                "message": "USR-002: Пользователь с email 'nonexistent@example.com' не найден.",
                "path": "/api/tasks/create"
            }
            """;

    private static final String ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE = """
            {
                "timestamp": "%s",
                "status": 500,
                "error": "Internal Server Error",
                "message": "Произошла непредвиденная ошибка сервера. Пожалуйста, попробуйте позже или обратитесь в службу поддержки.",
                "path": "/api/tasks/getAll"
            }
            """;


    @Operation(
            summary = "➕ Создать новую задачу",
            description = """
            Создает новую задачу в системе. Автором задачи автоматически становится текущий аутентифицированный пользователь.
            Если указан исполнитель (`executorUsername`), система попытается найти его по email и назначить задаче.
            """,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "✅ Задача успешно создана. Возвращает данные созданной задачи.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Task.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Ошибка валидации: Неверный формат данных (например, пустой заголовок задачи, некорректная дата).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_400_INVALID_DATA_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован: Отсутствует или неверный JWT токен. (Код ошибки: `AUTH-001`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Ресурс не найден: Указанный исполнитель задачи (по email) не найден. (Код ошибки: `USR-002`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_404_USER_NOT_FOUND_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "🚫 Внутренняя ошибка сервера: Непредвиденная ошибка при обработке запроса.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE))
            )
    })
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskDTO> createTask(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания новой задачи.", required = true,
                    content = @Content(schema = @Schema(implementation = CreateTaskDTO.class))
            )
            @Valid @RequestBody CreateTaskDTO taskDTO
    ) {
        TaskDTO createdTask = taskService.createTask(taskDTO);
        URI location = URI.create(String.format("/api/tasks/%s", createdTask.getId()));
        return ResponseEntity.created(location).body(createdTask);
    }


    @Operation(
            summary = "✏️ Редактировать существующую задачу",
            description = """
            Обновляет только переданные поля существующей задачи.
            **Только автор задачи может ее редактировать.**
            Поля, не указанные в запросе, останутся без изменений.
            Чтобы снять исполнителя, передайте поле `executorUsername` с пустой строкой `""`.
            """,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Задача успешно обновлена. Возвращает обновленные данные задачи.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Ошибка валидации: Неверный формат данных в теле запроса.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_400_INVALID_DATA_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "❌ Доступ запрещен: Текущий пользователь не является автором задачи. (Код ошибки: `TASK-002`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_403_TASK_SPECIFIC_ACCESS_DENIED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Ресурс не найден: Задача с указанным ID не найдена, или указанный исполнитель не найден. (Код ошибки: `TASK-001` или `USR-002`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = "Задача не найдена", value = ERROR_404_TASK_NOT_FOUND_EXAMPLE),
                            @ExampleObject(name = "Исполнитель не найден", value = ERROR_404_USER_NOT_FOUND_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "🚫 Внутренняя ошибка сервера.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE))
            )
    })
    @PatchMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskDTO> patchTask(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для частичного обновления задачи. Передавайте только те поля, которые нужно изменить.", required = true,
                    content = @Content(schema = @Schema(implementation = UpdateTaskDTO.class))
            )
            @Valid @RequestBody UpdateTaskDTO taskDTO,
            @Parameter(description = "Уникальный идентификатор задачи для редактирования.", required = true, example = "1")
            @PathVariable long id
    ) {
        TaskDTO updatedTask = taskService.patchTask(taskDTO, id);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(
            summary = "🗑️ Удалить существующую задачу",
            description = """
            Полностью удаляет задачу из системы по её ID.
            **Только автор задачи может её удалить.** Действие необратимо.
            """,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "✅ Задача успешно удалена. Тело ответа пустое."
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "❌ Доступ запрещен: Текущий пользователь не является автором задачи. (Код ошибки: `TASK-002`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_403_TASK_SPECIFIC_ACCESS_DENIED_EXAMPLE))
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
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Уникальный идентификатор задачи для удаления.", required = true, example = "1")
            @PathVariable long id
    ) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "📋 Получить список всех задач",
            description = """
            Возвращает список всех задач, доступных в системе.
            """
            ,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Список задач успешно получен.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Task.class, type = "array"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "🚫 Внутренняя ошибка сервера.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE))
            )
    })
    @GetMapping("/getAll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        List<TaskDTO> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @Operation(
            summary = "🆔 Получить задачу по ID",
            description = """
            Возвращает полную информацию о задаче по её уникальному идентификатору.
            """
            ,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Задача успешно найдена и возвращена.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Task.class))
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
    @GetMapping("/getById/{taskId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskDTO> getTaskById(
            @Parameter(description = "Уникальный идентификатор задачи.", required = true, example = "1")
            @PathVariable long taskId
    ) {
        TaskDTO task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(task);
    }

    @Operation(
            summary = "📊 Получить задачи по статусу",
            description = """
            Возвращает список задач, находящихся в определенном статусе (например, `WAITING`, `IN_PROGRESS`).
            """
            ,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Список задач по статусу успешно получен.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Task.class, type = "array"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Неверный запрос: Некорректное значение статуса.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_400_INVALID_DATA_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "🚫 Внутренняя ошибка сервера.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE))
            )
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskDTO>> getTasksByStatus(
            @Parameter(description = "Статус задачи для фильтрации.", required = true, example = "WAITING", schema = @Schema(implementation = Status.class))
            @PathVariable Status status
    ) {
        List<TaskDTO> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
            summary = "➡️ Назначить задачу пользователю",
            description = """
            Назначает задачу другому пользователю.
            **Только автор задачи может изменить её исполнителя.**
            """,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Задача успешно назначена. Возвращает обновленную задачу.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Task.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "❌ Доступ запрещен: Текущий пользователь не является автором задачи. (Код ошибки: `TASK-002`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_403_TASK_SPECIFIC_ACCESS_DENIED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Ресурс не найден: Задача или пользователь с указанным ID не найдены. (Код ошибки: `TASK-001` или `USR-002`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = "Задача не найдена", value = ERROR_404_TASK_NOT_FOUND_EXAMPLE),
                            @ExampleObject(name = "Пользователь не найден", value = ERROR_404_USER_NOT_FOUND_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "🚫 Внутренняя ошибка сервера.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE))
            )
    })
    @PostMapping("/{taskId}/assign/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskDTO> assignTask(
            @Parameter(description = "Уникальный идентификатор задачи.", required = true, example = "1")
            @PathVariable Long taskId,
            @Parameter(description = "Уникальный идентификатор пользователя, которому будет назначена задача.", required = true, example = "2")
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetail userDetail
    ) {
        if (userDetail == null) {
            throw new org.springframework.security.access.AccessDeniedException("Не удалось определить текущего пользователя.");
        }

        AppUser currentUser = userDetail.appUser();

        TaskDTO taskDto = taskService.getTaskById(taskId);

        if (!taskService.isTaskAuthor(taskDto, currentUser.getEmail())) {
            log.warn("🚫 Пользователь '{}' (ID: {}) пытался назначить задачу ID: {}, но не является ее автором.",
                    currentUser.getEmail(), currentUser.getId(), taskId);
            throw new org.springframework.security.access.AccessDeniedException(
                    "TASK-002: Только автор задачи может ее назначать.");
        }

        TaskDTO assignedTask = taskService.assignTask(taskId, userId);
        return ResponseEntity.ok(assignedTask);
    }


    @Operation(
            summary = "🔄 Установить статус задачи",
            description = """
            Изменяет статус задачи.
            **Только автор задачи или текущий исполнитель могут изменить её статус.**
            """
            ,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Статус задачи успешно изменен. Возвращает обновленную задачу.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Неверный запрос: Некорректное значение статуса или нелогичный переход статусов.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_400_INVALID_DATA_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "❌ Доступ запрещен: Текущий пользователь не является ни автором, ни исполнителем задачи. (Код ошибки: `TASK-002`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_403_TASK_SPECIFIC_ACCESS_DENIED_EXAMPLE))
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
    @PutMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskDTO> setStatus(
            @Parameter(description = "Уникальный идентификатор задачи.", required = true, example = "1")
            @PathVariable long id,
            @Parameter(description = "Новый статус для задачи.", required = true, example = "IN_PROGRESS", schema = @Schema(implementation = Status.class))
            @RequestParam Status status
    ) {
        TaskDTO updatedTask = taskService.setStatus(id, status);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(
            summary = "🗓️ Получить задачи между датами",
            description = """
            Возвращает список задач, у которых срок выполнения (`dueDate`) находится в указанном диапазоне дат (включительно).
            """
            ,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Список задач по датам успешно получен.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskDTO.class, type = "array"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Неверный запрос: Некорректный формат даты или начальная дата позже конечной. (Код ошибки: `TASK-003`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_400_INVALID_DATE_RANGE_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "🚫 Внутренняя ошибка сервера.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE))
            )
    })
    @GetMapping("/between-dates")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskDTO>> getTasksBetweenDates(
            @Parameter(description = "Начальная дата срока выполнения (YYYY-MM-DD).", required = true, example = "2025-06-30")
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "Конечная дата срока выполнения (YYYY-MM-DD).", required = true, example = "2025-07-25")
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<TaskDTO> tasks = taskService.getTasksBetweenDates(startDate, endDate);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
            summary = "🧑‍💻 Получить все задачи пользователя (автор или исполнитель)",
            description = """
            Возвращает список задач, где указанный пользователь является либо автором, либо исполнителем.
            """
            ,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Список задач для пользователя успешно получен.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskDTO.class, type = "array"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Ресурс не найден: Пользователь с указанным email не найден. (Код ошибки: `USR-002`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_404_USER_NOT_FOUND_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "🚫 Внутренняя ошибка сервера.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE))
            )
    })
    @GetMapping("/by-user/{email}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskDTO>> getAllTasksByUser(
            @Parameter(description = "Email пользователя, чьи задачи необходимо получить.", required = true, example = "user@example.com")
            @PathVariable String email
    ) {
        List<TaskDTO> tasks = taskService.getAllTasksByUser(email);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
            summary = "🔢 Получить все ID задач",
            description = """
            Возвращает список всех уникальных идентификаторов задач, присутствующих в системе.
            """
            ,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Список ID задач успешно получен.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "array"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "🚫 Внутренняя ошибка сервера.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE))
            )
    })
    @GetMapping("/all-ids")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Long>> getAllTaskIds() {
        List<Long> taskIds = taskService.getAllTaskIds();
        return ResponseEntity.ok(taskIds);
    }

    @Operation(
            summary = "🔎 Получить задачи по фильтру (по заголовку)",
            description = """
            Возвращает список задач, заголовок которых содержит указанную подстроку (без учета регистра).
            Этот эндпоинт демонстрирует использование динамической фильтрации.
            """
            ,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Список задач по фильтру успешно получен.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskDTO.class, type = "array"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "🚫 Внутренняя ошибка сервера.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE))
            )
    })
    @GetMapping("/by-filter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskDTO>> getTasksByFilter(
            @Parameter(description = "Подстрока для поиска в заголовке задачи (без учета регистра).", example = "отчет")
            @RequestParam(required = false) String title
    ) {
        Specification<Task> spec = (root, query, criteriaBuilder) -> {
            assert query != null;
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("author", JoinType.LEFT);
                root.fetch("executor", JoinType.LEFT);
                root.fetch("comments", JoinType.LEFT);
            }

            Predicate predicate = criteriaBuilder.conjunction();
            if (title != null && !title.isEmpty()) {
                String lowerTitle = title.toLowerCase();
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + lowerTitle + "%"));
            }

            return predicate;
        };

        List<TaskDTO> tasks = taskService.getTasksByFilter(spec);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
            summary = "🧑‍💻📄 Получить задачи, порученные текущему пользователю",
            description = """
            Возвращает список задач, где текущий аутентифицированный пользователь является исполнителем.
            """
            ,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Список порученных задач успешно получен.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskDTO.class, type = "array"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Ресурс не найден: Аутентифицированный пользователь не найден в системе. (Код ошибки: `USR-002`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_404_USER_NOT_FOUND_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "🚫 Внутренняя ошибка сервера.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE))
            )
    })
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskDTO>> getMyTask(@AuthenticationPrincipal UserDetail userDetail) {
        AppUser currentUser = userDetail.appUser();
        log.info("📢 Получение задач, порученных пользователю '{}' (ID: {}).", currentUser.getEmail(), currentUser.getId());
        List<TaskDTO> tasks = taskService.getMyTasks(currentUser);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
            summary = "👑 Удалить любую задачу (только для ADMIN)",
            description = """
            Административное действие. Позволяет пользователю с ролью `ADMIN` безвозвратно удалить любую задачу по её ID,
            независимо от её автора.
            """,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "✅ Задача успешно удалена. Тело ответа пустое."
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "❌ Доступ запрещен: У текущего пользователя нет роли `ADMIN`.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_403_ACCESS_DENIED_EXAMPLE))
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
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAnyTask(
            @Parameter(description = "Уникальный идентификатор задачи для удаления.", required = true, example = "1")
            @PathVariable Long id
    ) {
        log.info("📢 Вызов DELETE /api/tasks/admin/{}: Удаление задачи (админ).", id);
        taskService.deleteAnyTask(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "👑 Обновить любую задачу (только для ADMIN)",
            description = """
            Административное действие. Позволяет пользователю с ролью `ADMIN` обновить данные любой задачи по её ID,
            независимо от её автора.
            """,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Задача успешно обновлена. Возвращает обновленные данные задачи.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Task.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Ошибка валидации: Неверный формат данных в теле запроса.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_400_INVALID_DATA_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Не аутентифицирован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "❌ Доступ запрещен: У текущего пользователя нет роли `ADMIN`.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_403_ACCESS_DENIED_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Ресурс не найден: Задача с указанным ID не найдена, или указанный исполнитель не найден. (Код ошибки: `TASK-001` или `USR-002`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = "Задача не найдена", value = ERROR_404_TASK_NOT_FOUND_EXAMPLE),
                            @ExampleObject(name = "Исполнитель не найден", value = ERROR_404_USER_NOT_FOUND_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "🚫 Внутренняя ошибка сервера.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_500_INTERNAL_SERVER_ERROR_EXAMPLE))
            )
    })
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Task> updateAnyTask(
            @Parameter(description = "Уникальный идентификатор задачи для обновления.", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для обновления задачи.", required = true,
                    content = @Content(schema = @Schema(implementation = UpdateTaskDTO.class))
            )
            @RequestBody @Valid UpdateTaskDTO updateTaskDTO
    ) {
        log.info("📢 Вызов PUT /api/tasks/admin/{}: Обновление задачи (админ).", id);
        Task updatedTask = taskService.updateAnyTask(id, updateTaskDTO);
        return ResponseEntity.ok(updatedTask);
    }
}