package com.example.taskmanagement.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Перечисление, представляющее статус задачи.
 */
@Getter
public enum Status {
    /**
     * Статус "Ожидание".
     */
    WAITING("Ожидание"),
    /**
     * Статус "В работе".
     */
    IN_PROGRESS("В работе"),
    /**
     * Статус "Выполнено".
     */
    COMPLETED("Выполнено"),
    /**
     * Статус "Отменено".
     */
    CANCELLED("Отменено"),
    /**
     * Статус "Приостановлено".
     */
    ON_HOLD("Приостановлено"),
    /**
     * Статус "Проверка".
     */
    IN_REVIEW("Проверка"),
    /**
     * Статус "Отклонено".
     */
    REJECTED("Отклонено");

    /**
     * Отображаемое имя статуса.
     */
    private final String displayName;

    /**
     * Конструктор перечисления Status.
     * @param displayName Отображаемое имя статуса.
     */
    Status(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Возвращает значение перечисления Status, соответствующее заданному имени.
     * @param status Имя статуса.
     * @return Значение перечисления Status.
     * @throws IllegalArgumentException Если статус {@code null} или не соответствует ни одному из значений перечисления.
     * @throws NullPointerException if {@code status} is {@code null}
     */
    @NotNull
    public static Status fromString(@NotNull String status) {
        return java.util.Arrays.stream(Status.values())
                .filter(s -> s.displayName.equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный статус: " + status));
    }

    /**
     * Проверяет, является ли статус "Выполнено".
     * @return {@code true}, если статус "Выполнено", иначе {@code false}.
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }

    /**
     * Проверяет, является ли статус "В работе".
     * @return {@code true}, если статус "В работе", иначе {@code false}.
     */
    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    /**
     * Проверяет, является ли статус "Отменено".
     * @return {@code true}, если статус "Отменено", иначе {@code false}.
     */
    public boolean isCancelled() {
        return this == CANCELLED;
    }

    /**
     * Проверяет, является ли статус "Приостановлено".
     * @return {@code true}, если статус "Приостановлено", иначе {@code false}.
     */
    public boolean isOnHold() {
        return this == ON_HOLD;
    }

    /**
     * Проверяет, является ли статус "Ожидание".
     * @return {@code true}, если статус "Ожидание", иначе {@code false}.
     */
    public boolean isWaiting() {
        return this == WAITING;
    }

    /**
     * Проверяет, является ли статус "Проверка".
     * @return {@code true}, если статус "Проверка", иначе {@code false}.
     */
    public boolean isInReview() {
        return this == IN_REVIEW;
    }

    /**
     * Проверяет, является ли статус "Отклонено".
     * @return {@code true}, если статус "Отклонено", иначе {@code false}.
     */
    public boolean isRejected() {
        return this == REJECTED;
    }

    /**
     * Возвращает следующий статус в workflow.
     * @return Следующий статус.
     * @throws IllegalStateException Если текущий статус не имеет следующего состояния.
     */
    public Status nextStatus() {
        return switch (this) {
            case WAITING -> IN_PROGRESS;
            case IN_PROGRESS -> IN_REVIEW;
            case IN_REVIEW -> COMPLETED;
            case COMPLETED, CANCELLED, REJECTED, ON_HOLD -> this;
            default -> throw new IllegalStateException("Неизвестный статус: " + this);
        };
    }
}