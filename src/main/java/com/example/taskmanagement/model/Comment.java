package com.example.taskmanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * <p><b>Сущность: Комментарий (Comment)</b></p>
 *
 * <p>
 *     Представляет собой комментарий, оставленный пользователем к определенной задаче.
 *     Эта сущность связывает пользователей и задачи, формируя обсуждения.
 * </p>
 *
 * <p><b>Ключевые архитектурные аспекты:</b></p>
 * <ul>
 *     <li><b>Аудит:</b> Интеграция с {@link AuditingEntityListener} для автоматического
 *     заполнения полей даты создания и обновления.</li>
 *     <li><b>Предотвращение рекурсии:</b> Аннотация {@link JsonIgnore} на поле {@code task}
 *     является критически важной для разрыва циклической зависимости при сериализации
 *     объектов в JSON (Task → List<Comment> → Task).</li>
 *     <li><b>Оптимизация запросов:</b> Все связи {@code @ManyToOne} используют {@link FetchType#LAZY}
 *     для предотвращения избыточной загрузки связанных сущностей.</li>
 * </ul>
 *
 * @see com.example.taskmanagement.model.Task
 * @see com.example.taskmanagement.model.AppUser
 * @see org.springframework.data.jpa.domain.support.AuditingEntityListener
 */
@Entity
@Table(name = "comments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@RequiredArgsConstructor
@ToString(exclude = {"task", "appUser"})
@EqualsAndHashCode(of = "id")
public class Comment {

    /**
     * <p><b>Уникальный Идентификатор</b></p>
     * <p>Первичный ключ, генерируемый базой данных.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * <p><b>Связанная Задача</b></p>
     * <p>Задача, к которой относится данный комментарий. Является "владельцем" связи.</p>
     * <blockquote>
     *     <p><b>{@link JsonIgnore}:</b> Обязательная аннотация для предотвращения
     *     бесконечной рекурсии при JSON-сериализации.</p>
     *     <p><b>{@code optional = false}:</b> Гарантирует, что комментарий не может
     *     существовать без задачи. На уровне SQL это приводит к использованию
     *     более эффективного {@code INNER JOIN} вместо {@code LEFT OUTER JOIN}.</p>
     * </blockquote>
     */
    @NotNull(message = "Комментарий должен быть привязан к задаче")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnore
    private Task task;

    /**
     * <p><b>Автор Комментария</b></p>
     * <p>Пользователь, оставивший комментарий.</p>
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "app_user_id", nullable = false)
    private AppUser appUser;

    /**
     * <p><b>Текст Комментария</b></p>
     * <p>Содержимое комментария. Не может быть пустым.</p>
     */
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 2000, message = "Текст комментария должен быть короче 2000 символов")
    @Column(nullable = false, length = 2000)
    private String text;

    /**
     * <p><b>Дата и Время Создания</b></p>
     * <p>Момент времени, когда комментарий был сохранен. Заполняется автоматически.</p>
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * <p><b>Дата и Время Обновления</b></p>
     * <p>Момент времени последнего редактирования комментария. Обновляется автоматически.</p>
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}