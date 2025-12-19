package com.quality.commonality.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.quality.commonality.entity.AiClusterGroup;
import com.quality.commonality.entity.Batch;
import com.quality.commonality.entity.Issue;
import com.quality.commonality.mapper.AiClusterGroupMapper;
import com.quality.commonality.mapper.BatchMapper;
import com.quality.commonality.mapper.IssueMapper;
import com.quality.commonality.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private BatchMapper batchMapper;

    @Mock
    private IssueMapper issueMapper;
    
    @Mock
    private AiClusterGroupMapper clusterGroupMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(taskService, "baseMapper", issueMapper);
    }

    @Test
    void getMyTasks_shouldReturnBatchesWithStats() {
        Batch batch = new Batch();
        batch.setId(1L);
        batch.setBatchUid("B001");
        batch.setTotalCount(10);
        
        when(batchMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.singletonList(batch));
        when(issueMapper.selectCount(any(QueryWrapper.class))).thenReturn(5L); // 5 processed

        List<Map<String, Object>> result = taskService.getMyTasks(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("B001", result.get(0).get("batchId"));
        assertEquals(5L, result.get(0).get("processedCount"));
    }

    @Test
    void getTaskDetails_shouldReturnGroupedIssues() {
        Long batchId = 1L;
        
        AiClusterGroup group = new AiClusterGroup();
        group.setId(10L);
        group.setCategoryLarge("L1");
        group.setCategorySub("S1");
        
        Issue issue = new Issue();
        issue.setId(100L);
        issue.setClusterId(10L);
        issue.setProblemDetail("problem");
        
        when(clusterGroupMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.singletonList(group));
        when(issueMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.singletonList(issue));

        List<Map<String, Object>> result = taskService.getTaskDetails(batchId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).get("clusterId"));
        
        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get(0).get("issues");
        assertEquals(1, issues.size());
        assertEquals(100L, issues.get(0).get("id"));
    }
    
    @Test
    void verifyIssue_shouldUpdateStatusAndCheckCompletion() {
        Long issueId = 100L;
        Long operatorId = 200L;
        Long batchId = 1L;
        
        Issue issue = new Issue();
        issue.setId(issueId);
        issue.setBatchId(batchId);
        issue.setClusterId(10L);
        issue.setStatus("PENDING");
        
        AiClusterGroup group = new AiClusterGroup();
        group.setId(10L);
        group.setCategoryLarge("L1");
        group.setCategorySub("S1");
        
        when(issueMapper.selectById(issueId)).thenReturn(issue);
        when(clusterGroupMapper.selectById(10L)).thenReturn(group);
        when(issueMapper.updateById(issue)).thenReturn(1);
        
        // Mock checkBatchCompletion logic
        // Case 1: still pending items
        when(issueMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L); 
        
        taskService.verifyIssue(issueId, operatorId);
        
        assertEquals("VERIFIED", issue.getStatus());
        assertEquals(operatorId, issue.getOperatorId());
        assertEquals("L1", issue.getHumanCategoryLarge());
        verify(issueMapper).updateById(issue);
        // Batch not updated because count > 0
        verify(batchMapper, never()).updateById(any());
    }
    
    @Test
    void verifyIssue_shouldCompleteBatch_whenNoPendingIssues() {
        Long issueId = 100L;
        Long batchId = 1L;
        
        Issue issue = new Issue();
        issue.setId(issueId);
        issue.setBatchId(batchId);
        issue.setClusterId(10L);
        
        AiClusterGroup group = new AiClusterGroup();
        group.setId(10L);
        
        Batch batch = new Batch();
        batch.setId(batchId);
        batch.setStatus("ASSIGNED");
        
        when(issueMapper.selectById(issueId)).thenReturn(issue);
        when(clusterGroupMapper.selectById(10L)).thenReturn(group);
        when(issueMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L); // No pending
        when(batchMapper.selectById(batchId)).thenReturn(batch);
        
        taskService.verifyIssue(issueId, 1L);
        
        verify(batchMapper).updateById(batch);
        assertEquals("COMPLETED", batch.getStatus());
    }
    
    @Test
    void verifyIssue_shouldThrow_whenIssueNotFound() {
        when(issueMapper.selectById(anyLong())).thenReturn(null);
        assertThrows(RuntimeException.class, () -> taskService.verifyIssue(1L, 1L));
    }
    
    @Test
    void verifyIssue_shouldThrow_whenClusterNotFound() {
        Issue issue = new Issue();
        issue.setClusterId(999L);
        when(issueMapper.selectById(1L)).thenReturn(issue);
        when(clusterGroupMapper.selectById(999L)).thenReturn(null);
        
        assertThrows(RuntimeException.class, () -> taskService.verifyIssue(1L, 1L));
    }

    @Test
    void correctIssue_shouldUpdateStatusAndDetails() {
        Long issueId = 100L;
        Long operatorId = 200L;
        Map<String, String> data = new HashMap<>();
        data.put("categoryLarge", "NewL");
        data.put("categorySub", "NewS");
        data.put("reason", "Bad AI");
        
        Issue issue = new Issue();
        issue.setId(issueId);
        issue.setBatchId(1L);
        
        when(issueMapper.selectById(issueId)).thenReturn(issue);
        when(issueMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L); // still pending
        
        taskService.correctIssue(issueId, data, operatorId);
        
        assertEquals("CORRECTED", issue.getStatus());
        assertEquals("NewL", issue.getHumanCategoryLarge());
        assertEquals("NewS", issue.getHumanCategorySub());
        assertEquals("Bad AI", issue.getReason());
        assertEquals(operatorId, issue.getOperatorId());
        
        verify(issueMapper).updateById(issue);
    }
    
    @Test
    void correctIssue_shouldThrow_whenIssueNotFound() {
        when(issueMapper.selectById(anyLong())).thenReturn(null);
        assertThrows(RuntimeException.class, () -> taskService.correctIssue(1L, Collections.emptyMap(), 1L));
    }
}
