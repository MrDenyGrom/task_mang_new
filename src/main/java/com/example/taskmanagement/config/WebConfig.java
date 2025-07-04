package com.example.taskmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * <p><b>Глобальная Конфигурация Web MVC</b></p>
 *
 * <p>
 *     Определяет общие настройки для веб-слоя приложения, реализуя интерфейс
 *     {@link WebMvcConfigurer}. Основное назначение этого класса — централизованное
 *     управление политикой Cross-Origin Resource Sharing (CORS).
 * </p>
 *
 * <p><b>Назначение CORS:</b></p>
 * <blockquote>
 *     Позволяет веб-приложениям (например, SPA на React, Angular, Vue),
 *     загруженным с одного домена (origin), безопасно выполнять запросы
 *     к этому API, который находится на другом домене.
 * </blockquote>
 *
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 * @see org.springframework.web.servlet.config.annotation.CorsRegistry
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    /**
     * <p><b>Настройка Правил CORS</b></p>
     *
     * <p>
     *     Конфигурирует глобальные правила CORS для всех эндпоинтов ({@code "/**"}) в приложении.
     * </p>
     * <ul>
     *     <li><b>allowedOrigins:</b> Разрешает запросы от доменов, перечисленных в
     *     свойстве {@code app.cors.allowed-origins}.</li>
     *     <li><b>allowedMethods:</b> Явно указывает разрешенные HTTP-методы.</li>
     *     <li><b>allowedHeaders:</b> Разрешает все заголовки в запросе.</li>
     *     <li><b>allowCredentials:</b> Разрешает передачу cookie и заголовков авторизации.</li>
     * </ul>
     *
     * @param registry Реестр для регистрации правил CORS.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}