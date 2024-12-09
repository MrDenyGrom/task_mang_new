package com.example.taskmanagement.model;

import lombok.Getter;
import org.springframework.lang.NonNull;

/**
 * Перечисление, представляющее роль пользователя.
 */
@Getter
public enum Role {
    /**
     * Роль пользователя.
     */
    USER("Пользователь"),
    /**
     * Роль администратора.
     */
    ADMIN("Администратор"),
    /**
     * Роль модератора.
     */
    MODERATOR("Модератор"),
    /**
     * Роль гостя.
     */
    GUEST("Гость");

    /**
     * Отображаемое имя роли.
     */
    private final String displayName;

    /**
     * Конструктор перечисления Role.
     * @param displayName Отображаемое имя роли.
     */
    Role(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Возвращает значение перечисления Role, соответствующее заданному имени.
     * @param roleName Имя роли.
     * @return Значение перечисления Role.
     * @throws IllegalArgumentException Если имя роли {@code null}, пустое или не соответствует ни одному из значений перечисления.
     */
    @NonNull
    public static Role fromString(@NonNull String roleName) {
        if (roleName.isBlank()) {
            throw new IllegalArgumentException("Название роли не может быть пустым");
        }
        String normalizedRoleName = roleName.trim().toLowerCase();
        return java.util.Arrays.stream(values())
                .filter(role -> role.displayName.toLowerCase().equals(normalizedRoleName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестная роль: " + roleName));

    }

    /**
     * Проверяет, является ли роль администраторской.
     * @return {@code true}, если роль администраторская, иначе {@code false}.
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Проверяет, является ли роль пользователя.
     * @return {@code true}, если роль пользователя, иначе {@code false}.
     */
    public boolean isUser() {
        return this == USER;
    }

    /**
     * Проверяет, является ли роль модераторской.
     * @return {@code true}, если роль модераторская, иначе {@code false}.
     */
    public boolean isModerator() {
        return this == MODERATOR;
    }

    /**
     * Проверяет, является ли роль гостевой.
     * @return {@code true}, если роль гостевая, иначе {@code false}.
     */
    public boolean isGuest() {
        return this == GUEST;
    }

    /**
     * Проверяет, имеет ли текущая роль права равные или выше, чем заданная роль.
     * @param otherRole Роль для сравнения.
     * @return {@code true}, если текущая роль имеет права равные или выше, чем заданная роль, иначе {@code false}.
     */
    public boolean hasPermissionsOfOrHigherThan(@NonNull Role otherRole) {
        return this.ordinal() <= otherRole.ordinal();
    }

    /**
     * Возвращает отображаемое имя роли.
     * @return Отображаемое имя роли.
     */
    @Override
    public String toString() {
        return displayName;
    }
}