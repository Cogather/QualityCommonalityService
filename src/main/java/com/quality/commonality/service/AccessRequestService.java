package com.quality.commonality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quality.commonality.entity.AccessRequest;

import java.util.List;
import java.util.Map;

public interface AccessRequestService extends IService<AccessRequest> {

    void apply(Long userId, Long adminId, String reason);

    List<Map<String, Object>> listByAdmin(Long adminId);

    void approve(Long requestId);

    void reject(Long requestId);
}


