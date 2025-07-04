package com.example.taskmanagement.controller;

import com.example.taskmanagement.config.UserDetail;
import com.example.taskmanagement.dto.*;
import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.model.Role;
import com.example.taskmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * <p><b>Контроллер для Управления Пользователями и Аутентификацией</b></p>
 * <p>Предоставляет REST API эндпоинты для всех операций, связанных с пользователями и их доступом.</p>
 * <p>
 *     Этот контроллер служит точкой входа для:
 *     <ul>
 *         <li>Регистрации новых пользователей.</li>
 *         <li>Аутентификации существующих пользователей и выдачи JWT токена.</li>
 *         <li>Получения и обновления информации о текущем аутентифицированном пользователе.</li>
 *         <li>Администрирования пользователей (просмотр, изменение ролей, удаление) — доступно только для ADMIN.</li>
 *     </ul>
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "1.👤 Пользователи и Аутентификация", description = """
    ### API для управления полным жизненным циклом пользователей.
    Этот раздел API отвечает за всё, что связано с учетными записями:
    *   **Регистрация** новых пользователей ✨.
    *   **Аутентификация** (логин) и получение JWT 🔑.
    *   **Управление** профилем текущего пользователя (смена пароля) 🔄.
    *   **Администрирование** всех пользователей (только для роли `ADMIN`) 👑.
    
    ---
    **Безопасность**: Все защищенные эндпоинты требуют `Bearer` токен, полученный через эндпоинт `/login`.
    """)
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    // --- Статические примеры для ответов об ошибках ---
    private static final String ERROR_400_INVALID_DATA_EXAMPLE = """
            {
                "timestamp": "2023-10-27T10:30:00.123Z",
                "status": 400,
                "error": "Bad Request",
                "message": "Validation failed for object='userRegistrationRequest'. Error count: 1",
                "details": [
                    {
                        "field": "password",
                        "message": "Пароль не может быть пустым"
                    }
                ],
                "path": "/api/users/register"
            }
            """;
    private static final String ERROR_400_WRONG_OLD_PASSWORD_EXAMPLE = """
            {
                "timestamp": "2023-10-27T10:30:00.123Z",
                "status": 400,
                "error": "Bad Request",
                "message": "USR-003: Неверный старый пароль",
                "path": "/api/users/me/password"
            }
            """;
    private static final String ERROR_401_UNAUTHORIZED_EXAMPLE = """
            {
                "timestamp": "2023-10-27T10:30:00.123Z",
                "status": 401,
                "error": "Unauthorized",
                "message": "AUTH-001: Неверный email или пароль",
                "path": "/api/users/login"
            }
            """;
    private static final String ERROR_403_FORBIDDEN_EXAMPLE = """
            {
                "timestamp": "2023-10-27T10:30:00.123Z",
                "status": 403,
                "error": "Forbidden",
                "message": "Доступ запрещен",
                "path": "/api/users/admin/users"
            }
            """;
    private static final String ERROR_404_USER_NOT_FOUND_EXAMPLE = """
            {
                "timestamp": "2023-10-27T10:30:00.123Z",
                "status": 404,
                "error": "Not Found",
                "message": "USR-002: Пользователь с ID 999 не найден",
                "path": "/api/users/admin/users/999"
            }
            """;
    private static final String ERROR_409_EMAIL_CONFLICT_EXAMPLE = """
            {
                "timestamp": "2023-10-27T10:30:00.123Z",
                "status": 409,
                "error": "Conflict",
                "message": "USR-001: Пользователь с таким email уже существует",
                "path": "/api/users/register"
            }
            """;
    private static final String ERROR_409_PASSWORD_MATCH_EXAMPLE = """
            {
                "timestamp": "2023-10-27T10:30:00.123Z",
                "status": 409,
                "error": "Conflict",
                "message": "USR-004: Новый пароль не должен совпадать со старым",
                "path": "/api/users/me/password"
            }
            """;


    @Operation(
            summary = "✨ Регистрация нового пользователя",
            description = """
            Создает новую учетную запись пользователя в системе.
            - По умолчанию всем новым пользователям присваивается роль `USER`.
            - Поле `email` должно быть уникальным и иметь корректный формат.
            - Поле `password` должно соответствовать политикам безопасности (например, не быть пустым).
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = """
                    ✅ **Пользователь успешно создан.**
                    Возвращает данные только что зарегистрированного пользователя.
                    В заголовке `Location` также возвращается URI нового ресурса.
                    """,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AllUserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ **Ошибка валидации.** Неверный формат или отсутствие обязательных полей в запросе (например, пустой пароль, некорректный email).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Пример ошибки валидации", value = ERROR_400_INVALID_DATA_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "❌ **Конфликт данных.** Пользователь с таким `email` уже существует в системе. (Код ошибки: `USR-001`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Пример конфликта email", value = ERROR_409_EMAIL_CONFLICT_EXAMPLE))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<AllUserDTO> registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания нового пользователя. Включает `email` и `password`.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRegistrationRequest.class, example = "{\"email\": \"new.user@example.com\", \"password\": \"strongPassword123\"}"))
            )
            @Valid @RequestBody UserRegistrationRequest request
    ) {
        AppUser newUser = modelMapper.map(request, AppUser.class);
        newUser.setRole(Role.USER);
        AppUser createdUser = userService.registerUser(newUser);
        URI location = URI.create(String.format("/api/users/%s", createdUser.getId()));
        return ResponseEntity.created(location).body(modelMapper.map(createdUser, AllUserDTO.class));
    }

    @Operation(
            summary = "🔑 Аутентификация пользователя (логин)",
            description = """
            Проверяет учетные данные (`email` и `password`).
            В случае успеха возвращает **JSON Web Token (JWT)**, который необходимо использовать для доступа к защищенным эндпоинтам.
            
            Токен следует передавать в заголовке `Authorization` в формате:
            `Authorization: Bearer <ваш_токен>`
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ **Аутентификация успешна.** Возвращает JWT токен в виде строки.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyLCJleHAiOjE2MTYyNDI2MjJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ **Ошибка аутентификации.** Неверный `email` или `password`. (Код ошибки: `AUTH-001`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Учетные данные пользователя для входа в систему.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AuthRequest.class, example = "{\"email\": \"user@example.com\", \"password\": \"password123\"}"))
            )
            @Valid @RequestBody AuthRequest authRequest
    ) {
        String token = userService.authenticateUser(authRequest.getEmail(), authRequest.getPassword());
        return ResponseEntity.ok(token);
    }

    @Operation(
            summary = "👤 Получение информации о текущем пользователе",
            description = """
            Возвращает полную информацию о пользователе, чей JWT токен используется для аутентификации.
            Идеально подходит для заполнения страницы "Мой профиль" в клиентском приложении.
            """,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ **Данные пользователя успешно получены.**",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AppUser.class))),
            @ApiResponse(responseCode = "401", description = "❌ **Не аутентифицирован.** Отсутствует или неверный JWT токен.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE)))
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppUser> getCurrentUser(@AuthenticationPrincipal UserDetail userDetail) {
        return ResponseEntity.ok(modelMapper.map(userDetail.appUser(), AppUser.class));
    }

    @Operation(
            summary = "🔄 Смена пароля текущего пользователя",
            description = """
            Позволяет аутентифицированному пользователю изменить свой пароль.
            - Необходимо предоставить `oldPassword` (текущий пароль) для подтверждения.
            - `newPassword` не должен совпадать со старым.
            - `newPassword` должен соответствовать политикам безопасности.
            """,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "✅ **Пароль успешно изменен.** Тело ответа пустое."),
            @ApiResponse(responseCode = "400", description = "❌ **Неверный запрос.** Либо ошибка валидации нового пароля, либо указан неверный старый пароль (Код ошибки: `USR-003`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = "Неверный старый пароль", value = ERROR_400_WRONG_OLD_PASSWORD_EXAMPLE),
                            @ExampleObject(name = "Ошибка валидации", value = ERROR_400_INVALID_DATA_EXAMPLE)
                    })
            ),
            @ApiResponse(responseCode = "401", description = "❌ **Не аутентифицирован.**",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))),
            @ApiResponse(responseCode = "409", description = "❌ **Конфликт.** Новый пароль совпадает со старым. (Код ошибки: `USR-004`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_409_PASSWORD_MATCH_EXAMPLE)))
    })
    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal UserDetail userDetail,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для смены пароля: старый и новый пароли.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdatePasswordRequest.class))
            )
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        userService.updatePassword(userDetail.appUser().getEmail(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "👑 [ADMIN] Получение списка всех пользователей",
            description = """
            Возвращает полный список всех зарегистрированных пользователей в системе.
            <b>Доступно только пользователям с ролью `ADMIN`</b>.
            
            *Примечание: В реальной системе здесь бы применялась пагинация для обработки большого количества пользователей.*
            """,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ **Список пользователей успешно получен.**",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = AllUserDTO.class)))),
            @ApiResponse(responseCode = "401", description = "❌ **Не аутентифицирован.**",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "❌ **Доступ запрещен.** У вас нет прав `ADMIN`.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_403_FORBIDDEN_EXAMPLE)))
    })
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AllUserDTO>> getAllUsers() {
        List<AllUserDTO> users = userService.getAllUserEmailsAndIds();
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "👑 [ADMIN] Удаление пользователя по ID",
            description = """
            Полностью и безвозвратно удаляет учетную запись пользователя по его ID.
            <b>Действие необратимо! Будьте осторожны.</b>
            <b>Доступно только пользователям с ролью `ADMIN`</b>.
            """,
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "✅ **Пользователь успешно удален.** Ответ не содержит тела, так как ресурс больше не существует."),
            @ApiResponse(responseCode = "401", description = "❌ **Не аутентифицирован.**",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_401_UNAUTHORIZED_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "❌ **Доступ запрещен.**",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_403_FORBIDDEN_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "❌ **Пользователь не найден.** Пользователь с указанным ID не существует. (Код ошибки: `USR-002`).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = ERROR_404_USER_NOT_FOUND_EXAMPLE)))
    })
    @DeleteMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "Уникальный ID пользователя, которого необходимо удалить.", required = true, example = "101")
            @PathVariable Long userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}