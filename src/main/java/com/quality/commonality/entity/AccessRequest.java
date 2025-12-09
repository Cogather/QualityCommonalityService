package com.quality.commonality.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("access_requests")
public class AccessRequest {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long adminId;

    private String status; // PENDING, APPROVED, REJECTED

    private String reason;

    private Date createdAt;
}


