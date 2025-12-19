package com.quality.commonality.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.quality.commonality.entity.AccessRequest;
import com.quality.commonality.entity.User;
import com.quality.commonality.mapper.AccessRequestMapper;
import com.quality.commonality.mapper.UserMapper;
import com.quality.commonality.service.impl.AccessRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessRequestServiceTest {

    @Mock
    private AccessRequestMapper accessRequestMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AccessRequestServiceImpl accessRequestService;

    @BeforeEach
    void setUp() {
        // Fix for MyBatis-Plus ServiceImpl NPE
        ReflectionTestUtils.setField(accessRequestService, "baseMapper", accessRequestMapper);
    }

    @Test
    void apply_shouldRemovePendingAndCreateNew() {
        Long userId = 1L;
        Long adminId = 2L;
        String reason = "Upgrade me";
        
        accessRequestService.apply(userId, adminId, reason);
        
        verify(accessRequestMapper).delete(any(QueryWrapper.class));
        verify(accessRequestMapper).insert(any(AccessRequest.class));
    }

    @Test
    void listByAdmin_shouldDelegateToMapper() {
        accessRequestService.listByAdmin(1L);
        verify(accessRequestMapper).selectPendingByAdmin(1L);
    }

    @Test
    void approve_shouldUpdateStatusAndUserRole() {
        AccessRequest req = new AccessRequest();
        req.setId(1L);
        req.setUserId(100L);
        req.setStatus("PENDING");
        
        User user = new User();
        user.setId(100L);
        user.setRole("GUEST");
        
        when(accessRequestMapper.selectById(1L)).thenReturn(req);
        when(userMapper.selectById(100L)).thenReturn(user);
        
        accessRequestService.approve(1L);
        
        assertEquals("APPROVED", req.getStatus());
        assertEquals("USER", user.getRole());
        
        verify(accessRequestMapper).updateById(req);
        verify(userMapper).updateById(user);
    }
    
    @Test
    void approve_shouldThrow_whenRequestNotFound() {
        when(accessRequestMapper.selectById(1L)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> accessRequestService.approve(1L));
    }
    
    @Test
    void approve_shouldThrow_whenUserNotFound() {
        AccessRequest req = new AccessRequest();
        req.setId(1L);
        req.setUserId(100L);
        when(accessRequestMapper.selectById(1L)).thenReturn(req);
        when(userMapper.selectById(100L)).thenReturn(null);
        
        assertThrows(RuntimeException.class, () -> accessRequestService.approve(1L));
    }

    @Test
    void reject_shouldUpdateStatusOnly() {
        AccessRequest req = new AccessRequest();
        req.setId(1L);
        req.setStatus("PENDING");
        
        when(accessRequestMapper.selectById(1L)).thenReturn(req);
        
        accessRequestService.reject(1L);
        
        assertEquals("REJECTED", req.getStatus());
        verify(accessRequestMapper).updateById(req);
        verifyNoInteractions(userMapper);
    }
    
    @Test
    void reject_shouldThrow_whenRequestNotFound() {
        when(accessRequestMapper.selectById(1L)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> accessRequestService.reject(1L));
    }
}
