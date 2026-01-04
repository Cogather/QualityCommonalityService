package com.quality.commonality.controller;

import com.quality.commonality.common.Result;
import com.quality.commonality.entity.User;
import com.quality.commonality.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/admins")
    public Result<List<User>> getAdmins() {
        return Result.success(userService.getAdmins());
    }
    
    @GetMapping
    public Result<List<User>> listUsers(@RequestParam(value = "role", required = false) String role) {
        return Result.success(userService.listUsers(role));
    }
    
    @PostMapping("/{id}/approve")
    public Result<String> approveUser(@PathVariable Long id) {
        userService.approveUser(id);
        return Result.success("User approved");
    }

    /**
     * 撤销用户权限（重置为 GUEST）
     */
    @PostMapping("/{id}/revoke")
    public Result<String> revokeUser(@PathVariable Long id) {
        userService.revokeUser(id);
        return Result.success("User revoked to GUEST");
    }

    /**
     * 根据 username 获取用户，如果不存在则创建（角色为 GUEST）
     */
    @PostMapping("/find-or-create")
    public Result<User> findOrCreateUser(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        if (username == null || username.trim().isEmpty()) {
            return Result.error("Username is required");
        }
        User user = userService.getOrCreateByUsername(username);
        return Result.success(user);
    }

    /**
     * 更新用户角色
     */
    @PostMapping("/{id}/role")
    public Result<String> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String role = payload.get("role");
        if (role == null || role.trim().isEmpty()) {
            return Result.error("Role is required");
        }
        // 简单校验角色合法性
        if (!"ADMIN".equals(role) && !"USER".equals(role) && !"GUEST".equals(role)) {
            return Result.error("Invalid role: " + role);
        }
        
        userService.updateUserRole(id, role);
        return Result.success("User role updated to " + role);
    }
}
