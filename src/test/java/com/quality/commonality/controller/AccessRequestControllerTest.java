package com.quality.commonality.controller;

import com.quality.commonality.service.AccessRequestService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccessRequestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccessRequestService accessRequestService;

    @InjectMocks
    private AccessRequestController accessRequestController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accessRequestController).build();
    }

    @Test
    void apply_shouldCallService() throws Exception {
        String json = "{\"userId\":1, \"adminId\":2, \"reason\":\"Please\"}";
        
        mockMvc.perform(post("/api/access/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        verify(accessRequestService).apply(1L, 2L, "Please");
    }

    @Test
    void listForAdmin_shouldReturnList() throws Exception {
        when(accessRequestService.listByAdmin(anyLong())).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/access/admin/1"))
                .andExpect(status().isOk());
        
        verify(accessRequestService).listByAdmin(1L);
    }

    @Test
    void approve_shouldCallService() throws Exception {
        mockMvc.perform(post("/api/access/1/approve"))
                .andExpect(status().isOk());
        
        verify(accessRequestService).approve(1L);
    }

    @Test
    void reject_shouldCallService() throws Exception {
        mockMvc.perform(post("/api/access/1/reject"))
                .andExpect(status().isOk());
        
        verify(accessRequestService).reject(1L);
    }
}

