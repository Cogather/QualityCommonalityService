package com.quality.commonality.controller;

import com.quality.commonality.common.Result;
import com.quality.commonality.entity.Batch;
import com.quality.commonality.entity.User;
import com.quality.commonality.service.BatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/batches")
@CrossOrigin(origins = "*")
public class BatchController {

    @Autowired
    private BatchService batchService;

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.success(batchService.getBatchList());
    }

    @PostMapping("/upload")
    public Result<String> upload(@RequestBody Map<String, String> payload) {
        String fileName = payload.get("fileName");
        // In real app, get user from context/token. Here assuming admin ID 1
        Long userId = 1L; 
        batchService.createMockBatch(fileName, userId);
        return Result.success("Batch created successfully");
    }

    @PostMapping("/{id}/distribute")
    public Result<String> distribute(@PathVariable Long id, @RequestBody Map<String, Long> payload) {
        Long userId = payload.get("userId");
        batchService.distributeBatch(id, userId);
        return Result.success("Batch distributed");
    }

    @GetMapping("/users")
    public Result<List<User>> getAssignableUsers() {
        return Result.success(batchService.getAssignableUsers());
    }
}

