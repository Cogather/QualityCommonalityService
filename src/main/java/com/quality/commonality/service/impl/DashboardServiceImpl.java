package com.quality.commonality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.quality.commonality.entity.Batch;
import com.quality.commonality.entity.Issue;
import com.quality.commonality.mapper.BatchMapper;
import com.quality.commonality.mapper.IssueMapper;
import com.quality.commonality.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private IssueMapper issueMapper;
    
    @Autowired
    private BatchMapper batchMapper;

    @Override
    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total Predictions
        Long total = issueMapper.selectCount(null);
        stats.put("totalPredictions", total);
        
        // Pending Batches
        QueryWrapper<Batch> batchQw = new QueryWrapper<>();
        batchQw.eq("status", "PENDING");
        stats.put("pendingBatches", batchMapper.selectCount(batchQw));
        
        // Mock Accuracy (Requires labeled data vs prediction, simplified here)
        // Let's say Verified / (Verified + Corrected)
        QueryWrapper<Issue> verifiedQw = new QueryWrapper<>();
        verifiedQw.eq("status", "VERIFIED");
        Long verified = issueMapper.selectCount(verifiedQw);
        
        QueryWrapper<Issue> correctedQw = new QueryWrapper<>();
        correctedQw.eq("status", "CORRECTED");
        Long corrected = issueMapper.selectCount(correctedQw);
        
        long totalProcessed = verified + corrected;
        double accuracy = totalProcessed > 0 ? (double) verified / totalProcessed * 100 : 0;
        stats.put("accuracy", String.format("%.1f", accuracy));
        
        // Correction Progress
        QueryWrapper<Issue> processedQw = new QueryWrapper<>();
        processedQw.ne("status", "PENDING");
        Long processedAll = issueMapper.selectCount(processedQw);
        
        double progress = total > 0 ? (double) processedAll / total * 100 : 0;
        stats.put("correctionProgress", (int) progress);
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getTopErrors() {
        return issueMapper.selectTopErrors();
    }

    @Override
    public List<Map<String, Object>> getCategoryDistribution() {
        return issueMapper.selectCategoryStats();
    }
}

