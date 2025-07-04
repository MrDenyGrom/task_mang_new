package com.example.taskmanagement.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * <p><b>Провайдер JWT Токенов</b></p>
 *
 * <p>
 *     Центральный компонент, отвечающий за все операции, связанные с
 *     жизненным циклом JSON Web Tokens (JWT):
 * </p>
 * <ul>
 *     <li><b>Генерация:</b> Создание новых токенов для аутентифицированных пользователей.</li>
 *     <li><b>Парсинг:</b> Извлечение данных (claims), таких как email, из строки токена.</li>
 *     <li><b>Валидация:</b> Проверка подлинности и срока действия токена.</li>
 * </ul>
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecretString;

    @Getter
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private SecretKey jwtSecretKey;

    /**
     * <p><b>Инициализация Секретного Ключа</b></p>
     * <p>
     *     Метод, который вызывается после внедрения зависимостей. Он преобразует
     *     строковый секрет из конфигурации в криптографически безопасный
     *     объект {@link SecretKey} для подписи и проверки токенов.
     * </p>
     */
    @PostConstruct
    protected void init() {
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecretString.getBytes());
    }

    /**
     * <p><b>Генерация Токена</b></p>
     *
     * @param email Уникальный идентификатор пользователя (email), который будет закодирован в токене.
     * @return Сгенерированный и подписанный JWT в виде строки.
     */
    public String generateToken(@NotNull String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * <p><b>Извлечение Email из Токена</b></p>
     *
     * @param token JWT в виде строки.
     * @return Email пользователя (subject из claims).
     */
    public String getEmailFromJWT(@NotNull String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * <p><b>Валидация Токена</b></p>
     * <p>
     *     Проверяет, является ли токен валидным: корректно ли он подписан
     *     и не истек ли его срок действия.
     * </p>
     *
     * @param token JWT в виде строки.
     * @return {@code true}, если токен валиден, иначе {@code false}.
     */
    public boolean validateToken(@NotNull String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Некорректная структура JWT токена: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.warn("Срок действия JWT токена истек: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Неподдерживаемый формат JWT токена: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("Пустая или некорректная строка JWT токена: {}", ex.getMessage());
        }
        return false;
    }
}