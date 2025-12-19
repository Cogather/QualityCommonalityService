package com.quality.commonality.controller;

import com.quality.commonality.service.BatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BatchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BatchService batchService;

    @InjectMocks
    private BatchController batchController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(batchController).build();
    }

    @Test
    void list_shouldReturnBatches() throws Exception {
        when(batchService.getBatchList()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/batches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void upload_shouldHandleJsonFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "data.json", "application/json", "{}".getBytes());
        when(batchService.uploadAndParseBatch(any(), any())).thenReturn(Collections.emptyMap());

        mockMvc.perform(multipart("/api/batches/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void upload_shouldRejectNonJson() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "data.txt", "text/plain", "{}".getBytes());
        
        mockMvc.perform(multipart("/api/batches/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("只支持JSON文件")); // Changed from .msg to .message
    }
    
    @Test
    void upload_shouldHandleEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "data.json", "application/json", new byte[0]);
        
        mockMvc.perform(multipart("/api/batches/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void distribute_shouldCallService() throws Exception {
        String json = "{\"userId\": 123}";
        
        mockMvc.perform(post("/api/batches/1/distribute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        verify(batchService).distributeBatch(1L, 123L);
    }

    @Test
    void getAssignableUsers_shouldReturnUsers() throws Exception {
        when(batchService.getAssignableUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/batches/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void deleteBatch_shouldCallService() throws Exception {
        mockMvc.perform(delete("/api/batches/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        verify(batchService).deleteBatch(1L);
    }
    
    @Test
    void deleteBatch_shouldHandleException() throws Exception {
        doThrow(new RuntimeException("Delete failed")).when(batchService).deleteBatch(1L);
        
        mockMvc.perform(delete("/api/batches/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("删除失败: Delete failed")); // Changed from .msg to .message
    }
}
