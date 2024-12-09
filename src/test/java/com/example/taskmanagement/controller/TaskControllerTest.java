package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.CreateTaskDTO;
import com.example.taskmanagement.dto.UpdateTaskDTO;
import com.example.taskmanagement.exception.TaskNotFoundException;
import com.example.taskmanagement.model.Priority;
import com.example.taskmanagement.model.Status;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;


    @InjectMocks
    private TaskController taskController;


    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createTask_success() throws Exception {
        CreateTaskDTO taskDTO = new CreateTaskDTO();
        taskDTO.setHead("Test Task");
        taskDTO.setDescription("Test description");
        taskDTO.setStatus(Status.WAITING);
        taskDTO.setPriority(Priority.HIGH);
        taskDTO.setExecutorUsername("testuser");
        taskDTO.setDueDate(LocalDate.now().plusDays(7));

        Task mockTask = new Task();
        mockTask.setId(1L);
        when(taskService.createTask(any(CreateTaskDTO.class))).thenReturn(mockTask);

        ResponseEntity<?> response = taskController.createTask(taskDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(taskService, times(1)).createTask(any(CreateTaskDTO.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createTask_taskServiceThrowsException() throws Exception {
        CreateTaskDTO taskDTO = new CreateTaskDTO();
        taskDTO.setHead("Test Task");
        taskDTO.setDescription("Test description");
        taskDTO.setStatus(Status.WAITING);
        taskDTO.setPriority(Priority.HIGH);
        taskDTO.setExecutorUsername("testuser");
        taskDTO.setDueDate(LocalDate.now().plusDays(7));

        doThrow(new IllegalArgumentException("Invalid data")).when(taskService).createTask(any(CreateTaskDTO.class));

        ResponseEntity<?> response = taskController.createTask(taskDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(taskService, times(1)).createTask(any(CreateTaskDTO.class));
    }



    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void editTask_success() {
        UpdateTaskDTO updateTaskDTO = new UpdateTaskDTO();
        updateTaskDTO.setHead("Updated Task");

        Task mockTask = new Task();
        mockTask.setId(1L);
        when(taskService.editTask(any(UpdateTaskDTO.class), anyLong())).thenReturn(mockTask);

        ResponseEntity<Task> response = taskController.editTask(updateTaskDTO, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(taskService, times(1)).editTask(any(UpdateTaskDTO.class), anyLong());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void editTask_taskNotFound() {
        UpdateTaskDTO updateTaskDTO = new UpdateTaskDTO();
        doThrow(new TaskNotFoundException("Task not found")).when(taskService).editTask(any(UpdateTaskDTO.class), anyLong());
        ResponseEntity<Task> response = taskController.editTask(updateTaskDTO, 1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void deleteTask_success() {
        doNothing().when(taskService).deleteTask(anyLong());
        ResponseEntity<Void> response = taskController.deleteTask(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(taskService, times(1)).deleteTask(anyLong());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void deleteTask_taskNotFound() {
        doThrow(new TaskNotFoundException("Task not found")).when(taskService).deleteTask(anyLong());
        ResponseEntity<Void> response = taskController.deleteTask(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}