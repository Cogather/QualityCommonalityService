package com.quality.commonality.controller;

import com.quality.commonality.common.Result;
import com.quality.commonality.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(dashboardService.getAdminStats());
    }

    @GetMapping("/chart/top-errors")
    public Result<List<Map<String, Object>>> getTopErrors() {
        return Result.success(dashboardService.getTopErrors());
    }

    @GetMapping("/chart/category-dist")
    public Result<List<Map<String, Object>>> getCategoryDistribution() {
        return Result.success(dashboardService.getCategoryDistribution());
    }
}

