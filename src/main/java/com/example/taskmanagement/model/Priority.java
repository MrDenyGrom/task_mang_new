package com.example.taskmanagement.model;

import lombok.Getter;
import org.springframework.lang.NonNull;

/**
 * Перечисление, представляющее приоритет задачи.
 */
@Getter
public enum Priority {
    /**
     * Критический приоритет.
     */
    CRITICAL("Критический"),
    /**
     * Высокий приоритет.
     */
    HIGH("Высокий"),
    /**
     * Средний приоритет.
     */
    MEDIUM("Средний"),
    /**
     * Низкий приоритет.
     */
    LOW("Низкий"),
    /**
     * Наинизший приоритет.
     */
    LOWEST("Наинизший");

    /**
     * Отображаемое имя приоритета.
     */
    private final String displayName;

    /**
     * Конструктор перечисления Priority.
     * @param displayName Отображаемое имя приоритета.
     */
    Priority(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Возвращает значение перечисления Priority, соответствующее заданному имени.
     * @param priorityName Имя приоритета.
     * @return Значение перечисления Priority.
     * @throws IllegalArgumentException Если имя приоритета {@code null}, пустое или не соответствует ни одному из значений перечисления.
     * @throws NullPointerException if {@code priorityName} is {@code null}
     */
    @NonNull
    public static Priority fromString(@NonNull String priorityName) {
        if (priorityName.isBlank()) {
            throw new IllegalArgumentException("Название приоритета не может быть пустым");
        }
        String normalizedPriorityName = priorityName.trim().toLowerCase();
        return java.util.Arrays.stream(values())
                .filter(priority -> priority.displayName.toLowerCase().equals(normalizedPriorityName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный приоритет: " + priorityName));
    }

    /**
     * Проверяет, является ли приоритет критическим.
     * @return {@code true}, если приоритет критический, иначе {@code false}.
     */
    public boolean isCritical() {
        return this == CRITICAL;
    }

    /**
     * Проверяет, является ли приоритет высоким.
     * @return {@code true}, если приоритет высокий, иначе {@code false}.
     */
    public boolean isHigh() {
        return this == HIGH;
    }

    /**
     * Проверяет, является ли приоритет средним.
     * @return {@code true}, если приоритет средний, иначе {@code false}.
     */
    public boolean isMedium() {
        return this == MEDIUM;
    }

    /**
     * Проверяет, является ли приоритет низким.
     * @return {@code true}, если приоритет низкий, иначе {@code false}.
     */
    public boolean isLow() {
        return this == LOW;
    }

    /**
     * Проверяет, является ли приоритет наинизшим.
     * @return {@code true}, если приоритет наинизший, иначе {@code false}.
     */
    public boolean isLowest() {
        return this == LOWEST;
    }
}