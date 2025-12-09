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
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(LoginRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        User user = this.getOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!request.getPassword().equals(user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
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
        User user = this.getById(userId);
        if (user == null) throw new RuntimeException("User not found");
        
        user.setRole("USER"); // Default to USER role after approval
        this.updateById(user);
    }

    @Override
    public void revokeUser(Long userId) {
        User user = this.getById(userId);
        if (user == null) throw new RuntimeException("User not found");

        user.setRole("GUEST");
        this.updateById(user);
    }

    @Override
    public List<User> listUsers(String role) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (role != null && !role.isEmpty()) {
            qw.eq("role", role);
        }
        return this.list(qw);
    }
}
