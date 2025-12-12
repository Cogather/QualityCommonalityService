package com.quality.commonality.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("batches")
public class Batch {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchUid; // B-231001-001
    
    private String fileName;
    
    private Integer totalCount;
    
    private String status; // PENDING, ASSIGNED, COMPLETED
    
    private Long assigneeId;
    
    private Long createdBy;
    
    private Date createdAt;
}

