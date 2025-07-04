package com.example.taskmanagement.service;

import com.example.taskmanagement.controller.GlobalExceptionHandler;
import com.example.taskmanagement.dto.CreateTaskDTO;
import com.example.taskmanagement.dto.TaskDTO;
import com.example.taskmanagement.dto.UpdateTaskDTO;
import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.model.Status;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p><b>Сервис для Управления Задачами 📝</b></p>
 *
 * <p>
 *     Содержит основную бизнес-логику для всех операций, связанных с задачами:
 *     создание, чтение, обновление, удаление и фильтрация.
 * </p>
 *
 * <p><b>Подход к Обработке Ошибок:</b></p>
 * <blockquote>
 *     Сервис активно использует {@link ResponseStatusException} для обработки
 *     всех ожидаемых ошибок (например, "задача не найдена", "исполнитель не найден", "недостаточно прав").
 *     Это позволяет контроллерам оставаться "чистыми", делегируя формирование
 *     HTTP-ответов об ошибках фреймворку Spring через {@link GlobalExceptionHandler}.
 *     Каждая бизнес-ошибка имеет уникальный код для удобства фронтенд-разработчиков.
 * </blockquote>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    private static final String TASK_NOT_FOUND_CODE = "TASK-001";
    private static final String TASK_ACCESS_DENIED_CODE = "TASK-002";
    private static final String USER_NOT_FOUND_CODE = "USR-002";

    /**
     * <p><b>Создает новую задачу ➕</b></p>
     * <p>
     *     Создает новую задачу, привязывая ее к текущему аутентифицированному пользователю в качестве автора.
     *     Если указан исполнитель, пытается найти его по email.
     * </p>
     *
     * @param createTaskDTO DTO для создания задачи, содержащее заголовок, описание, срок выполнения и (опционально) email исполнителя.
     * @return Созданная и сохраненная сущность {@link Task}.
     * @throws ResponseStatusException
     *         <ul>
     *             <li><b>401 UNAUTHORIZED</b> (`AUTH-001`): Если пользователь не аутентифицирован (этот случай обычно перехватывается Spring Security раньше).</li>
     *             <li><b>404 NOT_FOUND</b> (`USR-002`): Если аутентифицированный пользователь не найден в БД (редкий случай) или указанный исполнитель не найден.</li>
     *         </ul>
     */
    @Transactional
    public TaskDTO createTask(CreateTaskDTO createTaskDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("❌ Ошибка аутентификации при создании задачи. Пользователь не авторизован.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "AUTH-001: Для создания задачи необходимо авторизоваться.");
        }

        String authorEmail = authentication.getName();
        AppUser author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> {
                    log.debug("❌ Аутентифицированный пользователь '{}' не найден в базе данных.", authorEmail);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_CODE + ": Аутентифицированный пользователь не найден.");
                });

        Task task = modelMapper.map(createTaskDTO, Task.class);
        task.setAuthor(author);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        if (createTaskDTO.getExecutorUsername() != null && !createTaskDTO.getExecutorUsername().isEmpty()) {
            AppUser executor = userRepository.findByEmail(createTaskDTO.getExecutorUsername())
                    .orElseThrow(() -> {
                        log.debug("❌ Исполнитель с email '{}' не найден.", createTaskDTO.getExecutorUsername());
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_CODE + ": Исполнитель с email '" + createTaskDTO.getExecutorUsername() + "' не найден.");
                    });
            task.setExecutor(executor);
        } else {
            task.setExecutor(null);
        }

        Task savedTask = taskRepository.save(task);
        log.info("✅ Задача '{}' успешно создана (ID: {}) автором '{}'.", savedTask.getTitle(), savedTask.getId(), authorEmail);

        return convertToDTO(savedTask);
    }

    /**
     * <p><b>Редактирует существующую задачу ✏️</b></p>
     * <p>
     *     Позволяет автору задачи обновить её данные.
     * </p>
     *
     * @param updateTaskDTO DTO с данными для обновления задачи.
     * @param taskId ID задачи для редактирования.
     * @return Обновленная сущность {@link Task}.
     * @throws ResponseStatusException
     *         <ul>
     *             <li><b>404 NOT_FOUND</b> (`TASK-001`): Если задача с указанным ID не найдена.</li>
     *             <li><b>404 NOT_FOUND</b> (`USR-002`): Если указанный исполнитель (если он меняется) не найден.</li>
     *             <li><b>403 FORBIDDEN</b> (`TASK-002`): Если текущий пользователь не является автором задачи.</li>
     *         </ul>
     */
    @Transactional
    public TaskDTO editTask(UpdateTaskDTO updateTaskDTO, long taskId) {
        log.info("📢 Попытка редактирования задачи с ID: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.debug("❌ Задача с ID '{}' не найдена для редактирования.", taskId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(TASK_NOT_FOUND_CODE + ": Задача с ID %d не найдена.", taskId));
                });

        AppUser currentUser = getAuthenticatedUser();

        if (!task.getAuthor().getId().equals(currentUser.getId())) {
            log.error("🚫 Пользователь '{}' (ID: {}) пытался редактировать задачу ID: {}, к которой у него нет прав. Автор задачи: '{}' (ID: {}).",
                    currentUser.getEmail(), currentUser.getId(), taskId, task.getAuthor().getEmail(), task.getAuthor().getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, TASK_ACCESS_DENIED_CODE + ": Вы не имеете прав на редактирование этой задачи.");
        }

        modelMapper.map(updateTaskDTO, task);

        if (updateTaskDTO.getExecutorUsername() != null && !updateTaskDTO.getExecutorUsername().isEmpty()) {
            AppUser newExecutor = userRepository.findByEmail(updateTaskDTO.getExecutorUsername())
                    .orElseThrow(() -> {
                        log.debug("❌ Новый исполнитель с email '{}' не найден при редактировании задачи ID: {}.",
                                updateTaskDTO.getExecutorUsername(), taskId);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_CODE + ": Исполнитель с email '" + updateTaskDTO.getExecutorUsername() + "' не найден.");
                    });
            task.setExecutor(newExecutor);
        } else if (updateTaskDTO.getExecutorUsername() != null && updateTaskDTO.getExecutorUsername().isEmpty()) {
            task.setExecutor(null);
        }

        task.setUpdatedAt(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);
        log.info("✅ Задача с ID: {} успешно обновлена пользователем '{}'.", updatedTask.getId(), currentUser.getEmail());

        return convertToDTO(updatedTask);
    }

    /**
     * <p><b>Удаляет задачу 🗑️</b></p>
     * <p>
     *     Позволяет автору задачи удалить её из системы.
     * </p>
     *
     * @param taskId ID задачи для удаления.
     * @throws ResponseStatusException
     *         <ul>
     *             <li><b>404 NOT_FOUND</b> (`TASK-001`): Если задача с указанным ID не найдена.</li>
     *             <li><b>403 FORBIDDEN</b> (`TASK-002`): Если текущий пользователь не является автором задачи.</li>
     *         </ul>
     */
    @Transactional
    public void deleteTask(long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.debug("❌ Задача с ID '{}' не найдена для удаления.", taskId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(TASK_NOT_FOUND_CODE + ": Задача с ID %d не найдена.", taskId));
                });

        AppUser currentUser = getAuthenticatedUser();

        if (!task.getAuthor().getId().equals(currentUser.getId())) {
            log.debug("🚫 Пользователь '{}' (ID: {}) пытался удалить задачу ID: {}, к которой у него нет прав. Автор задачи: '{}' (ID: {}).",
                    currentUser.getEmail(), currentUser.getId(), taskId, task.getAuthor().getEmail(), task.getAuthor().getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, TASK_ACCESS_DENIED_CODE + ": Вы не имеете прав на удаление этой задачи.");
        }

        taskRepository.deleteById(taskId);
        log.info("✅ Задача с ID: {} успешно удалена пользователем '{}'.", taskId, currentUser.getEmail());
    }

    /**
     * <p><b>Получает список всех задач 📋</b></p>
     * <p>
     *     Возвращает список всех задач в системе.
     *     Этот метод может быть доступен всем аутентифицированным пользователям или только администраторам,
     *     в зависимости от настроек безопасности на уровне контроллера.
     * </p>
     *
     * @return {@link List} всех сущностей {@link Task}.
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getAllTasks() {
        log.debug("📢 Получение всех задач.");
        List<Task> tasks = taskRepository.findAllWithComments();
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * <p><b>Получает задачи по сроку выполнения 🗓️</b></p>
     * <p>
     *     Возвращает список задач, срок выполнения которых попадает в указанный диапазон дат.
     * </p>
     *
     * @param startDate Начальная дата диапазона (включительно).
     * @param endDate Конечная дата диапазона (включительно).
     * @return {@link List} DTO объектов {@link TaskDTO}, представляющих задачи в указанном диапазоне.
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksBetweenDates(LocalDate startDate, LocalDate endDate) {
        log.debug("📢 Поиск задач со сроком выполнения между {} и {}.", startDate, endDate);
        if (startDate.isAfter(endDate)) {
            log.debug("❌ Неверный диапазон дат: начальная дата {} после конечной {}.", startDate, endDate);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TASK-003: Начальная дата не может быть позже конечной даты.");
        }
        List<Task> tasks = taskRepository.findByDueDateBetween(startDate, endDate);
        log.info("✅ Найдено {} задач со сроком выполнения между {} и {}.", tasks.size(), startDate, endDate);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * <p><b>Получает задачи по ID 🆔</b></p>
     * <p>
     *     Возвращает одну задачу по её уникальному идентификатору.
     * </p>
     *
     * @param taskId ID задачи.
     * @return Найденная сущность {@link Task}.
     * @throws ResponseStatusException
     *         <ul>
     *             <li><b>404 NOT_FOUND</b> (`TASK-001`): Если задача с указанным ID не найдена.</li>
     *         </ul>
     */
    @Transactional(readOnly = true)
    public TaskDTO getTaskById(long taskId) {
        log.debug("📢 Поиск задачи по ID: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.debug("❌ Задача с ID '{}' не найдена.", taskId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(TASK_NOT_FOUND_CODE + ": Задача с ID %d не найдена.", taskId));
                });
        return convertToDTO(task);
    }

    /**
     * <p><b>Получает все задачи пользователя (автор или исполнитель) 🧑‍💻</b></p>
     * <p>
     *     Возвращает список задач, где пользователь является либо автором, либо исполнителем.
     * </p>
     *
     * @param email Email пользователя.
     * @return {@link List} DTO объектов {@link TaskDTO}, принадлежащих пользователю.
     * @throws ResponseStatusException
     *         <ul>
     *             <li><b>404 NOT_FOUND</b> (`USR-002`): Если пользователь с указанным email не найден.</li>
     *         </ul>
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getAllTasksByUser(String email) {
        log.debug("📢 Поиск всех задач для пользователя с email: '{}'.", email);
        AppUser user = userService.getUserByEmail(email);
        List<Task> tasks = taskRepository.findByAuthorOrExecutor(user, user);
        log.info("✅ Найдено {} задач для пользователя '{}'.", tasks.size(), email);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * <p><b>Назначает задачу пользователю ➡️</b></p>
     * <p>
     *     Назначает задачу указанному пользователю. **Только автор задачи может изменить исполнителя.**
     *     Принимает ID задачи и ID пользователя-исполнителя.
     * </p>
     *
     * @param taskId ID задачи для назначения.
     * @param executorId ID пользователя, на которого будет назначена задача.
     * @return Обновленная сущность {@link Task}.
     * @throws ResponseStatusException
     *         <ul>
     *             <li><b>404 NOT_FOUND</b> (`TASK-001`): Если задача не найдена.</li>
     *             <li><b>404 NOT_FOUND</b> (`USR-002`): Если пользователь-исполнитель не найден.</li>
     *             <li><b>403 FORBIDDEN</b> (`TASK-002`): Если текущий пользователь не является автором задачи (проверка на уровне контроллера).</li>
     *         </ul>
     */
    @Transactional
    public TaskDTO assignTask(Long taskId, Long executorId) {
        log.debug("📢 Попытка назначения задачи ID: {} на исполнителя ID: {}", taskId, executorId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.debug("❌ Задача с ID '{}' не найдена для назначения.", taskId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(TASK_NOT_FOUND_CODE + ": Задача не найдена с ID: %d.", taskId));
                });

        AppUser executor = userRepository.findById(executorId)
                .orElseThrow(() -> {
                    log.debug("❌ Пользователь с ID '{}' не найден для назначения задачи ID: {}.", executorId, taskId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(USER_NOT_FOUND_CODE + ": Пользователь не найден с ID: %d.", executorId));
                });

        task.setExecutor(executor);
        Task assignedTask = taskRepository.save(task);
        log.info("✅ Задача с ID {} успешно назначена на пользователя с ID {}.", taskId, executorId);
        return convertToDTO(assignedTask);
    }

    /**
     * <p><b>Устанавливает новый статус задачи 🔄</b></p>
     * <p>
     *     Позволяет автору или текущему исполнителю задачи изменить её статус.
     * </p>
     *
     * @param taskId ID задачи.
     * @param newStatus Новый статус задачи.
     * @return Обновленная сущность {@link Task}.
     * @throws ResponseStatusException
     *         <ul>
     *             <li><b>404 NOT_FOUND</b> (`TASK-001`): Если задача не найдена.</li>
     *             <li><b>403 FORBIDDEN</b> (`TASK-002`): Если текущий пользователь не является автором или исполнителем задачи.</li>
     *             <li><b>400 BAD_REQUEST</b> (`TASK-004`): Если передан недопустимый статус.</li>
     *         </ul>
     */
    @Transactional
    public TaskDTO setStatus(long taskId, Status newStatus) {
        log.debug("📢 Попытка изменения статуса задачи ID: {} на '{}'.", taskId, newStatus);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.debug("❌ Задача с ID '{}' не найдена для изменения статуса.", taskId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(TASK_NOT_FOUND_CODE + ": Задача не найдена с ID: %d.", taskId));
                });

        AppUser currentUser = getAuthenticatedUser();

        boolean isAuthor = task.getAuthor().getId().equals(currentUser.getId());
        boolean isExecutor = (task.getExecutor() != null && task.getExecutor().getId().equals(currentUser.getId()));

        if (!isAuthor && !isExecutor) {
            log.warn("🚫 Пользователь '{}' (ID: {}) пытался изменить статус задачи ID: {}, но не является ни автором, ни исполнителем.",
                    currentUser.getEmail(), currentUser.getId(), taskId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, TASK_ACCESS_DENIED_CODE + ": Вы не имеете прав на изменение статуса этой задачи.");
        }

        task.setStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);
        log.info("✅ Статус задачи с ID {} успешно изменен на '{}' пользователем '{}'.", taskId, newStatus, currentUser.getEmail());
        return convertToDTO(updatedTask);
    }

    /**
     * <p><b>[ADMIN] Удаляет любую задачу по ID 💣</b></p>
     * <p>
     *     Удаляет задачу из системы без проверки прав пользователя, кроме проверки роли ADMIN (которая осуществляется на уровне контроллера).
     *     Предназначен для администраторов.
     * </p>
     *
     * @param taskId ID задачи для удаления.
     * @throws ResponseStatusException
     *         <ul>
     *             <li><b>404 NOT_FOUND</b> (`TASK-001`): Если задача с указанным ID не найдена.</li>
     *         </ul>
     */
    @Transactional
    public void deleteAnyTask(Long taskId) {
        log.info("👑 Попытка удаления любой задачи с ID: {} (админ).", taskId);
        if (!taskRepository.existsById(taskId)) {
            log.error("❌ Задача с ID '{}' не найдена для административного удаления.", taskId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(TASK_NOT_FOUND_CODE + ": Задача с ID %d не найдена.", taskId));
        }
        taskRepository.deleteById(taskId);
        log.info("✅ Задача с ID {} успешно удалена (админ).", taskId);
    }


    /**
     * <p><b>Частично обновляет существующую задачу (PATCH) ✏️</b></p>
     * <p>
     *     Позволяет автору задачи обновить только указанные поля.
     *     Поля, которые в DTO равны `null`, не будут изменены в сущности.
     * </p>
     *
     * @param updateTaskDTO DTO с данными для частичного обновления.
     * @param taskId ID задачи для редактирования.
     * @return Обновленный DTO задачи.
     * @throws ResponseStatusException ...
     */
    @Transactional
    public TaskDTO patchTask(UpdateTaskDTO updateTaskDTO, long taskId) {
        log.info("📢 Попытка частичного обновления (PATCH) задачи с ID: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.debug("❌ Задача с ID '{}' не найдена для частичного редактирования.", taskId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(TASK_NOT_FOUND_CODE + ": Задача с ID %d не найдена.", taskId));
                });

        AppUser currentUser = getAuthenticatedUser();

        if (!task.getAuthor().getId().equals(currentUser.getId())) {
            log.error("🚫 Пользователь '{}' (ID: {}) пытался редактировать задачу ID: {}, к которой у него нет прав.",
                    currentUser.getEmail(), currentUser.getId(), taskId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, TASK_ACCESS_DENIED_CODE + ": Вы не имеете прав на редактирование этой задачи.");
        }

        if (updateTaskDTO.getTitle() != null) {
            task.setTitle(updateTaskDTO.getTitle());
        }
        if (updateTaskDTO.getDescription() != null) {
            task.setDescription(updateTaskDTO.getDescription());
        }
        if (updateTaskDTO.getStatus() != null) {
            task.setStatus(updateTaskDTO.getStatus());
        }
        if (updateTaskDTO.getPriority() != null) {
            task.setPriority(updateTaskDTO.getPriority());
        }
        if (updateTaskDTO.getDueDate() != null) {
            task.setDueDate(updateTaskDTO.getDueDate());
        }

        if (updateTaskDTO.getExecutorUsername() != null) {
            if (updateTaskDTO.getExecutorUsername().isEmpty()) {
                task.setExecutor(null);
            } else {
                AppUser newExecutor = userRepository.findByEmail(updateTaskDTO.getExecutorUsername())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_CODE + ": Исполнитель с email '" + updateTaskDTO.getExecutorUsername() + "' не найден."));
                task.setExecutor(newExecutor);
            }
        }

        task.setUpdatedAt(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);
        log.info("✅ Задача с ID: {} успешно частично обновлена пользователем '{}'.", updatedTask.getId(), currentUser.getEmail());

        return convertToDTO(updatedTask);
    }

    /**
     * <p><b>[ADMIN] Обновляет любую задачу по ID 👑✏️</b></p>
     * <p>
     *     Обновляет данные любой задачи по её ID. Предназначен для администраторов,
     *     позволяя им изменять задачи независимо от автора.
     * </p>
     *
     * @param taskId ID задачи для обновления.
     * @param updateTaskDTO DTO с данными для обновления.
     * @return Обновленная сущность {@link Task}.
     * @throws ResponseStatusException
     *         <ul>
     *             <li><b>404 NOT_FOUND</b> (`TASK-001`): Если задача не найдена.</li>
     *             <li><b>404 NOT_FOUND</b> (`USR-002`): Если указанный исполнитель (если он меняется) не найден.</li>
     *         </ul>
     */
    @Transactional
    public Task updateAnyTask(Long taskId, UpdateTaskDTO updateTaskDTO) {
        log.info("👑 Попытка обновления любой задачи с ID: {} (админ).", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("❌ Задача с ID '{}' не найдена для административного обновления.", taskId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(TASK_NOT_FOUND_CODE + ": Задача не найдена с ID: %d.", taskId));
                });

        modelMapper.map(updateTaskDTO, task);

        if (updateTaskDTO.getExecutorUsername() != null && !updateTaskDTO.getExecutorUsername().isEmpty()) {
            AppUser newExecutor = userRepository.findByEmail(updateTaskDTO.getExecutorUsername())
                    .orElseThrow(() -> {
                        log.error("❌ Новый исполнитель с email '{}' не найден при административном обновлении задачи ID: {}.",
                                updateTaskDTO.getExecutorUsername(), taskId);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_CODE + ": Исполнитель с email '" + updateTaskDTO.getExecutorUsername() + "' не найден.");
                    });
            task.setExecutor(newExecutor);
        } else if (updateTaskDTO.getExecutorUsername() != null && updateTaskDTO.getExecutorUsername().isEmpty()) {
            task.setExecutor(null);
        }

        task.setUpdatedAt(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);
        log.info("✅ Задача с ID {} успешно обновлена (админ).", taskId);
        return updatedTask;
    }


    /**
     * <p><b>Получает задачи, порученные текущему пользователю 🧑‍💻📄</b></p>
     * <p>
     *     Возвращает список задач, где текущий аутентифицированный пользователь указан как исполнитель.
     * </p>
     *
     * @param appUser Сущность {@link AppUser} текущего аутентифицированного пользователя.
     * @return {@link List} DTO объектов {@link TaskDTO}, представляющих задачи.
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getMyTasks(AppUser appUser) {
        log.debug("📢 Получение задач, порученных пользователю '{}' (ID: {}).", appUser.getEmail(), appUser.getId());
        List<Task> tasks = taskRepository.findByExecutor(appUser);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * <p><b>Получает задачи по статусу 📊</b></p>
     * <p>
     *     Возвращает список задач с указанным статусом.
     * </p>
     *
     * @param status Статус задачи (например, `TO_DO`, `IN_PROGRESS`, `DONE`).
     * @return {@link List} сущностей {@link Task}.
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByStatus(Status status) {
        log.debug("📢 Поиск задач по статусу: '{}'.", status);
        List<Task> tasks = taskRepository.findByStatus(status);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * <p><b>Получает ID всех задач 🔢</b></p>
     * <p>
     *     Возвращает список всех ID задач, существующих в системе.
     * </p>
     *
     * @return {@link List} {@link Long} - список всех ID задач.
     */
    @Transactional(readOnly = true)
    public List<Long> getAllTaskIds() {
        log.debug("📢 Получение всех ID задач.");
        return taskRepository.getAllTaskIds();
    }

    /**
     * <p><b>Получает задачи по фильтру (на основе спецификации) 🔎</b></p>
     * <p>
     *     Возвращает список задач, соответствующих критериям, определенным в {@link Specification}.
     *     Позволяет гибко фильтровать задачи по различным полям (например, по заголовку).
     * </p>
     *
     * @param spec {@link Specification<Task>} - критерии фильтрации.
     * @return {@link List} DTO объектов {@link TaskDTO}, соответствующих фильтру.
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByFilter(Specification<Task> spec) {
        log.debug("📢 Поиск задач по пользовательскому фильтру.");

        List<Task> tasks = taskRepository.findAll(spec);

        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * <p><b>Проверяет, является ли пользователь автором задачи (на основе DTO) ✅</b></p>
     * <p>
     *     Вспомогательный метод для проверки прав доступа: является ли пользователь,
     *     представленный email'ом, автором задачи, информация о которой содержится в DTO.
     * </p>
     *
     * @param taskDto {@link TaskDTO} - DTO задачи для проверки.
     * @param email Email пользователя для проверки.
     * @return `true`, если email в DTO совпадает с переданным email'ом, иначе `false`.
     */
    public boolean isTaskAuthor(TaskDTO taskDto, String email) {
        if (taskDto.getAuthor() == null) {
            return false;
        }
        return taskDto.getAuthor().equals(email);
    }

    /**
     * <p><b>Вспомогательный метод: Получает текущего аутентифицированного пользователя из БД 🧑‍💻</b></p>
     * <p>
     *     Используется другими методами сервиса для получения объекта {@link AppUser}
     *     на основе информации об аутентификации.
     * </p>
     *
     * @return Сущность {@link AppUser} текущего пользователя.
     * @throws ResponseStatusException
     *         <ul>
     *             <li><b>401 UNAUTHORIZED</b> (`AUTH-001`): Если пользователь не аутентифицирован.</li>
     *             <li><b>404 NOT_FOUND</b> (`USR-002`): Если аутентифицированный пользователь не найден в БД (очень редкий случай).</li>
     *         </ul>
     */
    private AppUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            log.error("❌ Попытка получить аутентифицированного пользователя без авторизации.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "AUTH-001: Для выполнения этого действия необходимо авторизоваться.");
        }
        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.error("❌ Аутентифицированный пользователь '{}' не найден в базе данных.", username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_CODE + ": Аутентифицированный пользователь не найден.");
                });
    }

    /**
     * <p><b>Вспомогательный метод: Конвертирует сущность задачи в DTO ↔️</b></p>
     * <p>
     *     Использует {@link ModelMapper} для преобразования {@link Task} в {@link TaskDTO}.
     * </p>
     *
     * @param task Сущность {@link Task}.
     * @return {@link TaskDTO} представление задачи.
     */
    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = modelMapper.map(task, TaskDTO.class);
        if (task.getAuthor() != null) {
            dto.setAuthor(task.getAuthor().getEmail());
        }
        if (task.getExecutor() != null) {
            dto.setExecutor(task.getExecutor().getEmail());
        }
        return dto;
    }
}