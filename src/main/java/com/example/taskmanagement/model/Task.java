package com.example.taskmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p><b>Сущность: Задача (Task)</b></p>
 *
 * <p>
 *     Представляет собой центральную доменную модель приложения. Задача является
 *     основным объектом, вокруг которого строятся все бизнес-процессы.
 * </p>
 *
 * <p><b>Ключевые архитектурные аспекты:</b></p>
 * <ul>
 *     <li><b>Аудит:</b> Интеграция с {@link AuditingEntityListener} для автоматического
 *     заполнения полей {@code createdAt} и {@code updatedAt}.</li>
 *     <li><b>Оптимизация производительности:</b> Все связи с другими сущностями
 *     (<code>author</code>, <code>executor</code>, <code>comments</code>) по умолчанию
 *     используют {@link FetchType#LAZY} для предотвращения избыточных запросов к БД.</li>
 * </ul>
 *
 * @see com.example.taskmanagement.model.AppUser
 * @see com.example.taskmanagement.model.Comment
 * @see org.springframework.data.jpa.domain.support.AuditingEntityListener
 */
@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@RequiredArgsConstructor
@ToString(exclude = {"author", "executor", "comments"})
@EqualsAndHashCode(of = "id")
public class Task {

    /**
     * <p><b>Уникальный Идентификатор</b></p>
     * <p>Первичный ключ сущности. Генерируется автоматически базой данных.</p>
     * <blockquote>
     *     <b>Стратегия генерации:</b> {@link GenerationType#IDENTITY} является
     *     предпочтительной для большинства современных СУБД (PostgreSQL, MySQL),
     *     поскольку она делегирует генерацию ID самой базе данных.
     * </blockquote>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * <p><b>Заголовок Задачи</b></p>
     * <p>Краткое, обязательное для заполнения наименование задачи.</p>
     */
    @NotBlank(message = "Заголовок задачи не может быть пустым")
    @Size(max = 100, message = "Заголовок не должен превышать 100 символов")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /**
     * <p><b>Описание Задачи</b></p>
     * <p>Полное, развернутое описание сути задачи.</p>
     */
    @Size(max = 2000, message = "Описание не должно превышать 2000 символов")
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * <p><b>Статус Выполнения</b></p>
     * <p>Текущий этап жизненного цикла задачи (например, "В РАБОТЕ", "ВЫПОЛНЕНО").</p>
     * <blockquote>
     *     <b>Хранение в БД:</b> {@link EnumType#STRING} используется для хранения
     *     имени перечисления в виде строки, что повышает читаемость данных в базе.
     * </blockquote>
     */
    @NotNull(message = "Статус не может быть null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    /**
     * <p><b>Приоритет Задачи</b></p>
     * <p>Определяет важность задачи (например, "ВЫСОКИЙ", "НИЗКИЙ").</p>
     */
    @NotNull(message = "Приоритет не может быть null")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    /**
     * <p><b>Автор Задачи</b></p>
     * <p>Пользователь, создавший задачу. Связь является обязательной.</p>
     * <blockquote>
     *     <b>Оптимизация:</b> Связь определена как {@link FetchType#LAZY}, чтобы
     *     данные автора не загружались вместе с задачей до явного обращения к ним.
     * </blockquote>
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    /**
     * <p><b>Исполнитель Задачи</b></p>
     * <p>Пользователь, ответственный за выполнение задачи. Может быть не назначен.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executor_id")
    private AppUser executor;

    /**
     * <p><b>Комментарии к Задаче</b></p>
     * <p>Список всех комментариев, связанных с данной задачей.</p>
     * <blockquote>
     *     <p><b>Владение связью:</b> {@code mappedBy = "task"} указывает, что
     *     управление этой связью (внешним ключом) находится на стороне сущности {@link Comment}.</p>
     *     <p><b>Каскадные операции:</b> {@code cascade = CascadeType.ALL} означает, что
     *     любые операции (persist, merge, remove) с задачей будут каскадно применены к ее комментариям.</p>
     *     <p><b>Удаление "сирот":</b> {@code orphanRemoval = true} обеспечивает удаление комментария
     *     из БД, если он был удален из данной коллекции.</p>
     * </blockquote>
     */
    @OneToMany(
            mappedBy = "task",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Comment> comments = new ArrayList<>();

    /**
     * <p><b>Дата и Время Создания</b></p>
     * <p>Момент времени, когда задача была впервые сохранена в базе данных.</p>
     * <blockquote>
     *     Поле заполняется автоматически механизмом аудита Spring Data JPA.
     * </blockquote>
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * <p><b>Дата и Время Обновления</b></p>
     * <p>Момент времени последнего изменения задачи.</p>
     * <blockquote>
     *     Поле обновляется автоматически при каждом изменении сущности.
     * </blockquote>
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * <p><b>Срок Выполнения</b></p>
     * <p>Планируемая дата, к которой задача должна быть выполнена.</p>
     */
    @FutureOrPresent(message = "Дата выполнения должна быть в настоящем или будущем")
    @Column(name = "due_date")
    private LocalDate dueDate;
}