package com.quality.commonality.service;

import com.quality.commonality.dto.LoginRequest;
import com.quality.commonality.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> login(LoginRequest request);
}

