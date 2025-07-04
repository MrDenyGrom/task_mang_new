package com.example.taskmanagement.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * <p><b>Индикатор Состояния Базы Данных</b></p>
 *
 * <p>
 *     Реализация {@link HealthIndicator}, интегрируемая со Spring Boot Actuator
 *     для мониторинга жизнеспособности подключения к основной базе данных.
 * </p>
 *
 * <p><b>Принцип работы:</b></p>
 * <ul>
 *     <li>Запрашивает соединение у {@link DataSource}.</li>
 *     <li>Проверяет валидность соединения в течение заданного таймаута.</li>
 *     <li>Формирует отчет о состоянии (UP/DOWN) для эндпоинта {@code /actuator/health}.</li>
 * </ul>
 *
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.boot.actuate.health.Health
 * @see javax.sql.DataSource
 */
@Component("databaseHealthIndicator")
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final int VALIDATION_TIMEOUT_SECONDS = 1;

    private final DataSource dataSource;

    /**
     * <p><b>Конструктор Индикатора</b></p>
     *
     * <p>
     *     Внедряет зависимость {@link DataSource} для выполнения проверок соединения.
     * </p>
     *
     * @param dataSource Источник данных для проверки. Не должен быть {@code null}.
     * @throws NullPointerException если {@code dataSource} равен {@code null}.
     */
    public DatabaseHealthIndicator(final DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource не может быть null");
    }

    /**
     * <p><b>Проверка Состояния Подключения</b></p>
     *
     * <p>
     *     Основной метод, выполняющий фактическую проверку соединения с базой данных.
     *     Логика работы следующая:
     * </p>
     * <ul>
     *     <li><b>Успех (UP):</b> Если соединение из пула успешно получено и проходит
     *     валидацию методом {@code connection.isValid()}.</li>
     *     <li><b>Отказ (DOWN):</b> Если соединение невалидно или при попытке его
     *     получения/проверки возникает {@link SQLException}. В отчет о состоянии
     *     добавляется детальная информация об ошибке.</li>
     * </ul>
     *
     * @return Объект {@link Health}, инкапсулирующий статус и детали проверки.
     */
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(VALIDATION_TIMEOUT_SECONDS)) {
                return Health.up().withDetail("message", "База данных доступна").build();
            } else {
                return Health.down().withDetail("message", "Соединение с базой данных невалидно").build();
            }
        } catch (SQLException ex) {
            return Health.down(ex)
                    .withDetail("message", "Ошибка при подключении к базе данных")
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }
}