package com.example.taskmanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p><b>Конфигурация OpenAPI & Swagger UI</b></p>
 *
 * <p>
 *     Настраивает интеграцию с OpenAPI 3 для автоматического добавления
 *     глобальной схемы безопасности JWT (Bearer Token) в пользовательский интерфейс Swagger.
 * </p>
 *
 * <p><b>Ключевые возможности:</b></p>
 * <ul>
 *     <li>Добавляет кнопку <b>"Authorize"</b> в Swagger UI.</li>
 *     <li>Позволяет разработчикам аутентифицироваться прямо из интерфейса документации.</li>
 *     <li>Обеспечивает корректное добавление заголовка {@code Authorization: Bearer <token>}
 *     ко всем защищенным запросам, отправляемым через Swagger UI.</li>
 * </ul>
 *
 * @see io.swagger.v3.oas.models.OpenAPI
 * @see io.swagger.v3.oas.models.security.SecurityScheme
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    /**
     * <p><b>Настройка Глобальной Безопасности OpenAPI</b></p>
     * <p>
     *     Создает кастомный бин {@link OpenAPI}, который регистрирует
     *     схему безопасности Bearer Token и применяет ее ко всем эндпоинтам API.
     * </p>
     * @return Объект {@link OpenAPI} с настроенной схемой JWT.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, createBearerAuthScheme()))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    /**
     * <p><b>Описание Схемы Безопасности Bearer Token</b></p>
     * <p>
     *     Вспомогательный метод, создающий и описывающий {@link SecurityScheme}
     *     для аутентификации по JWT.
     * </p>
     * @return Сконфигурированный объект {@link SecurityScheme}.
     */
    private SecurityScheme createBearerAuthScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name(SECURITY_SCHEME_NAME)
                .description("<b>Аутентификация по JWT.</b><br>Введите токен в формате: <code>Bearer <ваш_токен></code>");
    }
}