package com.quality.commonality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quality.commonality.entity.AiClusterGroup;
import com.quality.commonality.entity.Batch;
import com.quality.commonality.entity.Issue;
import com.quality.commonality.mapper.AiClusterGroupMapper;
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
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl extends ServiceImpl<IssueMapper, Issue> implements TaskService {

    @Autowired
    private BatchMapper batchMapper;
    
    @Autowired
    private IssueMapper issueMapper;
    
    @Autowired
    private AiClusterGroupMapper clusterGroupMapper;

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
    public List<Map<String, Object>> getTaskDetails(Long batchId) {
        // 1. 查询该批次下的所有聚类组
        QueryWrapper<AiClusterGroup> clusterQw = new QueryWrapper<>();
        clusterQw.eq("batch_id", batchId);
        clusterQw.orderByAsc("category_large", "category_sub");
        List<AiClusterGroup> clusterGroups = clusterGroupMapper.selectList(clusterQw);
        
        // 2. 查询该批次下的所有问题
        QueryWrapper<Issue> issueQw = new QueryWrapper<>();
        issueQw.eq("batch_id", batchId);
        issueQw.orderByAsc("id");
        List<Issue> allIssues = issueMapper.selectList(issueQw);
        
        // 3. 按clusterId分组问题
        Map<Long, List<Issue>> issuesByCluster = allIssues.stream()
            .collect(Collectors.groupingBy(Issue::getClusterId));
        
        // 4. 构建返回结果：按聚类组分组
        List<Map<String, Object>> result = new ArrayList<>();
        for (AiClusterGroup cluster : clusterGroups) {
            Map<String, Object> clusterMap = new HashMap<>();
            
            // 聚类组基本信息
            clusterMap.put("clusterId", cluster.getId());
            clusterMap.put("categoryLarge", cluster.getCategoryLarge());
            clusterMap.put("categorySub", cluster.getCategorySub());
            clusterMap.put("summary", cluster.getSummary());
            clusterMap.put("problemCount", cluster.getProblemCount());
            
            // 该聚类组下的所有问题
            List<Issue> clusterIssues = issuesByCluster.getOrDefault(cluster.getId(), new ArrayList<>());
            List<Map<String, Object>> issueList = new ArrayList<>();
            for (Issue issue : clusterIssues) {
                Map<String, Object> issueMap = new HashMap<>();
                issueMap.put("id", issue.getId());
                issueMap.put("problemDetail", issue.getProblemDetail());
                issueMap.put("resolutionDetail", issue.getResolutionDetail());
                issueMap.put("issueDetails", issue.getIssueDetails());
                issueMap.put("issueNo", issue.getIssueNo());
                issueMap.put("prodEnName", issue.getProdEnName());
                issueMap.put("status", issue.getStatus());
                issueMap.put("humanCategoryLarge", issue.getHumanCategoryLarge());
                issueMap.put("humanCategorySub", issue.getHumanCategorySub());
                issueMap.put("reason", issue.getReason());
                // humanCategory字段在所有状态都存储实际类别，直接使用即可
                issueList.add(issueMap);
            }
            clusterMap.put("issues", issueList);
            
            result.add(clusterMap);
        }
        
        return result;
    }

    @Override
    @Transactional
    public void verifyIssue(Long issueId, Long operatorId) {
        Issue issue = this.getById(issueId);
        if(issue == null) throw new RuntimeException("Issue not found");
        
        // 获取AI预测的类别（从ai_cluster_groups表）
        AiClusterGroup cluster = clusterGroupMapper.selectById(issue.getClusterId());
        if(cluster == null) throw new RuntimeException("Cluster not found");
        
        issue.setStatus("VERIFIED");
        issue.setProcessedAt(new Date());
        issue.setOperatorId(operatorId); // 更新操作人ID
        // VERIFIED状态：将AI预测的类别写入human_category字段（统一使用此字段）
        issue.setHumanCategoryLarge(cluster.getCategoryLarge());
        issue.setHumanCategorySub(cluster.getCategorySub());
        // 清空纠错原因
        issue.setReason(null);
        
        this.updateById(issue);
        checkBatchCompletion(issue.getBatchId());
    }

    @Override
    @Transactional
    public void correctIssue(Long issueId, Map<String, String> data, Long operatorId) {
        Issue issue = this.getById(issueId);
        if(issue == null) throw new RuntimeException("Issue not found");
        
        String categoryLarge = data.get("categoryLarge");
        String categorySub = data.get("categorySub");
        
        issue.setStatus("CORRECTED");
        issue.setProcessedAt(new Date());
        issue.setOperatorId(operatorId); // 更新操作人ID
        // CORRECTED状态：将人工修正的类别写入human_category字段
        issue.setHumanCategoryLarge(categoryLarge);
        issue.setHumanCategorySub(categorySub);
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

