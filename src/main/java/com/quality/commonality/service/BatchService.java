package com.quality.commonality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quality.commonality.entity.Batch;
import com.quality.commonality.entity.User;

import java.util.List;
import java.util.Map;

public interface BatchService extends IService<Batch> {
    List<Map<String, Object>> getBatchList();
    
    void createMockBatch(String fileName, Long uploaderId);
    
    void distributeBatch(Long batchId, Long userId);
    
    List<User> getAssignableUsers();
}

