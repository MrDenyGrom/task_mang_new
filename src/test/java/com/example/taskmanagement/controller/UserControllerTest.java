package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.*;
import com.example.taskmanagement.exception.UserAlreadyExistsException;
import com.example.taskmanagement.model.AppUser;
import com.example.taskmanagement.security.JwtTokenProvider;
import com.example.taskmanagement.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void registerUser_success() {
        UserRegistrationRequest request = new UserRegistrationRequest("test@example.com", "password");
        AppUser user = new AppUser();
        user.setEmail("test@example.com");
        user.setPassword("password");
        when(userService.registerUser(any(AppUser.class))).thenReturn(user);

        ResponseEntity<AppUser> response = userController.registerUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    void registerUser_userAlreadyExistsException() {
        UserRegistrationRequest request = new UserRegistrationRequest("test@example.com", "password");
        doThrow(UserAlreadyExistsException.class).when(userService).registerUser(any(AppUser.class));

        ResponseEntity<AppUser> response = userController.registerUser(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void registerUser_authenticationException() {
        UserRegistrationRequest request = new UserRegistrationRequest("test@example.com", "password");
        doThrow(BadCredentialsException.class).when(userService).registerUser(any(AppUser.class));

        ResponseEntity<AppUser> response = userController.registerUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    void login_success() {
        AuthRequest authRequest = new AuthRequest("test@example.com", "password");
        when(userService.authenticateUser("test@example.com", "password")).thenReturn("testToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseEntity<Void> result = userController.login(authRequest, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Bearer testToken", response.getHeader("Authorization"));
    }

    @Test
    void login_authenticationException() {
        AuthRequest authRequest = new AuthRequest("test@example.com", "password");
        doThrow(BadCredentialsException.class).when(userService).authenticateUser(anyString(), anyString());
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseEntity<Void> result = userController.login(authRequest, response);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void getCurrentUser_success() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer testToken");
        when(jwtTokenProvider.validateToken("testToken")).thenReturn(true);
        when(jwtTokenProvider.getEmailFromJWT("testToken")).thenReturn("test@example.com");
        AppUser user = new AppUser();
        user.setEmail("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);

        ResponseEntity<AppUser> response = userController.getCurrentUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    void getCurrentUser_invalidToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer testToken");
        when(jwtTokenProvider.validateToken("testToken")).thenReturn(false);

        ResponseEntity<AppUser> response = userController.getCurrentUser(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void updatePassword_success() {
        PasswordUpdateDto passwordDto = new PasswordUpdateDto("oldPassword", "newPassword");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResponseEntity<String> response = userController.updatePassword(passwordDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updatePassword_passwordsMatch() {
        PasswordUpdateDto passwordDto = new PasswordUpdateDto("password", "password");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResponseEntity<String> response = userController.updatePassword(passwordDto);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }


}