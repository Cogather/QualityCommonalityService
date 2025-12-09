package com.quality.commonality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quality.commonality.entity.Issue;

import java.util.List;
import java.util.Map;

public interface TaskService extends IService<Issue> {
    List<Map<String, Object>> getMyTasks(Long userId);
    
    List<Issue> getTaskDetails(Long batchId);
    
    void verifyIssue(Long issueId);
    
    void correctIssue(Long issueId, Map<String, String> correctionData);
}

