package com.quality.commonality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quality.commonality.entity.AiClusterGroup;
import com.quality.commonality.entity.Batch;
import com.quality.commonality.entity.Issue;
import com.quality.commonality.entity.User;
import com.quality.commonality.mapper.AiClusterGroupMapper;
import com.quality.commonality.mapper.BatchMapper;
import com.quality.commonality.mapper.IssueMapper;
import com.quality.commonality.mapper.UserMapper;
import com.quality.commonality.service.BatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BatchServiceImpl extends ServiceImpl<BatchMapper, Batch> implements BatchService {

    @Autowired
    private BatchMapper batchMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private IssueMapper issueMapper;
    
    @Autowired
    private AiClusterGroupMapper clusterGroupMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Map<String, Object>> getBatchList() {
        return batchMapper.selectBatchesWithAssignee();
    }
    
    @Override
    @Transactional
    public Map<String, Object> uploadAndParseBatch(MultipartFile file, Long uploaderId) {
        try {
            // 1. 读取并解析JSON文件
            String jsonContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<Map<String, Object>> clusterGroups = objectMapper.readValue(
                jsonContent, 
                new TypeReference<List<Map<String, Object>>>() {}
            );
            
            // 2. 创建批次记录
            Batch batch = new Batch();
            batch.setFileName(file.getOriginalFilename());
            batch.setBatchUid(generateBatchUid());
            batch.setCreatedBy(uploaderId);
            batch.setCreatedAt(new Date());
            batch.setStatus("PENDING");
            
            // 计算总问题数：统计所有details数组中的元素总数
            int totalCount = 0;
            for (Map<String, Object> group : clusterGroups) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> details = (List<Map<String, Object>>) group.get("details");
                if (details != null && !details.isEmpty()) {
                    totalCount += details.size();
                }
            }
            batch.setTotalCount(totalCount);
            this.save(batch);
            
            // 3. 遍历每个聚类组，创建聚类组和问题记录
            for (Map<String, Object> groupData : clusterGroups) {
                // 解析type字段，拆分为大类/子类
                String type = (String) groupData.get("type");
                String[] categoryParts = parseCategoryType(type);
                
                // 创建AI聚类组
                AiClusterGroup clusterGroup = new AiClusterGroup();
                clusterGroup.setBatchId(batch.getId());
                clusterGroup.setCategoryLarge(categoryParts[0]);
                clusterGroup.setCategorySub(categoryParts[1]);
                clusterGroup.setSummary((String) groupData.get("summary"));
                
                // 从details数组获取实际问题列表
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> details = (List<Map<String, Object>>) groupData.get("details");
                int problemCount = 0;
                if (details != null && !details.isEmpty()) {
                    problemCount = details.size();
                } else {
                    // 如果没有details，使用count字段
                    Object countObj = groupData.get("count");
                    if (countObj instanceof Number) {
                        problemCount = ((Number) countObj).intValue();
                    }
                }
                
                clusterGroup.setProblemCount(problemCount);
                clusterGroup.setCreatedAt(new Date());
                // 先保存聚类组，获取ID
                clusterGroupMapper.insert(clusterGroup);
                
                // 创建每个问题记录
                if (details != null && !details.isEmpty()) {
                    for (Map<String, Object> detail : details) {
                        Issue issue = new Issue();
                        issue.setBatchId(batch.getId());
                        issue.setClusterId(clusterGroup.getId());
                        issue.setProblemDetail(getStringValue(detail, "PROBLEM_DETAIL"));
                        issue.setResolutionDetail(getStringValue(detail, "RESOLUTION_DETAIL"));
                        issue.setIssueDetails(getStringValue(detail, "ISSUE_DETAILS"));
                        issue.setIssueNo(getStringValue(detail, "ISSUE_NO"));
                        issue.setProdEnName(getStringValue(detail, "PROD_EN_NAME"));
                        issue.setStatus("PENDING");
                        // PENDING状态：初始化human_category字段为AI预测的类别（统一使用此字段）
                        issue.setHumanCategoryLarge(categoryParts[0]);
                        issue.setHumanCategorySub(categoryParts[1]);
                        
                        issueMapper.insert(issue);
                    }
                }
            }
            
            // 4. 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("batch_id", batch.getId());
            result.put("file_name", batch.getFileName());
            result.put("total_count", batch.getTotalCount());
            result.put("status", batch.getStatus());
            result.put("batch_uid", batch.getBatchUid());
            
            return result;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON file: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成批次号：B-YYYYMMDD-XXX
     */
    private String generateBatchUid() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        String randomStr = String.format("%03d", new Random().nextInt(1000));
        return "B-" + dateStr + "-" + randomStr;
    }
    
    /**
     * 解析类别类型，拆分为大类/子类
     * 支持格式："大类/子类" 或 "大类 > 子类" 或只有大类
     */
    private String[] parseCategoryType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return new String[]{"未分类", "未分类"};
        }
        
        String trimmed = type.trim();
        
        // 尝试按 "/" 分割
        if (trimmed.contains("/")) {
            String[] parts = trimmed.split("/", 2);
            return new String[]{
                parts[0].trim(),
                parts.length > 1 && !parts[1].trim().isEmpty() ? parts[1].trim() : "未分类"
            };
        }
        
        // 尝试按 ">" 分割
        if (trimmed.contains(">")) {
            String[] parts = trimmed.split(">", 2);
            return new String[]{
                parts[0].trim(),
                parts.length > 1 && !parts[1].trim().isEmpty() ? parts[1].trim() : "未分类"
            };
        }
        
        // 如果没有分隔符，整个作为大类，子类为空
        return new String[]{trimmed, "未分类"};
    }
    
    /**
     * 安全获取字符串值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
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
        // 注意：users表中没有status字段，只按role筛选
        return userMapper.selectList(qw);
    }
    
    @Override
    @Transactional
    public void deleteBatch(Long batchId) {
        // 1. 检查批次是否存在
        Batch batch = this.getById(batchId);
        if (batch == null) {
            throw new RuntimeException("批次不存在");
        }
        
        // 2. 删除该批次下的所有问题
        QueryWrapper<Issue> issueQw = new QueryWrapper<>();
        issueQw.eq("batch_id", batchId);
        issueMapper.delete(issueQw);
        
        // 3. 删除该批次下的所有聚类组
        QueryWrapper<AiClusterGroup> clusterQw = new QueryWrapper<>();
        clusterQw.eq("batch_id", batchId);
        clusterGroupMapper.delete(clusterQw);
        
        // 4. 删除批次本身
        this.removeById(batchId);
    }
}

