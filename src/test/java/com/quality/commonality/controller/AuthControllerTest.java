package com.quality.commonality.controller;

import com.quality.commonality.common.Result;
import com.quality.commonality.dto.LoginRequest;
import com.quality.commonality.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void login_shouldReturnSuccess_whenCredentialsAreValid() throws Exception {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("token", "mock-token");
        
        when(userService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        String loginJson = "{\"username\":\"admin\",\"password\":\"password\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("mock-token"));
    }

    @Test
    void login_shouldReturnError_whenServiceThrowsException() throws Exception {
        when(userService.login(any(LoginRequest.class))).thenThrow(new RuntimeException("Invalid credentials"));

        String loginJson = "{\"username\":\"admin\",\"password\":\"wrong\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk()) // The controller catches exception and returns 200 with error code/msg in body
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Invalid credentials")); // Changed from .msg to .message
    }
}
