package com.quality.commonality.controller;

import com.quality.commonality.common.Result;
import com.quality.commonality.dto.LoginRequest;
import com.quality.commonality.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Allow frontend to call
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        try {
            Map<String, Object> data = userService.login(loginRequest);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

