package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.CreateTaskDTO;
import com.example.taskmanagement.dto.TaskDTO;
import com.example.taskmanagement.dto.UpdateTaskDTO;
import com.example.taskmanagement.exception.TaskNotFoundException;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.model.Status;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Контроллер для управления задачами.
 * Предоставляет REST API endpoints для создания, получения, обновления и удаления задач.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;
    private final UserRepository userRepository;

    public TaskController(TaskService taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    /**
     * Создание новой задачи.
     *
     * @param taskDTO DTO для создания задачи.
     * @return ResponseEntity с созданной задачей и статусом CREATED (201) в случае успеха,
     *         или со статусом BAD_REQUEST (400) при некорректных данных,
     *         или со статусом NOT_FOUND (404), если ресурс не найден,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Создать новую задачу")
    public ResponseEntity<?> createTask(@Valid @RequestBody CreateTaskDTO taskDTO) {
        try {
            Task createdTask = taskService.createTask(taskDTO);
            return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>("Ошибка базы данных: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при создании задачи: ", e);
            return new ResponseEntity<>("Произошла непредвиденная ошибка.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Редактирование существующей задачи.
     *
     * @param taskDTO DTO для обновления задачи.
     * @param id ID задачи для редактирования.
     * @return ResponseEntity с обновленной задачей и статусом OK (200) в случае успеха,
     *         или со статусом BAD_REQUEST (400) при некорректных данных,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом FORBIDDEN (403) при отсутствии доступа,
     *         или со статусом NOT_FOUND (404), если задача не найдена,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @PutMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Редактировать существующую задачу")
    public ResponseEntity<Task> editTask(@Valid @RequestBody UpdateTaskDTO taskDTO, @PathVariable long id) {
        try {
            Task updatedTask = taskService.editTask(taskDTO, id);
            return new ResponseEntity<>(updatedTask, HttpStatus.OK);
        } catch (TaskNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Удаление существующей задачи.
     *
     * @param id ID задачи для удаления.
     * @return ResponseEntity со статусом NO_CONTENT (204) в случае успеха,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом FORBIDDEN (403) при отсутствии доступа,
     *         или со статусом NOT_FOUND (404), если задача не найдена,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удалить задачу")
    public ResponseEntity<Void> deleteTask(@PathVariable long id) {
        try {
            taskService.deleteTask(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (TaskNotFoundException e) {
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
     * Получение всех задач.
     *
     * @return ResponseEntity с списком всех задач и статусом OK (200) в случае успеха,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом FORBIDDEN (403) при отсутствии доступа,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @GetMapping("/getAll")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить все задачи")
    public ResponseEntity<Object> getAllTasks() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Получение задачи по ID.
     *
     * @param taskId ID задачи для получения.
     * @return ResponseEntity с задачей и статусом OK (200) в случае успеха,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом FORBIDDEN (403) при отсутствии доступа,
     *         или со статусом NOT_FOUND (404), если задача не найдена,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @GetMapping("/getById/{taskId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить задачу по ID")
    public ResponseEntity<Task> getTaskById(@PathVariable long taskId) {
        try {
            Task task = taskService.getTaskById(taskId);
            return new ResponseEntity<>(task, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (TaskNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Получение задач по статусу.
     *
     * @param status Статус задач для получения.
     * @return ResponseEntity с списком задач и статусом OK (200) в случае успеха,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом FORBIDDEN (403) при отсутствии доступа,
     *         или со статусом BAD_REQUEST (400) при некорректном статусе,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить задачи по статусу")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable Status status) {
        try {
            List<Task> tasks = taskService.getTasksByStatus(status);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
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
     * Назначение задачи пользователю.  Только автор задачи может изменить исполнителя.
     *
     * @param taskId ID задачи.
     * @param userId ID пользователя.
     * @param authentication Объект аутентификации Spring Security.
     * @return ResponseEntity с назначенной задачей и статусом OK (200) в случае успеха,
     *         или со статусом NOT_FOUND (404), если задача или пользователь не найдены,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом FORBIDDEN (403), если пользователь не является автором задачи,
     *         или со статусом BAD_REQUEST (400) при некорректных данных,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @PostMapping("/{taskId}/assign/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Назначить задачу пользователю")
    public ResponseEntity<Task> assignTask(@PathVariable Long taskId, @PathVariable Long userId, Authentication authentication) {
        try {
            Task task = taskService.getTaskById(taskId);

            if (!taskService.isTaskAuthor(task, authentication.getName())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            Task assignedTask = taskService.assignTask(taskId, userId);
            return new ResponseEntity<>(assignedTask, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
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
     * Установка нового статуса задачи.
     *
     * @param id ID задачи.
     * @param status Новый статус задачи.
     * @return ResponseEntity с обновленной задачей и статусом OK (200) в случае успеха,
     *         или со статусом NOT_FOUND (404), если задача не найдена,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом FORBIDDEN (403) при отсутствии доступа,
     *         или со статусом BAD_REQUEST (400) при некорректных данных,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Установить статус задачи")
    public ResponseEntity<Task> setStatus(@PathVariable long id, @RequestParam Status status) {
        try {
            Task updatedTask = taskService.setStatus(id, status);
            return new ResponseEntity<>(updatedTask, HttpStatus.OK);
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
     * Получение задач между датами.
     *
     * @param startDate Начальная дата.
     * @param endDate Конечная дата.
     * @return ResponseEntity со списком задач и статусом OK (200) в случае успеха,
     *         или со статусом BAD_REQUEST (400) при некорректных датах,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом FORBIDDEN (403) при отсутствии доступа,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @GetMapping("/getBetween-dates")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить задачи между датами")
    public ResponseEntity<List<TaskDTO>> getTasksBetweenDates(
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            List<TaskDTO> tasks = taskService.getTasksBetweenDates(startDate, endDate);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Получение задач пользователя.
     *
     * @param email Email пользователя.
     * @return ResponseEntity со списком задач и статусом OK (200) в случае успеха,
     *         или со статусом NOT_FOUND (404), если пользователь не найден,
     *         или со статусом BAD_REQUEST (400) при некорректном email,
     *         или со статусом FORBIDDEN (403) при отсутствии доступа,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @GetMapping("/getBy-user/{email}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить все задачи пользователя")
    public ResponseEntity<List<TaskDTO>> getAllTasksByUser(@PathVariable String email) {
        try {
            List<TaskDTO> tasks = taskService.getAllTasksByUser(email);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Получение ID всех задач.
     *
     * @return ResponseEntity со списком ID задач и статусом OK (200) в случае успеха,
     *         или со статусом BAD_REQUEST (400) при некорректном запросе,
     *         или со статусом FORBIDDEN (403) при отсутствии доступа,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @GetMapping("/getAllIds")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить все ID задач")
    public ResponseEntity<List<Long>> getAllTaskIds() {
        try {
            List<Long> taskIds = taskService.getAllTaskIds();
            return new ResponseEntity<>(taskIds, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Получение задач по фильтру.
     *
     * @param head Заголовок задачи для фильтрации.
     * @return ResponseEntity со списком задач и статусом OK (200) в случае успеха,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при ошибке.
     */
    @GetMapping("/by-filter")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить задачи по фильтру")
    public ResponseEntity<List<Task>> getTasksByFilter(@RequestParam(required = false) String head) {
        try {
            Specification<Task> spec = (root, query, criteriaBuilder) -> {
                Predicate predicate = criteriaBuilder.conjunction();
                if (head != null && !head.isEmpty()) {
                    String lowerHead = head.toLowerCase();
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(criteriaBuilder.lower(root.get("head")), "%" + lowerHead + "%"));
                }
                root.fetch("comments", JoinType.LEFT);

                return predicate;
            };

            List<Task> tasks = taskService.getTasksByFilter(spec);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Получение порученных мне задач.
     *
     * @param authentication Аутентификация пользователя.
     * @return ResponseEntity со списком задач и статусом OK (200) в случае успеха,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @GetMapping("/my")
    @Operation(summary = "Получить порученные мне задачи")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyTask(Authentication authentication) {
        try {
            AppUser user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
            List<Task> tasks = taskService.getMyTasks(user);
            return ResponseEntity.ok(tasks);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Удаление задачи по ID (любой).
     *
     * @param id ID задачи для удаления.
     * @return ResponseEntity со статусом NO_CONTENT (204) в случае успеха,
     *         или со статусом NOT_FOUND (404), если задача не найдена,
     *         или со статусом FORBIDDEN (403) при отсутствии доступа,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Удалить любую задачу (админ)")
    public ResponseEntity<Void> deleteAnyTask(@PathVariable Long id) {
        try {
            logger.info("DELETE /api/tasks/admin/{}: Удаление задачи (admin)", id);
            taskService.deleteAnyTask(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (TaskNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Обновление задачи по ID (любой).
     *
     * @param id ID задачи для обновления.
     * @param updateTaskDTO DTO для обновления задачи.
     * @return ResponseEntity с обновленной задачей и статусом OK (200) в случае успеха,
     *         или со статусом NOT_FOUND (404), если задача не найдена,
     *         или со статусом BAD_REQUEST (400) при некорректных данных,
     *         или со статусом FORBIDDEN (403) при отсутствии доступа,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Обновить любую задачу (админ)")
    public ResponseEntity<Task> updateAnyTask(@PathVariable Long id, @RequestBody UpdateTaskDTO updateTaskDTO) {
        try {
            logger.info("PUT /api/tasks/admin/{}: Обновление задачи (admin)", id);
            Task updatedTask = taskService.updateAnyTask(id, updateTaskDTO);
            return new ResponseEntity<>(updatedTask, HttpStatus.OK);
        } catch (TaskNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}