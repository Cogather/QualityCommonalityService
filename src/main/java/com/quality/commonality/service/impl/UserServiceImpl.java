package com.quality.commonality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quality.commonality.dto.LoginRequest;
import com.quality.commonality.entity.User;
import com.quality.commonality.mapper.UserMapper;
import com.quality.commonality.service.UserService;
import com.quality.commonality.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(LoginRequest request) {
        // Simple implementation: Find user by username
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        User user = this.getOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // In real app, use BCrypt or similar. Here we just compare strings for "simple version"
        // Or if the design doc implies hashing, we should assume the input is hashed or we hash it here.
        // For this demo, let's assume plain text check for simplicity as requested.
        if (!request.getPassword().equals(user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        if ("DISABLED".equals(user.getStatus())) {
            throw new RuntimeException("Account is disabled");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        
        return data;
    }
}

