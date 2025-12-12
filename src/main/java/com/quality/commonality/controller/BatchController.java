package com.quality.commonality.controller;

import com.quality.commonality.common.Result;
import com.quality.commonality.entity.User;
import com.quality.commonality.service.BatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        
        if (!file.getOriginalFilename().endsWith(".json")) {
            return Result.error("只支持JSON文件");
        }
        
        // In real app, get user from context/token. Here assuming admin ID 1
        Long userId = 1L;
        
        try {
            Map<String, Object> result = batchService.uploadAndParseBatch(file, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
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
    
    @DeleteMapping("/{id}")
    public Result<String> deleteBatch(@PathVariable Long id) {
        try {
            batchService.deleteBatch(id);
            return Result.success("批次删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}

