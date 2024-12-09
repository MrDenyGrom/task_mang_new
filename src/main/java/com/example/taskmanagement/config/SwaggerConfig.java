package com.example.taskmanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Конфигурационный класс для Swagger UI.
 * Настраивает документацию API, включая информацию о сервере, авторизацию и контактную информацию.
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port}")
    private String serverPort;

    @Value("${swagger.server.url}")
    private String serverUrl;

    /**
     * Создание бина OpenAPI для настройки Swagger UI.
     *
     * @return объект OpenAPI с конфигурацией Swagger.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server().url(serverUrl + ":" + serverPort);
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearer-key");
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Схема аутентификации Bearer Token.  Добавьте 'Bearer ' + ваш токен в поле 'Value'.");

        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(server))
                .security(List.of(securityRequirement))
                .components(new Components().addSecuritySchemes("bearer-key", securityScheme));
    }

    /**
     * Создание объекта Info с информацией о API.
     *
     * @return объект Info с информацией о API.
     */
    private Info apiInfo() {
        return new Info()
                .title("API Таск-менеджер")
                .version("1.0.0")
                .description("API для управления задачами и комментариями")
                .termsOfService("http://swagger.io/terms/")
                .contact(apiContact())
                .license(apiLicense());
    }

    /**
     * Создание объекта Contact с контактной информацией.
     *
     * @return объект Contact с контактной информацией.
     */
    private Contact apiContact() {
        return new Contact()
                .name("Разработчик")
                .url("https://spb.hh.ru/resume/153de787ff0e1ae19b0039ed1f6a686a727778")
                .email("daniilevgentk@mail.ru");
    }

    /**
     * Создание объекта License с информацией о лицензии.
     *
     * @return объект License с информацией о лицензии.
     */
    private License apiLicense() {
        return new License()
                .name("Apache 2.0")
                .url("http://www.apache.org/licenses/LICENSE-2.0.html");
    }
}