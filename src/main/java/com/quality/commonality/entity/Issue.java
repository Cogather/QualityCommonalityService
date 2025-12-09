package com.quality.commonality.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("issues")
public class Issue {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long batchId;
    
    private String title;
    
    private String description;
    
    private String resolution;
    
    private String issueType;
    
    private String spdt;
    
    private String ipmt;
    
    private String aiCategoryLarge;
    
    private String aiCategorySub;
    
    private String humanCategoryLarge;
    
    private String humanCategorySub;
    
    private String reason;
    
    private String status; // PENDING, VERIFIED, CORRECTED
    
    private Date processedAt;
}

