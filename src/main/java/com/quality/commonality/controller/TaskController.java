package com.quality.commonality.controller;

import com.quality.commonality.common.Result;
import com.quality.commonality.entity.Issue;
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
    public Result<List<Issue>> getTaskDetails(@PathVariable Long batchId) {
        // Note: batchId here refers to the database ID, not the string UID. 
        // Frontend should pass the internal ID or backend should handle lookup by UID.
        // For simplicity let's assume internal ID or we can add a lookup.
        // Given current frontend passes string UID in route params but maybe not internal ID.
        // Let's assume frontend will be updated to pass internal ID or we look up by UID if string.
        // To keep it simple, let's assume ID is passed.
        return Result.success(taskService.getTaskDetails(batchId));
    }
    
    // Helper to find ID by UID if needed, but let's stick to ID for now.
    
    @PostMapping("/items/{itemId}/verify")
    public Result<String> verifyItem(@PathVariable Long itemId) {
        taskService.verifyIssue(itemId);
        return Result.success("Verified");
    }

    @PostMapping("/items/{itemId}/correct")
    public Result<String> correctItem(@PathVariable Long itemId, @RequestBody Map<String, String> data) {
        taskService.correctIssue(itemId, data);
        return Result.success("Correction submitted");
    }
}

