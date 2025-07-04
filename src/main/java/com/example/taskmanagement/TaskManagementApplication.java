package com.example.taskmanagement;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * <p><b>Точка Входа в Приложение "Task Management API"</b></p>
 *
 * <p>
 *     Этот класс является главным для всего Spring Boot приложения. Он выполняет
 *     автоконфигурацию, сканирование компонентов и запуск встроенного веб-сервера.
 * </p>
 *
 * <p><b>Конфигурация OpenAPI</b></p>
 * <blockquote>
 *     Класс также служит центральным узлом для высокоуровневой документации API
 *     с помощью аннотации {@link OpenAPIDefinition}. Она определяет общую
 *     информацию, которая отображается вверху страницы Swagger UI, такую как
 *     название, версия, контактные данные и доступные серверы.
 * </blockquote>
 *
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see io.swagger.v3.oas.annotations.OpenAPIDefinition
 */
@SpringBootApplication
@EnableJpaAuditing
@OpenAPIDefinition(
        info = @Info(
                title = "Task Management API",
                version = "1.1.1",
                description = "REST API для управления задачами, пользователями и комментариями.",
                contact = @Contact(
                        name = "Команда разработки (Ткаченко Даниил и Крутых Карина, группа 4343)",
                        email = "danillevgentk@mail.ru",
                        url = "https://pro.guap.ru/inside/profile/46020"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Локальный сервер для разработки"),
                @Server(url = "https://api.prod.example.com", description = "Развернутый сервер")
        }
)
public class TaskManagementApplication {

    /**
     * <p><b>Запуск Приложения</b></p>
     *
     * <p>
     *     Инициализирует контекст Spring ApplicationContext и запускает
     *     встроенный веб-сервер (Tomcat), делая приложение
     *     доступным для обработки HTTP-запросов.
     * </p>
     *
     * @param args Аргументы командной строки, переданные при запуске.
     */
    public static void main(String[] args) {
        SpringApplication.run(TaskManagementApplication.class, args);
    }
}