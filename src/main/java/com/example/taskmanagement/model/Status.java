package com.example.taskmanagement.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.Arrays;

/**
 * <p><b>Перечисление: Статус Задачи (Status)</b></p>
 *
 * <p>
 *     Определяет полный жизненный цикл задачи, представляя собой конечный автомат.
 *     Каждый элемент перечисления соответствует определенному этапу в рабочем процессе (workflow).
 * </p>
 *
 * <p><b>Ключевые особенности:</b></p>
 * <ul>
 *     <li>Предоставляет удобные вспомогательные методы (например, {@code isCompleted()}).</li>
 *     <li>Содержит простую логику рабочего процесса в методе {@link #nextStatus()}.</li>
 *     <li>Обеспечивает безопасное преобразование из строки в константу через {@link #fromString(String)}.</li>
 * </ul>
 */
@Getter
public enum Status {
    /**
     * <p>Задача создана, но работа по ней еще не началась.</p>
     */
    WAITING("Ожидание"),

    /**
     * <p>Задача находится в активной фазе выполнения.</p>
     */
    IN_PROGRESS("В работе"),

    /**
     * <p>Задача успешно завершена.</p>
     */
    COMPLETED("Выполнено"),

    /**
     * <p>Выполнение задачи было отменено.</p>
     */
    CANCELLED("Отменено"),

    /**
     * <p>Работа по задаче временно приостановлена.</p>
     */
    ON_HOLD("Приостановлено"),

    /**
     * <p>Задача выполнена и ожидает проверки (например, со стороны автора или модератора).</p>
     */
    IN_REVIEW("Проверка"),

    /**
     * <p>Результат выполнения задачи был проверен и отклонен.</p>
     */
    REJECTED("Отклонено");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    /**
     * <p><b>Фабричный Метод из Строки</b></p>
     * <p>
     *     Преобразует строковое представление статуса (например, "В работе")
     *     в соответствующую константу перечисления, игнорируя регистр.
     * </p>
     * @param status Отображаемое имя статуса для поиска.
     * @return Соответствующий экземпляр {@link Status}.
     * @throws IllegalArgumentException если статус не найден.
     */
    @NotNull
    public static Status fromString(@NotNull String status) {
        return Arrays.stream(Status.values())
                .filter(s -> s.displayName.equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный статус: " + status));
    }

    /**
     * <p><b>Проверка на Статус 'Выполнено'</b></p>
     * @return {@code true}, если текущий статус {@code COMPLETED}, иначе {@code false}.
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }

    /**
     * <p><b>Проверка на Статус 'В Работе'</b></p>
     * @return {@code true}, если текущий статус {@code IN_PROGRESS}, иначе {@code false}.
     */
    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    /**
     * <p><b>Переход к Следующему Статусу</b></p>
     * <p>
     *     Реализует логику простого рабочего процесса (workflow) для задачи.
     *     Возвращает следующий логический статус или текущий, если он является терминальным.
     * </p>
     * @return Следующий статус в цепочке жизненного цикла.
     */
    public Status nextStatus() {
        return switch (this) {
            case WAITING -> IN_PROGRESS;
            case IN_PROGRESS -> IN_REVIEW;
            case IN_REVIEW -> COMPLETED;
            default -> this;
        };
    }
}