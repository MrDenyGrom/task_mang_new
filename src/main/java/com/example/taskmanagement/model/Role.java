package com.example.taskmanagement.model;

import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p><b>Перечисление: Роль Пользователя (Role)</b></p>
 *
 * <p>
 *     Определяет уровни доступа и права пользователей в системе.
 *     Каждая роль имеет числовой уровень прав для удобного и безопасного сравнения.
 * </p>
 *
 * <p><b>Ключевые архитектурные решения:</b></p>
 * <blockquote>
 *     <p><b>1. Надежная иерархия прав:</b> Вместо ненадежного метода {@code ordinal()}
 *     используется явное поле {@code powerLevel}. Это делает систему прав устойчивой
 *     к изменению порядка объявления констант в перечислении.</p>
 *
 *     <p><b>2. Производительный поиск:</b> Для метода {@link #fromString(String)} используется
 *     статическая, предварительно кэшированная {@link Map}, что обеспечивает
 *     поиск роли за константное время O(1) и избегает перебора массива на каждый вызов.</p>
 * </blockquote>
 */
@Getter
public enum Role {
    /**
     * <p><b>Администратор (уровень 0):</b> Максимальные права доступа в системе.</p>
     */
    ADMIN("Администратор", 0),
    /**
     * <p><b>Модератор (уровень 1):</b> Расширенные права, например, для управления контентом.</p>
     */
    MODERATOR("Модератор", 1),
    /**
     * <p><b>Пользователь (уровень 2):</b> Стандартные права для аутентифицированных пользователей.</p>
     */
    USER("Пользователь", 2),
    /**
     * <p><b>Гость (уровень 3):</b> Минимальные права для неаутентифицированных сессий.</p>
     */
    GUEST("Гость", 3);

    private final String displayName;
    private final int powerLevel;

    private static final Map<String, Role> NAME_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(role -> role.displayName.toLowerCase(), Function.identity()));

    Role(String displayName, int powerLevel) {
        this.displayName = displayName;
        this.powerLevel = powerLevel;
    }

    /**
     * <p><b>Получение Роли из Строки (Оптимизированное)</b></p>
     * <p>
     *     Безопасно и производительно получает экземпляр {@link Role}
     *     из его строкового представления, игнорируя регистр.
     * </p>
     * @param roleName Отображаемое имя роли (например, "Администратор").
     * @return Соответствующий экземпляр {@link Role}.
     * @throws IllegalArgumentException если имя роли пустое или не найдено.
     */
    @NonNull
    public static Role fromString(@NonNull String roleName) {
        String normalized = roleName.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Название роли не может быть пустым");
        }
        Role role = NAME_MAP.get(normalized);
        if (role == null) {
            throw new IllegalArgumentException("Неизвестная роль: " + roleName);
        }
        return role;
    }

    /**
     * <p><b>Проверка Уровня Доступа</b></p>
     * <p>
     *     Сравнивает уровень прав текущей роли с другой, используя поле {@code powerLevel}.
     *     Чем меньше значение {@code powerLevel}, тем выше права.
     * </p>
     * @param otherRole Роль, с которой производится сравнение.
     * @return {@code true}, если текущая роль имеет права, равные или превышающие права {@code otherRole}.
     */
    public boolean hasPermissionsOfOrHigherThan(@NonNull Role otherRole) {
        return this.powerLevel <= otherRole.powerLevel;
    }

    @Override
    public String toString() {
        return displayName;
    }
}