package com.quality.commonality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quality.commonality.entity.AccessRequest;
import com.quality.commonality.mapper.AccessRequestMapper;
import com.quality.commonality.mapper.UserMapper;
import com.quality.commonality.entity.User;
import com.quality.commonality.service.AccessRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AccessRequestServiceImpl extends ServiceImpl<AccessRequestMapper, AccessRequest> implements AccessRequestService {

    private final UserMapper userMapper;

    public AccessRequestServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public void apply(Long userId, Long adminId, String reason) {
        // 先关闭同一用户的未处理申请，避免重复
        QueryWrapper<AccessRequest> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        qw.eq("status", "PENDING");
        this.remove(qw);

        AccessRequest req = new AccessRequest();
        req.setUserId(userId);
        req.setAdminId(adminId);
        req.setReason(reason);
        req.setStatus("PENDING");
        this.save(req);
    }

    @Override
    public List<Map<String, Object>> listByAdmin(Long adminId) {
        return this.baseMapper.selectPendingByAdmin(adminId);
    }

    @Override
    @Transactional
    public void approve(Long requestId) {
        AccessRequest req = this.getById(requestId);
        if (req == null) throw new RuntimeException("Request not found");
        req.setStatus("APPROVED");
        this.updateById(req);

        // 审批通过后，更新用户角色为 USER
        User user = userMapper.selectById(req.getUserId());
        if (user == null) throw new RuntimeException("User not found");
        user.setRole("USER");
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void reject(Long requestId) {
        AccessRequest req = this.getById(requestId);
        if (req == null) throw new RuntimeException("Request not found");
        req.setStatus("REJECTED");
        this.updateById(req);
    }
}


