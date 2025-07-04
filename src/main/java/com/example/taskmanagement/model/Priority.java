package com.example.taskmanagement.model;

import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.Arrays;

/**
 * <p><b>Перечисление: Приоритет Задачи (Priority)</b></p>
 *
 * <p>
 *     Определяет степень важности и срочности задачи. Используется для
 *     сортировки и визуального выделения задач в пользовательском интерфейсе.
 * </p>
 */
@Getter
public enum Priority {
    /**
     * <p>Критический. Требует немедленного внимания.</p>
     */
    CRITICAL("Критический"),
    /**
     * <p>Высокий. Задача имеет высокую важность.</p>
     */
    HIGH("Высокий"),
    /**
     * <p>Средний. Стандартный приоритет для большинства задач.</p>
     */
    MEDIUM("Средний"),
    /**
     * <p>Низкий. Задача не является срочной.</p>
     */
    LOW("Низкий"),
    /**
     * <p>Наинизший. Задача может быть выполнена в последнюю очередь.</p>
     */
    LOWEST("Наинизший");

    private final String displayName;

    Priority(String displayName) {
        this.displayName = displayName;
    }

    /**
     * <p><b>Фабричный Метод из Строки</b></p>
     * <p>
     *     Преобразует строковое представление приоритета (например, "Высокий")
     *     в соответствующую константу перечисления, игнорируя регистр.
     * </p>
     * @param priorityName Отображаемое имя приоритета для поиска.
     * @return Соответствующий экземпляр {@link Priority}.
     * @throws IllegalArgumentException если имя приоритета пустое или не найдено.
     */
    @NonNull
    public static Priority fromString(@NonNull String priorityName) {
        if (priorityName.isBlank()) {
            throw new IllegalArgumentException("Название приоритета не может быть пустым");
        }
        String normalizedPriorityName = priorityName.trim().toLowerCase();
        return Arrays.stream(values())
                .filter(priority -> priority.displayName.toLowerCase().equals(normalizedPriorityName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный приоритет: " + priorityName));
    }

}