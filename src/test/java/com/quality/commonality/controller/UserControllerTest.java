package com.quality.commonality.controller;

import com.quality.commonality.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void getAdmins_shouldReturnList() throws Exception {
        when(userService.getAdmins()).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/users/admins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        verify(userService).getAdmins();
    }

    @Test
    void listUsers_shouldReturnList() throws Exception {
        when(userService.listUsers(anyString())).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/users?role=USER"))
                .andExpect(status().isOk());
        
        verify(userService).listUsers("USER");
    }

    @Test
    void approveUser_shouldCallService() throws Exception {
        mockMvc.perform(post("/api/users/1/approve"))
                .andExpect(status().isOk());
        
        verify(userService).approveUser(1L);
    }

    @Test
    void revokeUser_shouldCallService() throws Exception {
        mockMvc.perform(post("/api/users/1/revoke"))
                .andExpect(status().isOk());
        
        verify(userService).revokeUser(1L);
    }
}

