package com.quality.commonality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quality.commonality.entity.Batch;
import com.quality.commonality.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface BatchService extends IService<Batch> {
    List<Map<String, Object>> getBatchList();
    
    /**
     * 上传并解析JSON文件，创建批次
     * @param file JSON文件
     * @param uploaderId 上传人ID
     * @return 批次ID和统计信息
     */
    Map<String, Object> uploadAndParseBatch(MultipartFile file, Long uploaderId);
    
    void distributeBatch(Long batchId, Long userId);
    
    List<User> getAssignableUsers();
    
    /**
     * 删除批次及其关联的所有数据
     * @param batchId 批次ID
     */
    void deleteBatch(Long batchId);
}

