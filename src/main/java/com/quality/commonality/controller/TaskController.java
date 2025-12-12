package com.quality.commonality.controller;

import com.quality.commonality.common.Result;
import com.quality.commonality.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public Result<List<Map<String, Object>>> getMyTasks(@RequestParam(required = false, defaultValue = "1") Long userId) {
        // In real app use security context to get current user
        return Result.success(taskService.getMyTasks(userId));
    }

    @GetMapping("/{batchId}")
    public Result<List<Map<String, Object>>> getTaskDetails(@PathVariable Long batchId) {
        // 返回按聚类组分组的任务列表
        // 每个聚类组包含：clusterId, categoryLarge, categorySub, summary, problemCount, issues[]
        // 每个问题包含：id, problemDetail, resolutionDetail, issueDetails, issueNo, prodEnName, status等
        return Result.success(taskService.getTaskDetails(batchId));
    }
    
    // Helper to find ID by UID if needed, but let's stick to ID for now.
    
    @PostMapping("/items/{itemId}/verify")
    public Result<String> verifyItem(
            @PathVariable Long itemId,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        // In real app use security context to get current user
        taskService.verifyIssue(itemId, userId);
        return Result.success("Verified");
    }

    @PostMapping("/items/{itemId}/correct")
    public Result<String> correctItem(
            @PathVariable Long itemId,
            @RequestBody Map<String, String> data,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        // In real app use security context to get current user
        taskService.correctIssue(itemId, data, userId);
        return Result.success("Correction submitted");
    }
}

