package com.quality.commonality.controller;

import com.quality.commonality.common.Result;
import com.quality.commonality.entity.AccessRequest;
import com.quality.commonality.service.AccessRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/access")
@CrossOrigin(origins = "*")
public class AccessRequestController {

    @Autowired
    private AccessRequestService accessRequestService;

    @PostMapping("/apply")
    public Result<String> apply(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        Long adminId = Long.valueOf(payload.get("adminId").toString());
        String reason = payload.getOrDefault("reason", "").toString();
        accessRequestService.apply(userId, adminId, reason);
        return Result.success("申请已提交");
    }

    @GetMapping("/admin/{adminId}")
    public Result<List<Map<String, Object>>> listForAdmin(@PathVariable Long adminId) {
        return Result.success(accessRequestService.listByAdmin(adminId));
    }

    @PostMapping("/{id}/approve")
    public Result<String> approve(@PathVariable Long id) {
        accessRequestService.approve(id);
        return Result.success("已审批通过");
    }

    @PostMapping("/{id}/reject")
    public Result<String> reject(@PathVariable Long id) {
        accessRequestService.reject(id);
        return Result.success("已驳回");
    }
}


