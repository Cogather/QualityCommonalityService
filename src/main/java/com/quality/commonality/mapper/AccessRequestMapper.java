package com.quality.commonality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quality.commonality.entity.AccessRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AccessRequestMapper extends BaseMapper<AccessRequest> {

    @Select("SELECT ar.id, ar.user_id as userId, u.username as userName, " +
            "ar.admin_id as adminId, a.username as adminName, ar.reason, ar.status, ar.created_at as createdAt " +
            "FROM access_requests ar " +
            "LEFT JOIN users u ON ar.user_id = u.id " +
            "LEFT JOIN users a ON ar.admin_id = a.id " +
            "WHERE ar.admin_id = #{adminId} AND ar.status = 'PENDING' " +
            "ORDER BY ar.created_at DESC")
    List<Map<String, Object>> selectPendingByAdmin(Long adminId);
}


