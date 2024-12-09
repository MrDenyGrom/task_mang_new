package com.example.taskmanagement.repository;

import com.example.taskmanagement.model.AppUser;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с пользователями.
 */
public interface UserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Находит пользователя по email.
     * @param email Email пользователя.
     * @return Optional объект, содержащий пользователя, если он найден.
     */
    Optional<AppUser> findByEmail(@NotNull String email);

    /**
     * Проверяет, существует ли пользователь с заданным email.
     * @param email Email пользователя.
     * @return true, если пользователь существует, иначе false.
     */
    boolean existsByEmail(@NotNull String email);
}