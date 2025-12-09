package com.quality.commonality.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    // Storing as String for simplicity, or could use Enum
    private String role; // ADMIN, USER, GUEST
    
    private String status; // ACTIVE, DISABLED
    
    private Long targetApproverId;
    
    private String passwordHash; // Simplified as plain password for this demo if needed, but field name suggests hash
    
    private Date createdAt;
}

