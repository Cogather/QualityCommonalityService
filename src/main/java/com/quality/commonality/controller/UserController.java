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
}
