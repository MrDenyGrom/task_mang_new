package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.*;
import com.example.taskmanagement.exception.TaskNotFoundException;
import com.example.taskmanagement.exception.UnauthorizedAccessException;
import com.example.taskmanagement.exception.UserAlreadyExistsException;
import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.security.JwtAuthenticationFilter;
import com.example.taskmanagement.security.JwtTokenProvider;
import com.example.taskmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Objects;

/**
 * Контроллер для управления пользователями.
 * Предоставляет REST API endpoints для создания, получения и обновления пользователя.
 */
@RestController
public class UserController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public UserController(JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    /**
     * Регистрация нового пользователя.
     *
     * @param request Данные для регистрации пользователя.
     * @return ResponseEntity с созданным пользователем и статусом CREATED (201) в случае успеха,
     *         или со статусом BAD_REQUEST (400) при ошибке аутентификации,
     *         или со статусом CONFLICT (409), если пользователь с таким email уже существует,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя")
    public ResponseEntity<AppUser> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            AppUser user = new AppUser();
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());

            AppUser createdUser = userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Авторизация пользователя.
     *
     * @param authRequest Данные для авторизации (email и пароль).
     * @param response    HTTP-ответ для установки заголовка авторизации.
     * @return ResponseEntity со статусом OK (200) в случае успеха,
     *         или со статусом BAD_REQUEST (400) при ошибке аутентификации,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody AuthRequest authRequest, HttpServletResponse response) {
        try {
            String token = userService.authenticateUser(authRequest.getEmail(), authRequest.getPassword());
            response.setHeader("Authorization", "Bearer " + token);
            return ResponseEntity.ok().build();
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение информации о текущем пользователе.
     *
     * @param request HTTP-запрос, содержащий токен авторизации.
     * @return ResponseEntity с данными пользователя и статусом OK (200) в случае успеха,
     *         или со статусом UNAUTHORIZED (401) при неверном токене,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @Operation(summary = "Получение информации о текущем пользователе")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppUser> getCurrentUser(HttpServletRequest request) {
        try {
            String token = JwtAuthenticationFilter.getJwtFromRequest(request);
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                throw new UnauthorizedAccessException("Неверный токен.");
            }

            String email = jwtTokenProvider.getEmailFromJWT(token);
            AppUser currentUser = userService.getUserByEmail(email);
            return ResponseEntity.ok(currentUser);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Выход пользователя из системы.
     *
     * @param request HTTP-запрос для получения сессии.
     * @return ResponseEntity со статусом OK (200) в случае успеха,
     *         или со статусом UNAUTHORIZED (401), если сессия не существует,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @Operation(summary = "Выход")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logoutUser(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Обновление пароля текущего пользователя.
     *
     * @param passwordDto DTO, содержащий старый и новый пароли.
     * @return ResponseEntity со статусом OK (200) в случае успеха,
     *         или со статусом CONFLICT (409), если старый и новый пароли совпадают,
     *         или со статусом BAD_REQUEST (400) при неверном старом пароле,
     *         или со статусом FORBIDDEN (403) при ошибке безопасности,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @PutMapping("/updatePassword")
    @Operation(summary = "Обновление пароля пользователя (для текущего пользователя)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> updatePassword(@Valid @RequestBody PasswordUpdateDto passwordDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        try {
            if (Objects.equals(passwordDto.getOldPassword(), passwordDto.getNewPassword())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            userService.updatePassword(currentUsername, passwordDto.getOldPassword(), passwordDto.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение списка email и ID всех пользователей.
     *
     * @return ResponseEntity со списком AllUserDTO и статусом OK (200) в случае успеха,
     *         или со статусом FORBIDDEN (403) при ошибке безопасности,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @GetMapping("/admin/emailsAndIds")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Получение всех пользователей (админ)")
    public ResponseEntity<List<AllUserDTO>> getAllUserEmailsAndIds() {
        try {
            List<AllUserDTO> userDTOs = userService.getAllUserEmailsAndIds();
            return new ResponseEntity<>(userDTOs, HttpStatus.OK);
        }catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    /**
     * Обновление пользователя.
     *
     * @param userId Идентификатор пользователя для обновления.
     * @param updateUserDTO DTO с данными для обновления пользователя.
     * @return ResponseEntity с обновленным Task и статусом OK (200) в случае успеха,
     *         или со статусом NOT_FOUND (404), если пользователь не найден,
     *         или со статусом BAD_REQUEST (400) при некорректных входных данных,
     *         или со статусом FORBIDDEN (403) при ошибке безопасности,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @PutMapping("/admin/update/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Обновить пользователя (админ)")
    public ResponseEntity<AppUser> updateUser(@PathVariable Long userId, @RequestBody UpdateUserDTO updateUserDTO) {
        try {
            AppUser updatedTask = userService.updateUser(userId, updateUserDTO);
            return new ResponseEntity<>(updatedTask, HttpStatus.OK);
        } catch (TaskNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Удаление пользователя.
     *
     * @param userId Идентификатор пользователя для удаления.
     * @return ResponseEntity с удаленным Task и статусом OK (200) в случае успеха,
     *         или со статусом NOT_FOUND (404), если пользователь не найден,
     *         или со статусом FORBIDDEN (403) при ошибке безопасности,
     *         или со статусом UNAUTHORIZED (401) при ошибке аутентификации,
     *         или со статусом INTERNAL_SERVER_ERROR (500) при других ошибках.
     */
    @DeleteMapping("/admin/delete/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Удалить пользователя (админ)")
    public ResponseEntity<AppUser> deleteUser(@PathVariable Long userId) {
        try {
            AppUser deletedTask = userService.deleteUser(userId);
            return new ResponseEntity<>(deletedTask, HttpStatus.OK);
        } catch (TaskNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
