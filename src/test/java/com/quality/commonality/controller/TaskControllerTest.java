package com.quality.commonality.controller;

import com.quality.commonality.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
    }

    @Test
    void getMyTasks_shouldReturnList() throws Exception {
        when(taskService.getMyTasks(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/tasks")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        verify(taskService).getMyTasks(1L);
    }

    @Test
    void getTaskDetails_shouldReturnDetails() throws Exception {
        when(taskService.getTaskDetails(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/tasks/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        verify(taskService).getTaskDetails(100L);
    }

    @Test
    void verifyItem_shouldCallService() throws Exception {
        mockMvc.perform(post("/api/tasks/items/10/verify")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        verify(taskService).verifyIssue(10L, 1L);
    }

    @Test
    void correctItem_shouldCallService() throws Exception {
        String json = "{\"categoryLarge\":\"L\",\"categorySub\":\"S\"}";
        
        mockMvc.perform(post("/api/tasks/items/10/correct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        verify(taskService).correctIssue(eq(10L), any(Map.class), eq(1L));
    }
}

