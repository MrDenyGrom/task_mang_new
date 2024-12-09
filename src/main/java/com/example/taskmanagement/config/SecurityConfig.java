package com.example.taskmanagement.config;

import com.example.taskmanagement.security.JwtAuthenticationEntryPoint;
import com.example.taskmanagement.security.JwtAuthenticationFilter;
import com.example.taskmanagement.security.JwtTokenProvider;
import com.example.taskmanagement.service.UserDetailService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Конфигурационный класс для Spring Security.
 * Настраивает безопасность приложения, включая JWT аутентификацию, авторизацию и управление сессиями.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailService userDetailService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, UserDetailService userDetailService,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailService = userDetailService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    /**
     * Создание бина JwtAuthenticationFilter.
     *
     * @return фильтр для аутентификации JWT.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailService);
    }

    /**
     * Настраивание безопасности приложения.
     *
     * @param http объект HttpSecurity для конфигурации.
     * @return цепочка фильтров безопасности.
     * @throws Exception если произошла ошибка во время конфигурации.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/register", "/login", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html/**", "/actuator/health").permitAll()
                        .requestMatchers("/admin/**", "/api/admin/**", "/api/tasks/admin/**", "/api/comments/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        log.info("SecurityFilterChain успешно настроен.");
        return http.build();
    }

    /**
     * Создание провайдера аутентификации.
     *
     * @return провайдер аутентификации.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Создание менеджера аутентификации.
     *
     * @param configuration конфигурация аутентификации.
     * @return менеджер аутентификации.
     * @throws Exception если произошла ошибка во время создания.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Создание кодировщика паролей.
     *
     * @return кодировщик паролей.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Создание объекта ModelMapper.
     *
     * @return объект ModelMapper для маппинга (конвертации) объектов.
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}