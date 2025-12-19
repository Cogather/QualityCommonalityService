package com.quality.commonality.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.quality.commonality.mapper.BatchMapper;
import com.quality.commonality.mapper.IssueMapper;
import com.quality.commonality.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private IssueMapper issueMapper;

    @Mock
    private BatchMapper batchMapper;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getAdminStats_shouldReturnAggregatedStats() {
        // Combine all selectCount calls into one sequential stub
        // Sequence in code:
        // 1. issueMapper.selectCount(null) -> Total
        // 2. batchMapper.selectCount(...) -> Pending Batches (handled by separate mock)
        // 3. issueMapper.selectCount(verifiedQw) -> Verified
        // 4. issueMapper.selectCount(correctedQw) -> Corrected
        // 5. issueMapper.selectCount(processedQw) -> Processed All
        
        when(issueMapper.selectCount(any()))
            .thenReturn(100L) // 1. Total
            .thenReturn(40L)  // 3. Verified
            .thenReturn(10L)  // 4. Corrected
            .thenReturn(50L); // 5. Processed All
            
        when(batchMapper.selectCount(any())).thenReturn(5L);

        Map<String, Object> stats = dashboardService.getAdminStats();

        assertEquals(100L, stats.get("totalPredictions"));
        assertEquals(5L, stats.get("pendingBatches"));
        assertEquals("80.0", stats.get("accuracy")); // 40 / 50 * 100
        assertEquals(50, stats.get("correctionProgress")); // 50 / 100 * 100
    }

    @Test
    void getTopErrors_shouldDelegate() {
        when(issueMapper.selectTopErrors()).thenReturn(Collections.emptyList());
        assertNotNull(dashboardService.getTopErrors());
    }

    @Test
    void getCategoryDistribution_shouldDelegate() {
        when(issueMapper.selectCategoryStats()).thenReturn(Collections.emptyList());
        assertNotNull(dashboardService.getCategoryDistribution());
    }
}
