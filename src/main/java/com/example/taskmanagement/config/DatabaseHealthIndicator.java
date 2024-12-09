package com.example.taskmanagement.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Индикатор состояния базы данных для Spring Boot Actuator.
 * Проверяет доступность базы данных путем попытки установить соединение.
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Проверка доступности базы данных.
     *
     * @return Объект Health, представляющий состояние базы данных.
     * Возвращает {@link Health#up()} если соединение успешно установлено.
     * Возвращает {@link Health#down()} если соединение не установлено или возникла ошибка.
     */
    @Override
    public Health health() {
        try {
            if (dataSource.getConnection().isValid(1)) {
                return Health.up().build();
            } else {
                return Health.down().build();
            }
        } catch (SQLException e) {
            return Health.down().withException(e).build();
        }
    }
}