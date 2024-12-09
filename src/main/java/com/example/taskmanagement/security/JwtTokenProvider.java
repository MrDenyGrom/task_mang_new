package com.example.taskmanagement.security;

import io.jsonwebtoken.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Поставщик токенов JWT.
 * Этот класс отвечает за генерацию, проверку и извлечение информации из токенов JWT.
 */
@SuppressWarnings("deprecation")
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Время жизни JWT токена в миллисекундах.
     */
    @Getter
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * Генерирует JWT токен для заданного адреса электронной почты.
     *
     * @param email Адрес электронной почты пользователя.
     * @return Сгенерированный JWT токен.
     */
    public String generateToken(@NotNull String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();

        log.info("Сгенерирован JWT токен для пользователя {}", email);
        return token;
    }

    /**
     * Извлекает адрес электронной почты из JWT токена.
     *
     * @param token JWT токен.
     * @return Адрес электронной почты, извлеченный из токена.
     * @throws JwtException Если токен недействителен.
     */
    public String getEmailFromJWT(@NotNull String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = claims.getSubject();
            log.debug("Получен email из JWT токена: {}", email);
            return email;
        } catch (JwtException e) {
            log.error("Ошибка при извлечении email из JWT: {}", e.getMessage());
            throw new JwtException("Невалидный JWT токен");
        }
    }

    /**
     * Проверяет действительность JWT токена.
     *
     * @param token JWT токен.
     * @return true, если токен действителен, иначе false.
     */
    public boolean validateToken(@NotNull String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(token);
            log.debug("JWT токен валиден");
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT токен просрочен: {}", ex.getMessage());
        } catch (JwtException ex) {
            log.error("Ошибка валидации JWT токена: {}", ex.getMessage());
        }
        return false;
    }
}