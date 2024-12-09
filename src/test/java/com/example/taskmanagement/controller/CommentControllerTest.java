package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.CommentDTO;
import com.example.taskmanagement.exception.CommentNotFoundException;
import com.example.taskmanagement.exception.TaskNotFoundException;
import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.model.Comment;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.service.CommentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тестовый класс для {@link CommentController}.
 */
@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    /** Мокированный сервис для работы с комментариями. */
    @Mock
    private CommentService commentService;
    /** Мокированный репозиторий для работы с задачами. */
    @Mock
    private TaskRepository taskRepository;
    /** Мокированный репозиторий для работы с пользователями. */
    @Mock
    private UserRepository userRepository;
    /** Инстанс контроллера комментариев, использующий мокированные зависимости. */
    @InjectMocks
    private CommentController commentController;

    /**
     * Тест успешного создания комментария.
     */
    @Test
    void createComment_success() {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setText("Test comment");
        Authentication authentication = mock(Authentication.class);

        Comment mockComment = new Comment();
        mockComment.setId(1L);
        mockComment.setText("Test comment");


        when(commentService.createComment(1L, commentDTO, authentication)).thenReturn(mockComment);

        ResponseEntity<?> response = commentController.createComment(1L, commentDTO, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(mockComment, response.getBody());

        verify(commentService, times(1)).createComment(1L, commentDTO, authentication);
    }

    /**
     * Тест создания комментария, когда задача не найдена.
     */
    @Test
    void createComment_taskNotFound() {
        CommentDTO commentDTO = new CommentDTO();
        Authentication authentication = mock(Authentication.class);
        when(commentService.createComment(1L, commentDTO, authentication)).thenThrow(TaskNotFoundException.class);
        ResponseEntity<?> response = commentController.createComment(1L, commentDTO, authentication);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Тест успешного получения всех комментариев для задачи.
     */
    @Test
    void getAllCommentsForTask_success() {
        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment());
        when(commentService.getCommentsByTaskId(1L)).thenReturn(comments);
        ResponseEntity<?> response = commentController.getAllCommentsForTask(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    /**
     * Тест успешного удаления комментария.
     */
    @Test
    void deleteComment_success() {
        ResponseEntity<?> response = commentController.deleteComment(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(commentService, times(1)).deleteComment(1L);
    }

    /**
     * Тест удаления комментария, когда комментарий не найден.
     */
    @Test
    void deleteComment_commentNotFound() {
        doThrow(CommentNotFoundException.class).when(commentService).deleteComment(1L);
        ResponseEntity<?> response = commentController.deleteComment(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Тест успешного обновления комментария.
     */
    @Test
    void updateComment_success() {
        CommentDTO commentDTO = new CommentDTO();
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser@example.com");
        Comment comment = new Comment();
        comment.setId(1L);
        when(commentService.getCommentById(1L)).thenReturn(comment);
        when(commentService.isCommentAuthor(comment, "testuser@example.com")).thenReturn(true);
        when(commentService.updateComment(1L, commentDTO)).thenReturn(comment);
        ResponseEntity<?> response = commentController.updateComment(1L, commentDTO, authentication);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    /**
     * Тест обновления комментария, когда комментарий не найден.
     */
    @Test
    void updateComment_commentNotFound() {
        CommentDTO commentDTO = new CommentDTO();
        Authentication authentication = mock(Authentication.class);
        when(commentService.getCommentById(1L)).thenThrow(CommentNotFoundException.class);
        ResponseEntity<?> response = commentController.updateComment(1L, commentDTO, authentication);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Тест обновления комментария, когда пользователь не является автором.
     */
    @Test
    void updateComment_forbidden() {
        CommentDTO commentDTO = new CommentDTO();
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser@example.com");
        Comment comment = new Comment();
        comment.setId(1L);
        when(commentService.getCommentById(1L)).thenReturn(comment);
        when(commentService.isCommentAuthor(comment, "testuser@example.com")).thenReturn(false);
        ResponseEntity<?> response = commentController.updateComment(1L, commentDTO, authentication);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }


    /**
     * Тест успешного получения комментариев пользователя.
     */
    @Test
    void getMyComments_success() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser@example.com");
        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment());
        when(commentService.getCommentsByEmail("testuser@example.com")).thenReturn(comments);
        ResponseEntity<?> response = commentController.getMyComments(authentication);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    /**
     * Тест успешного поиска комментариев.
     */
    @Test
    void searchComments_success() {
        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment());
        when(commentService.searchComments("keyword")).thenReturn(comments);
        ResponseEntity<List<Comment>> response = commentController.searchComments("keyword");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }


    /**
     * Тест успешного подсчета комментариев для задачи.
     */
    @Test
    void countCommentsByTask_success() {
        Task task = new Task();
        task.setId(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(commentService.countCommentsByTask(task)).thenReturn(5L);
        ResponseEntity<Long> response = commentController.countCommentsByTask(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5L, response.getBody());
    }

    /**
     * Тест подсчета комментариев для задачи, когда задача не найдена.
     */
    @Test
    void countCommentsByTask_taskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        ResponseEntity<Long> response = commentController.countCommentsByTask(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Тест успешного подсчета комментариев пользователя.
     */
    @Test
    void countMyComments_success() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser@example.com");
        AppUser user = new AppUser();
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(user));
        when(commentService.countCommentsByUser(user)).thenReturn(10L);
        ResponseEntity<Long> response = commentController.countMyComments(authentication);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody());
    }

    /**
     * Тест подсчета комментариев пользователя, когда пользователь не найден.
     */
    @Test
    void countMyComments_userNotFound() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser@example.com");
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.empty());
        ResponseEntity<Long> response = commentController.countMyComments(authentication);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}