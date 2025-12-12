package com.quality.commonality.service;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    Map<String, Object> getAdminStats();
    
    List<Map<String, Object>> getTopErrors();
    
    List<Map<String, Object>> getCategoryDistribution();
}

