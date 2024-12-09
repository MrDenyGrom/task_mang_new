package com.example.taskmanagement.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность, представляющая задачу в системе управления задачами.
 */
@Data
@Entity
@Table(name = "task")
@EntityListeners(AuditingEntityListener.class)
public class Task {
    /**
     * Уникальный идентификатор задачи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Заголовок задачи. Не может быть пустым.
     */
    @NotBlank(message = "Заголовок задачи не может быть пустым")
    @Column(nullable = false, length = 100)
    private String head;

    /**
     * Описание задачи.
     */
    @Column(length = 1000)
    private String description;

    /**
     * Статус задачи. Не может быть null.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    /**
     * Приоритет задачи. Не может быть null.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    /**
     * Автор задачи. Не может быть null.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    /**
     * Исполнитель задачи.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "executor_id")
    private AppUser executor;

    /**
     * Список комментариев к задаче.
     */
    @OneToMany(mappedBy = "task", fetch = FetchType.EAGER)
    private List<Comment> comments = new ArrayList<>();

    /**
     * Дата и время создания задачи. Заполняется автоматически.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления задачи. Заполняется автоматически.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Планируемая дата выполнения задачи. Должна быть в настоящем или будущем.
     */
    @FutureOrPresent(message = "Дата выполнения должна быть в настоящем или будущем")
    @Column(name = "due_date")
    private LocalDate dueDate;
}