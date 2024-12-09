package com.example.taskmanagement;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Главный класс приложения для управления задачами.
 */
@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "API управления задачами",
                version = "1.1",
                description = "REST API для управления задачами, пользователями и комментариями",
                contact = @Contact(
                        name = "Служба поддержки",
                        email = "danillevgentk@mail.ru",
                        url = "https://spb.hh.ru/resume/153de787ff0e1ae19b0039ed1f6a686a727778"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Локальное окружение"),
                @Server(url = "https://api.example.com", description = "Рабочее окружение")
        }
)
public class TaskManagementApplication {

    private static final Logger log = LoggerFactory.getLogger(TaskManagementApplication.class);

    /**
     * Точка входа в приложение.
     * @param args Аргументы командной строки.
     */
    public static void main(String[] args) {
        SpringApplication.run(TaskManagementApplication.class, args);
        log.info("API управления задачами запущено");
    }

    /**
     * Конфигурация CORS.
     */
    @Configuration
    public static class WebConfig implements WebMvcConfigurer {

        private final List<String> allowedOrigins = List.of("http://localhost:8080", "http://frontend_server");

        /**
         * Настраивает CORS mappings.
         * @param registry Реестр CORS.
         */
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOrigins(allowedOrigins.toArray(String[]::new))
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            log.info("CORS конфигурация загружена. Разрешённые origin: {}", allowedOrigins);
        }
    }
}