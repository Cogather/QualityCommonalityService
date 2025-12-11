package com.quality.commonality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quality.commonality.entity.Issue;

import java.util.List;
import java.util.Map;

public interface TaskService extends IService<Issue> {
    List<Map<String, Object>> getMyTasks(Long userId);
    
    /**
     * 获取批次的任务详情，按聚类组分组返回
     * @param batchId 批次ID
     * @return 按聚类组分组的任务列表，每个聚类组包含其下的所有问题
     */
    List<Map<String, Object>> getTaskDetails(Long batchId);
    
    /**
     * 标记问题为准确（VERIFIED状态）
     * @param issueId 问题ID
     * @param operatorId 操作人ID
     */
    void verifyIssue(Long issueId, Long operatorId);
    
    /**
     * 提交问题纠错（CORRECTED状态）
     * @param issueId 问题ID
     * @param correctionData 纠错数据
     * @param operatorId 操作人ID
     */
    void correctIssue(Long issueId, Map<String, String> correctionData, Long operatorId);
}

