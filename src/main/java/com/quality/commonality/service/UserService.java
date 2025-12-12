package com.quality.commonality.service;

import com.quality.commonality.dto.LoginRequest;
import com.quality.commonality.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> login(LoginRequest request);
    
    List<User> getAdmins();
    
    void approveUser(Long userId);

    /**
     * 撤销用户权限：将角色重置为 GUEST，清空审批人标记
     */
    void revokeUser(Long userId);

    /**
     * 查询用户列表，可选按角色过滤
     */
    List<User> listUsers(String role);
}
