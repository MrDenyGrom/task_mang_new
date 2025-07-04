package com.example.taskmanagement.config;

import com.example.taskmanagement.dto.CommentDTO;
import com.example.taskmanagement.model.Comment;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p><b>Центральная Конфигурация Приложения</b></p>
 *
 * <p>
 *     Предоставляет фундаментальные бины, используемые во всем приложении.
 *     Этот класс служит для определения общих компонентов, не принадлежащих
 *     к узкоспециализированным конфигурациям, таким как безопасность или веб-слой.
 * </p>
 *
 * @see org.springframework.context.annotation.Configuration
 */
@Configuration
public class ApplicationConfig {

    /**
     * <p><b>Бин для Маппинга Объектов</b></p>
     *
     * <p>
     *     Создает и настраивает синглтон-бин {@link ModelMapper}.
     * </p>
     *
     * <blockquote>
     *     <b>Назначение:</b> Автоматизация процесса преобразования между
     *     объектами передачи данных (DTO) и сущностями базы данных (Entities).
     *     Это устраняет необходимость в ручном написании шаблонного кода для маппинга полей.
     * </blockquote>
     *
     * @return Полностью сконфигурированный экземпляр {@link ModelMapper}.
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.typeMap(Comment.class, CommentDTO.class).addMappings(mapper -> {
            mapper.map(src -> src.getTask().getId(), CommentDTO::setTaskId);
            mapper.map(src -> src.getAppUser().getEmail(), CommentDTO::setAuthor);
        });

        return modelMapper;
    }
}