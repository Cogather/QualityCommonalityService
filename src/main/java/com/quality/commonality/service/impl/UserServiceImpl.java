package com.quality.commonality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quality.commonality.dto.LoginRequest;
import com.quality.commonality.entity.User;
import com.quality.commonality.mapper.UserMapper;
import com.quality.commonality.service.UserService;
import com.quality.commonality.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsername());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        User user = this.getOne(queryWrapper);

        if (user == null) {
            log.warn("Login failed: User not found for username: {}", request.getUsername());
            throw new RuntimeException("User not found");
        }

        if (!request.getPassword().equals(user.getPasswordHash())) {
            log.warn("Login failed: Invalid password for username: {}", request.getUsername());
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        log.info("Login successful for user: {}", request.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        
        return data;
    }

    @Override
    public List<User> getAdmins() {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("role", "ADMIN");
        return this.list(qw);
    }

    @Override
    public void approveUser(Long userId) {
        log.info("Approving user with ID: {}", userId);
        User user = this.getById(userId);
        if (user == null) {
            log.error("Approve user failed: User not found with ID: {}", userId);
            throw new RuntimeException("User not found");
        }
        
        user.setRole("USER"); // Default to USER role after approval
        this.updateById(user);
        log.info("User approved successfully. ID: {}, New Role: {}", userId, user.getRole());
    }

    @Override
    public void revokeUser(Long userId) {
        log.info("Revoking user with ID: {}", userId);
        User user = this.getById(userId);
        if (user == null) {
            log.error("Revoke user failed: User not found with ID: {}", userId);
            throw new RuntimeException("User not found");
        }

        user.setRole("GUEST");
        this.updateById(user);
        log.info("User revoked successfully. ID: {}, New Role: {}", userId, user.getRole());
    }

    @Override
    public List<User> listUsers(String role) {
        log.debug("Listing users with role filter: {}", role);
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (role != null && !role.isEmpty()) {
            qw.eq("role", role);
        }
        return this.list(qw);
    }

    @Override
    public User getOrCreateByUsername(String username) {
        log.debug("Get or create user by username: {}", username);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = this.getOne(queryWrapper);

        if (user == null) {
            log.info("User not found, creating new guest user: {}", username);
            user = new User();
            user.setUsername(username);
            user.setRole("GUEST");
            // 设置默认密码，实际生产中应更安全处理
            user.setPasswordHash("123456"); 
            this.save(user);
            log.info("New user created with ID: {}", user.getId());
        }
        return user;
    }

    @Override
    public void updateUserRole(Long userId, String role) {
        log.info("Updating role for user ID: {} to {}", userId, role);
        User user = this.getById(userId);
        if (user == null) {
            log.error("Update role failed: User not found with ID: {}", userId);
            throw new RuntimeException("User not found");
        }
        user.setRole(role);
        this.updateById(user);
    }
}
