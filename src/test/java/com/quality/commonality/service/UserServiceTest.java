package com.quality.commonality.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.quality.commonality.dto.LoginRequest;
import com.quality.commonality.entity.User;
import com.quality.commonality.mapper.UserMapper;
import com.quality.commonality.service.impl.UserServiceImpl;
import com.quality.commonality.util.JwtUtil;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPasswordHash("password");
        user.setRole("ADMIN");

        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(user);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("mock-token");

        Map<String, Object> result = userService.login(request);

        assertNotNull(result);
        assertEquals("mock-token", result.get("token"));
        assertEquals(user, result.get("user"));
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword("password");

        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> userService.login(request));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void login_shouldThrowException_whenPasswordInvalid() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setUsername("admin");
        user.setPasswordHash("password");

        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(user);

        Exception exception = assertThrows(RuntimeException.class, () -> userService.login(request));
        assertEquals("Invalid password", exception.getMessage());
    }

    @Test
    void getAdmins_shouldReturnAdminList() {
        User admin = new User();
        admin.setRole("ADMIN");
        when(userMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.singletonList(admin));

        List<User> result = userService.getAdmins();

        assertFalse(result.isEmpty());
        assertEquals("ADMIN", result.get(0).getRole());
    }

    @Test
    void approveUser_shouldUpdateRole_whenUserExists() {
        User user = new User();
        user.setId(1L);
        user.setRole("GUEST");

        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.approveUser(1L);

        assertEquals("USER", user.getRole());
        verify(userMapper).updateById(user);
    }

    @Test
    void approveUser_shouldThrowException_whenUserNotFound() {
        when(userMapper.selectById(1L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> userService.approveUser(1L));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void revokeUser_shouldUpdateRole_whenUserExists() {
        User user = new User();
        user.setId(1L);
        user.setRole("USER");

        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.revokeUser(1L);

        assertEquals("GUEST", user.getRole());
        verify(userMapper).updateById(user);
    }

    @Test
    void revokeUser_shouldThrowException_whenUserNotFound() {
        when(userMapper.selectById(1L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> userService.revokeUser(1L));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void listUsers_shouldFilterByRole_whenRoleProvided() {
        User user = new User();
        user.setRole("USER");
        when(userMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.singletonList(user));

        List<User> result = userService.listUsers("USER");

        assertFalse(result.isEmpty());
        assertEquals("USER", result.get(0).getRole());
    }

    @Test
    void listUsers_shouldReturnAll_whenRoleIsNull() {
        when(userMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        userService.listUsers(null);

        verify(userMapper).selectList(any(QueryWrapper.class));
    }
    
    @Test
    void listUsers_shouldReturnAll_whenRoleIsEmpty() {
        when(userMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        userService.listUsers("");

        verify(userMapper).selectList(any(QueryWrapper.class));
    }
}
