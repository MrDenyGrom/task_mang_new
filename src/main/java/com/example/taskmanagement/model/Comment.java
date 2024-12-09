package com.example.taskmanagement.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Сущность, представляющий комментарий к задаче.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    /**
     * Идентификатор комментария.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Задача, к которой относится комментарий.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinColumn(name = "task_id", nullable = false)
    @NotNull
    private Task task;

    /**
     * Пользователь, оставивший комментарий.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_user_id", nullable = false)
    @NotNull
    private AppUser appUser;

    /**
     * Текст комментария.  Не может быть пустым и должен быть короче 2000 символов.
     */
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 2000, message = "Текст комментария должен быть короче 2000 символов")
    @Column(nullable = false, length = 2000)
    private String text;

    /**
     * Дата и время создания комментария.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления комментария.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

}