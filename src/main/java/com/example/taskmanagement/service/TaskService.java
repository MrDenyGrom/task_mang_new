package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.CreateTaskDTO;
import com.example.taskmanagement.dto.TaskDTO;
import com.example.taskmanagement.dto.TaskFilterDTO;
import com.example.taskmanagement.dto.UpdateTaskDTO;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.exception.TaskNotFoundException;
import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.model.Status;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.modelmapper.ModelMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления задачами.
 */
@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, UserService userService, ModelMapper modelMapper) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    /**
     * Создает новую задачу.
     * @param taskDTO DTO для создания задачи.
     * @return Созданная задача.
     * @throws AccessDeniedException Если пользователь не аутентифицирован.
     * @throws ResourceNotFoundException Если автор задачи не найден.
     */
    public Task createTask(CreateTaskDTO taskDTO) {
        log.info("Попытка создания новой задачи с заголовком: {}", taskDTO.getHead());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.error("Ошибка аутентификации. Пользователь не авторизован.");
            throw new AccessDeniedException("Для создания задачи необходимо авторизоваться.");
        }

        String username = authentication.getName();
        log.debug("Авторизованный пользователь: {}", username);

        AppUser author = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Task task = mapCreateDTOToTask(taskDTO, author);

        Task savedTask = taskRepository.save(task);
        log.info("Задача успешно создана с ID: {}", savedTask.getId());

        return savedTask;
    }

    private Task mapCreateDTOToTask(CreateTaskDTO taskDTO, AppUser author) {
        Task task = modelMapper.map(taskDTO, Task.class);
        task.setAuthor(author);
        if (taskDTO.getExecutorUsername() != null) {
            AppUser executor = userRepository.findByEmail(taskDTO.getExecutorUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("Исполнитель не найден"));
            task.setExecutor(executor);
        }

        task.setDueDate(taskDTO.getDueDate());
        return task;
    }

    /**
     * Редактирует задачу.
     * @param taskDTO DTO с данными для обновления задачи.
     * @param id ID задачи.
     * @return Обновленная задача.
     * @throws TaskNotFoundException Если задача не найдена.
     * @throws AccessDeniedException Если у пользователя нет прав на редактирование задачи.
     * @throws ResourceNotFoundException Если исполнитель не найден.
     */
    public Task editTask(UpdateTaskDTO taskDTO, long id) {

        Task task = findTaskById(id);
        AppUser currentUser = getAuthenticatedUser();

        checkTaskAccess(task, currentUser);

        updateTaskFromDTO(task, taskDTO);
        task.setUpdatedAt(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);
        log.info("Задача с ID: {} успешно обновлена", updatedTask.getId());

        return updatedTask;
    }

    private Task findTaskById(long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача с ID: " + id + " не найдена"));
    }

    private AppUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("Вы должны быть авторизованы для выполнения этого действия.");
        }

        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Аутентифицированный пользователь не найден"));
    }

    private void updateTaskFromDTO(Task task, UpdateTaskDTO taskDTO) {
        modelMapper.map(taskDTO, task);
        task.setDueDate(taskDTO.getDueDate());
        if (taskDTO.getExecutorUsername() != null) {
            task.setExecutor(userRepository.findByEmail(taskDTO.getExecutorUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("Исполнитель не найден")));
        }
    }

    /**
     * Проверяет, имеет ли пользователь права на редактирование задачи.
     * @param task Задача.
     * @param currentUser Пользователь.
     */
    private void checkTaskAccess(Task task, AppUser currentUser) {
        if (!task.getAuthor().equals(currentUser)) {
            log.error("Пользователь '{}' не имеет прав на редактирование задачи с ID: {}", currentUser.getEmail(), task.getId());
            throw new AccessDeniedException("Вы не имеете прав на редактирование этой задачи.");
        }
    }

    /**
     * Удаляет задачу.
     * @param id ID задачи.
     * @throws TaskNotFoundException Если задача не найдена.
     * @throws AccessDeniedException Если у пользователя нет прав на удаление задачи.
     */
    public void deleteTask(long id) {

        Task task = findTaskById(id);
        AppUser currentUser = getAuthenticatedUser();

        checkTaskAccess(task, currentUser);

        taskRepository.deleteById(id);
        log.info("Задача с ID: {} успешно удалена", id);
    }

    /**
     * Возвращает все задачи.
     * @return Список задач.
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAllWithComments();
    }

    private Specification<Task> createTaskSpecification(TaskFilterDTO filterDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filterDTO != null && filterDTO.getHead() != null) {
                predicates.add(criteriaBuilder.like(root.get("head"), "%" + filterDTO.getHead() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Возвращает задачи между двумя датами.
     * @param startDate Начальная дата.
     * @param endDate Конечная дата.
     * @return Список задач.
     */
    public List<TaskDTO> getTasksBetweenDates(LocalDate startDate, LocalDate endDate) {
        List<Task> tasks = taskRepository.findByDueDateBetween(startDate, endDate);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Конвертирует задачу в DTO.
     * @param task Задача.
     * @return DTO задачи.
     */
    private TaskDTO convertToDTO(Task task) {
        return modelMapper.map(task, TaskDTO.class);
    }

    /**
     * Возвращает задачи по фильтру.
     * @param spec Спецификация фильтра.
     * @return Список задач.
     */
    public List<Task> getTasksByFilter(Specification<Task> spec) {
        if (spec == null) {
            return taskRepository.findAll();
        }
        return taskRepository.findAll(spec);
    }

    /**
     * Возвращает все задачи пользователя.
     * @param email Email пользователя.
     * @return Список задач.
     * @throws UsernameNotFoundException Если пользователь не найден.
     */
    public List<TaskDTO> getAllTasksByUser(String email) {
        AppUser user = userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));
        List<Task> tasks = taskRepository.findByAuthorOrExecutor(user, user);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает задачу по ID.
     * @param taskId ID задачи.
     * @return Задача.
     * @throws TaskNotFoundException Если задача не найдена.
     */
    public Task getTaskById(long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена"));
    }

    /**
     * Проверяет, является ли пользователь автором задачи.
     * @param task Задача.
     * @param email Email пользователя.
     * @return true, если пользователь является автором задачи, иначе false.
     */
    public boolean isTaskAuthor(Task task, String email) {
        return task.getAuthor().getEmail().equals(email);
    }

    /**
     * Возвращает все ID задач.
     * @return Список ID задач.
     */
    public List<Long> getAllTaskIds() {
        return taskRepository.getAllTaskIds();
    }

    /**
     * Возвращает задачи по статусу.
     * @param status Статус задачи.
     * @return Список задач.
     */
    public List<Task> getTasksByStatus(Status status) {
        return taskRepository.findByStatus(status);
    }

    /**
     * Назначает задачу пользователю.
     * @param taskId ID задачи.
     * @param userId ID пользователя.
     * @return Обновленная задача.
     * @throws TaskNotFoundException Если задача не найдена.
     * @throws ResourceNotFoundException Если пользователь не найден.
     */
    public Task assignTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена с id: " + taskId));

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден с id: " + userId));

        task.setExecutor(user);
        log.info("Задача с ID {} назначена на пользователя с ID {}", taskId, userId);
        return taskRepository.save(task);
    }

    /**
     * Удаляет задачу по ID.
     * @param id ID задачи.
     */
    public void deleteAnyTask(Long id) {
        taskRepository.deleteById(id);
        log.info("Задача с ID {} удалена", id);
    }

    /**
     * Обновляет задачу по ID (без проверки прав).
     * @param id ID задачи.
     * @param updateTaskDTO DTO с данными для обновления.
     * @return Обновленная задача.
     * @throws TaskNotFoundException Если задача не найдена.
     */
    public Task updateAnyTask(Long id, UpdateTaskDTO updateTaskDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена с id: " + id));

        updateTaskFromDTO(task, updateTaskDTO);
        log.info("Задача с ID {} обновлена", id);
        return taskRepository.save(task);
    }

    /**
     * Устанавливает статус задачи.
     * @param id ID задачи.
     * @param status Новый статус задачи.
     * @return Обновленная задача.
     * @throws TaskNotFoundException Если задача не найдена.
     * @throws AccessDeniedException Если у пользователя нет прав на изменение статуса задачи.
     */
    public Task setStatus(long id, Status status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена с ID: " + id));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow(ResourceNotFoundException::new);

        if (!task.getAuthor().equals(currentUser) && !task.getExecutor().equals(currentUser)) {
            throw new AccessDeniedException("Вы не имеете прав на изменение статуса этой задачи.");
        }
        task.setStatus(status);
        log.info("Статус задачи с ID {} изменен на {}", id, status);
        return taskRepository.save(task);
    }

    /**
     * Возвращает задачи, назначенные на пользователя.
     * @param appUser Пользователь.
     * @return Список задач.
     */
    public List<Task> getMyTasks(AppUser appUser) {
        return taskRepository.findByExecutor(appUser);
    }
}