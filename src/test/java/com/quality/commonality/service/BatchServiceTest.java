package com.quality.commonality.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.quality.commonality.entity.AiClusterGroup;
import com.quality.commonality.entity.Batch;
import com.quality.commonality.entity.Issue;
import com.quality.commonality.mapper.AiClusterGroupMapper;
import com.quality.commonality.mapper.BatchMapper;
import com.quality.commonality.mapper.IssueMapper;
import com.quality.commonality.mapper.UserMapper;
import com.quality.commonality.service.impl.BatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock
    private BatchMapper batchMapper;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private IssueMapper issueMapper;
    
    @Mock
    private AiClusterGroupMapper clusterGroupMapper;

    // Remove @InjectMocks and instantiate manually to ensure control
    private BatchServiceImpl batchService;

    @BeforeEach
    void setUp() {
        batchService = new BatchServiceImpl();
        // Ensure all dependencies are injected correctly, including baseMapper for ServiceImpl
        ReflectionTestUtils.setField(batchService, "baseMapper", batchMapper);
        ReflectionTestUtils.setField(batchService, "batchMapper", batchMapper);
        ReflectionTestUtils.setField(batchService, "userMapper", userMapper);
        ReflectionTestUtils.setField(batchService, "issueMapper", issueMapper);
        ReflectionTestUtils.setField(batchService, "clusterGroupMapper", clusterGroupMapper);
    }

    @Test
    void getBatchList_shouldDelegteToMapper() {
        when(batchMapper.selectBatchesWithAssignee()).thenReturn(Collections.emptyList());
        assertNotNull(batchService.getBatchList());
        verify(batchMapper).selectBatchesWithAssignee();
    }
    
    @Test
    void uploadAndParseBatch_shouldParseJsonAndSaveData() throws IOException {
        String json = "[{\"type\":\"L1/S1\", \"summary\":\"sum\", \"details\":[{\"PROBLEM_DETAIL\":\"p1\", \"ISSUE_NO\":\"NO1\"}]}]";
        MockMultipartFile file = new MockMultipartFile("file", "test.json", "application/json", json.getBytes(StandardCharsets.UTF_8));
        
        when(batchMapper.insert(any(Batch.class))).thenAnswer(invocation -> {
            Batch b = invocation.getArgument(0);
            b.setId(1L);
            return 1;
        });
        
        when(clusterGroupMapper.insert(any(AiClusterGroup.class))).thenAnswer(invocation -> {
            AiClusterGroup g = invocation.getArgument(0);
            g.setId(10L);
            return 1;
        });
        
        Map<String, Object> result = batchService.uploadAndParseBatch(file, 100L);
        
        assertNotNull(result);
        assertEquals(1L, result.get("batch_id"));
        
        // Verify interactions
        verify(batchMapper).insert(any(Batch.class));
        verify(clusterGroupMapper, times(1)).insert(any(AiClusterGroup.class));
        verify(issueMapper, times(1)).insert(any(Issue.class));
    }
    
    @Test
    void uploadAndParseBatch_shouldHandleAlternativeFormat() throws IOException {
        // Case with "count" instead of "details" and " > " separator
        String json = "[{\"type\":\"L1 > S1\", \"summary\":\"sum\", \"count\": 5}]";
        MockMultipartFile file = new MockMultipartFile("file", "test.json", "application/json", json.getBytes(StandardCharsets.UTF_8));
        
        when(batchMapper.insert(any(Batch.class))).thenAnswer(inv -> {
            Batch b = inv.getArgument(0);
            b.setId(1L);
            return 1;
        });
        
        Map<String, Object> result = batchService.uploadAndParseBatch(file, 100L);
        
        verify(clusterGroupMapper).insert(argThat(group -> 
            group.getCategoryLarge().equals("L1") && 
            group.getCategorySub().equals("S1") &&
            group.getProblemCount() == 5
        ));
        // No issues inserted because details is missing
        verify(issueMapper, never()).insert(any());
    }
    
    @Test
    void uploadAndParseBatch_shouldThrowOnIoException() throws IOException {
        MockMultipartFile file = spy(new MockMultipartFile("file", "test.json", "application/json", "{}".getBytes()));
        doThrow(new IOException("Bad read")).when(file).getBytes();
        
        assertThrows(RuntimeException.class, () -> batchService.uploadAndParseBatch(file, 1L));
    }

    @Test
    void distributeBatch_shouldUpdateAssignee() {
        Batch batch = new Batch();
        batch.setId(1L);
        when(batchMapper.selectById(1L)).thenReturn(batch);
        
        batchService.distributeBatch(1L, 200L);
        
        assertEquals(200L, batch.getAssigneeId());
        assertEquals("ASSIGNED", batch.getStatus());
        verify(batchMapper).updateById(batch);
    }
    
    @Test
    void distributeBatch_shouldThrow_whenBatchNotFound() {
        when(batchMapper.selectById(1L)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> batchService.distributeBatch(1L, 200L));
    }

    @Test
    void getAssignableUsers_shouldQueryUsers() {
        batchService.getAssignableUsers();
        verify(userMapper).selectList(any(QueryWrapper.class));
    }
    
    @Test
    void deleteBatch_shouldThrow_whenBatchNotFound() {
        when(batchMapper.selectById(1L)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> batchService.deleteBatch(1L));
    }
}
