package com.quality.commonality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quality.commonality.entity.Batch;
import com.quality.commonality.entity.Issue;
import com.quality.commonality.mapper.BatchMapper;
import com.quality.commonality.mapper.IssueMapper;
import com.quality.commonality.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl extends ServiceImpl<IssueMapper, Issue> implements TaskService {

    @Autowired
    private BatchMapper batchMapper;
    
    @Autowired
    private IssueMapper issueMapper;

    @Override
    public List<Map<String, Object>> getMyTasks(Long userId) {
        // Find assigned batches
        QueryWrapper<Batch> qw = new QueryWrapper<>();
        qw.eq("assignee_id", userId);
        qw.orderByDesc("created_at");
        List<Batch> batches = batchMapper.selectList(qw);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Batch b : batches) {
            Map<String, Object> map = new HashMap<>();
            map.put("batchId", b.getBatchUid());
            map.put("internalId", b.getId()); // Real ID for API calls
            map.put("fileName", b.getFileName());
            map.put("uploadTime", b.getCreatedAt());
            map.put("totalCount", b.getTotalCount());
            
            // Calculate processed count
            QueryWrapper<Issue> issueQw = new QueryWrapper<>();
            issueQw.eq("batch_id", b.getId());
            issueQw.ne("status", "PENDING");
            Long processed = issueMapper.selectCount(issueQw);
            
            map.put("processedCount", processed);
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Issue> getTaskDetails(Long batchId) {
        QueryWrapper<Issue> qw = new QueryWrapper<>();
        qw.eq("batch_id", batchId);
        return issueMapper.selectList(qw);
    }

    @Override
    @Transactional
    public void verifyIssue(Long issueId) {
        Issue issue = this.getById(issueId);
        if(issue == null) throw new RuntimeException("Issue not found");
        
        issue.setStatus("VERIFIED");
        issue.setProcessedAt(new Date());
        // Clear previous corrections if any
        issue.setHumanCategoryLarge(null);
        issue.setHumanCategorySub(null);
        issue.setReason(null);
        
        this.updateById(issue);
        checkBatchCompletion(issue.getBatchId());
    }

    @Override
    @Transactional
    public void correctIssue(Long issueId, Map<String, String> data) {
        Issue issue = this.getById(issueId);
        if(issue == null) throw new RuntimeException("Issue not found");
        
        issue.setStatus("CORRECTED");
        issue.setProcessedAt(new Date());
        issue.setHumanCategoryLarge(data.get("categoryLarge"));
        issue.setHumanCategorySub(data.get("categorySub"));
        issue.setReason(data.get("reason"));
        
        this.updateById(issue);
        checkBatchCompletion(issue.getBatchId());
    }
    
    private void checkBatchCompletion(Long batchId) {
        // Check if all issues in batch are processed
        QueryWrapper<Issue> qw = new QueryWrapper<>();
        qw.eq("batch_id", batchId);
        qw.eq("status", "PENDING");
        Long pendingCount = issueMapper.selectCount(qw);
        
        if (pendingCount == 0) {
            Batch batch = batchMapper.selectById(batchId);
            if (batch != null && !"COMPLETED".equals(batch.getStatus())) {
                batch.setStatus("COMPLETED");
                batchMapper.updateById(batch);
            }
        }
    }
}

