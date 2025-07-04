package com.example.taskmanagement.config;

import com.example.taskmanagement.model.Role;
import com.example.taskmanagement.security.JwtAuthenticationEntryPoint;
import com.example.taskmanagement.security.JwtAuthenticationFilter;
import com.example.taskmanagement.service.UserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * <p><b>Центральная Конфигурация Безопасности</b></p>
 *
 * <p>
 *     Определяет всю логику аутентификации и авторизации в приложении.
 *     Активирует веб-безопасность Spring и включает поддержку аннотаций
 *     для защиты методов на уровне сервисов.
 * </p>
 *
 * <p><b>Ключевые аннотации:</b></p>
 * <ul>
 *     <li>{@link EnableWebSecurity}: Включает интеграцию Spring Security с Spring MVC.</li>
 *     <li>{@link EnableMethodSecurity}: Разрешает использование аннотаций
 *     {@code @PreAuthorize} и {@code @PostAuthorize} для гранулярного контроля доступа.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailService userDetailService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private static final String[] PUBLIC_URLS = {
            "/api/users/register",
            "/api/users/login",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/**"
    };

    /**
     * <p><b>Основная Цепочка Фильтров Безопасности</b></p>
     *
     * <p>
     *     Определяет центральную конфигурацию правил безопасности для всех HTTP-запросов.
     * </p>
     *
     * <p><b>Ключевые аспекты конфигурации:</b></p>
     * <ul>
     *     <li><b>CSRF:</b> Отключен ({@code csrf(AbstractHttpConfigurer::disable)}),
     *     что является стандартной практикой для stateless REST API.</li>
     *
     *     <li><b>Управление сессиями:</b> Установлено в {@link SessionCreationPolicy#STATELESS}.
     *     Сервер не создает и не использует HTTP-сессии.</li>
     *
     *     <li><b>Обработка исключений:</b> Настроен кастомный {@link JwtAuthenticationEntryPoint}
     *     для корректной обработки ошибок аутентификации (401 Unauthorized).</li>
     *
     *     <li><b>Авторизация запросов:</b>
     *         <ul>
     *             <li>Публичный доступ к эндпоинтам из списка {@code PUBLIC_URLS}.</li>
     *             <li>Доступ к {@code /admin/**} требует роли "ADMIN".</li>
     *             <li>Все остальные запросы требуют аутентификации.</li>
     *         </ul>
     *     </li>
     *
     *     <li><b>JWT Фильтр:</b> {@link JwtAuthenticationFilter} интегрирован в цепочку
     *     для валидации токена на каждом защищенном запросе.</li>
     * </ul>
     *
     * @param http Конструктор для настройки веб-безопасности.
     * @return Сконфигурированный и готовый к работе {@link SecurityFilterChain}.
     * @throws Exception при возникновении ошибок в процессе конфигурации.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, @Autowired JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> {
                    authorizeRequests
                            .requestMatchers(PUBLIC_URLS)
                            .permitAll()
                            .requestMatchers("/admin/**")
                            .hasRole(String.valueOf(Role.ADMIN))
                            .anyRequest().authenticated();
                })
                .exceptionHandling(exceptionHandling -> {
                    exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint);
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * <p><b>Провайдер Аутентификации</b></p>
     *
     * <p>
     *     Определяет стратегию проверки учетных данных пользователя. Используется
     *     {@link DaoAuthenticationProvider}, который интегрируется с:
     * </p>
     * <ul>
     *      <li>{@link UserDetailService} для загрузки данных пользователя по его имени.</li>
     *      <li>{@link PasswordEncoder} для безопасного сравнения предоставленного пароля с хешем в базе.</li>
     * </ul>
     *
     * @return Настроенный провайдер аутентификации.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * <p><b>Менеджер Аутентификации</b></p>
     *
     * <p>
     *     Предоставляет {@link AuthenticationManager} как бин в контексте Spring.
     *     Это центральный компонент, который делегирует процесс аутентификации
     *     настроенным провайдерам.
     * </p>
     *
     * @param config Конфигурация Spring, из которой извлекается менеджер.
     * @return {@link AuthenticationManager}.
     * @throws Exception в случае ошибок.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * <p><b>Кодировщик Паролей</b></p>
     *
     * <p>
     *     Определяет алгоритм для хеширования паролей. Использование {@link BCryptPasswordEncoder}
     *     является отраслевым стандартом для надежного и безопасного хранения паролей.
     * </p>
     *
     * @return Бин {@link PasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}