package com.quality.commonality.controller;

import com.quality.commonality.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
    }

    @Test
    void getStats_shouldReturnData() throws Exception {
        when(dashboardService.getAdminStats()).thenReturn(Collections.emptyMap());
        
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        verify(dashboardService).getAdminStats();
    }

    @Test
    void getTopErrors_shouldReturnData() throws Exception {
        when(dashboardService.getTopErrors()).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/dashboard/chart/top-errors"))
                .andExpect(status().isOk());
        
        verify(dashboardService).getTopErrors();
    }

    @Test
    void getCategoryDistribution_shouldReturnData() throws Exception {
        when(dashboardService.getCategoryDistribution()).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/dashboard/chart/category-dist"))
                .andExpect(status().isOk());
        
        verify(dashboardService).getCategoryDistribution();
    }
}

