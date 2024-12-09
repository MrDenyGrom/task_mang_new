package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.AllUserDTO;
import com.example.taskmanagement.dto.UpdateUserDTO;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.exception.UserAlreadyExistsException;
import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Находит пользователя по email.
     * @param email Email пользователя.
     * @return Optional, содержащий пользователя, если он найден.
     */
    public Optional<AppUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Обновляет пароль пользователя.
     * @param email Email пользователя.
     * @param oldPassword Старый пароль.
     * @param newPassword Новый пароль.
     * @throws ResourceNotFoundException Если пользователь с таким email не найден.
     * @throws IllegalArgumentException Если старый пароль неверный.
     */
    public void updatePassword(String email, String oldPassword, String newPassword) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с таким email не найден."));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Неверный старый пароль");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Пароль пользователя {} успешно обновлен", email);
    }

    /**
     * Аутентифицирует пользователя.
     * @param email Email пользователя.
     * @param password Пароль пользователя.
     * @return JWT токен.
     * @throws IllegalArgumentException Если аутентификация не удалась.
     */
    public String authenticateUser(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.generateToken(email);
            log.info("Пользователь {} успешно аутентифицирован", email);
            return token;
        } catch (AuthenticationException e) {
            log.warn("Ошибка аутентификации пользователя {}: {}", email, e.getMessage());
            throw new IllegalArgumentException("Неверный email или пароль");
        }
    }

    /**
     * Регистрирует нового пользователя.
     * @param appUser Пользователь для регистрации.
     * @return Зарегистрированный пользователь.
     * @throws UserAlreadyExistsException Если пользователь с таким email уже существует.
     */
    public AppUser registerUser(AppUser appUser) {
        if (userRepository.existsByEmail(appUser.getEmail())) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует.");
        }
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        AppUser savedUser = userRepository.save(appUser);
        log.info("Пользователь {} успешно зарегистрирован", appUser.getEmail());
        return savedUser;
    }

    /**
     * Возвращает пользователя по email.
     * @param email Email пользователя.
     * @return Пользователь.
     * @throws ResourceNotFoundException Если пользователь с таким email не найден.
     */
    public AppUser getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с таким email не найден."));
    }

    /**
     * Возвращает список всех пользователей с их email и id.
     * @return Список DTO пользователей.
     */
    public List<AllUserDTO> getAllUserEmailsAndIds() {
        return userRepository.findAll().stream()
                .map(user -> new AllUserDTO(user.getId(), user.getEmail(), user.getRole()))
                .collect(Collectors.toList());
    }

    /**
     * Обновляет данные пользователя.
     * @param userId ID пользователя.
     * @param updateUserDTO DTO с данными для обновления.
     * @return Обновленный пользователь.
     * @throws ResourceNotFoundException Если пользователь с таким ID не найден.
     */
    public AppUser updateUser(Long userId, UpdateUserDTO updateUserDTO) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с таким id не найден. " + userId));
        user.setRole(updateUserDTO.getRole());
        return userRepository.save(user);
    }

    /**
     * Удаляет пользователя.
     * @param userId ID пользователя.
     * @return Удаленный пользователь.
     * @throws ResourceNotFoundException Если пользователь с таким ID не найден.
     */
    public AppUser deleteUser(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с таким id не найден. " + userId));
        userRepository.delete(user);
        return user;
    }
}