package com.quality.commonality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quality.commonality.entity.Batch;
import com.quality.commonality.entity.Issue;
import com.quality.commonality.entity.User;
import com.quality.commonality.mapper.BatchMapper;
import com.quality.commonality.mapper.IssueMapper;
import com.quality.commonality.mapper.UserMapper;
import com.quality.commonality.service.BatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class BatchServiceImpl extends ServiceImpl<BatchMapper, Batch> implements BatchService {

    @Autowired
    private BatchMapper batchMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private IssueMapper issueMapper;

    @Override
    public List<Map<String, Object>> getBatchList() {
        return batchMapper.selectBatchesWithAssignee();
    }

    @Override
    @Transactional
    public void createMockBatch(String fileName, Long uploaderId) {
        // 1. Create Batch
        Batch batch = new Batch();
        batch.setFileName(fileName);
        batch.setBatchUid("B-" + System.currentTimeMillis());
        batch.setCreatedBy(uploaderId);
        batch.setCreatedAt(new Date());
        batch.setStatus("PENDING");
        
        int count = new Random().nextInt(20) + 10; // 10-30 items
        batch.setTotalCount(count);
        
        this.save(batch);
        
        // 2. Create Mock Issues
        String[] categories = {"网络问题", "硬件问题", "软件问题", "安全问题", "其他"};
        String[] subCats = {"连接超时", "配置错误", "内存溢出", "磁盘故障", "代码异常", "认证失败", "权限拒绝", "未知原因"};
        
        for (int i = 0; i < count; i++) {
            Issue issue = new Issue();
            issue.setBatchId(batch.getId());
            issue.setTitle("Case #" + (1000 + i) + ": Mock Issue");
            issue.setDescription("Auto generated mock issue description for testing.");
            issue.setResolution("Pending investigation");
            issue.setIssueType("Bug");
            issue.setSpdt("Group-" + (char)('A' + i % 3));
            issue.setIpmt("IPMT-" + (i % 5));
            
            issue.setAiCategoryLarge(categories[i % categories.length]);
            issue.setAiCategorySub(subCats[i % subCats.length]);
            
            issue.setStatus("PENDING");
            issueMapper.insert(issue);
        }
    }

    @Override
    public void distributeBatch(Long batchId, Long userId) {
        Batch batch = this.getById(batchId);
        if (batch == null) throw new RuntimeException("Batch not found");
        
        batch.setAssigneeId(userId);
        batch.setStatus("ASSIGNED");
        this.updateById(batch);
    }

    @Override
    public List<User> getAssignableUsers() {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("role", "USER");
        qw.eq("status", "ACTIVE");
        return userMapper.selectList(qw);
    }
}

