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
    
    private Long clusterId; // 关联AI聚类组ID
    
    // 只保留需要展示的字段：PROBLEM_DETAIL, RESOLUTION_DETAIL, ISSUE_DETAILS, ISSUE_NO, PROD_EN_NAME
    private String problemDetail; // PROBLEM_DETAIL - 问题详情
    
    private String resolutionDetail; // RESOLUTION_DETAIL - 解决方案详情
    
    private String issueDetails; // ISSUE_DETAILS - 问题详细信息
    
    private String issueNo; // ISSUE_NO - 问题编号
    
    private String prodEnName; // PROD_EN_NAME - 产品英文名称
    
    // 实际类别字段（统一使用human_category字段存储）
    // VERIFIED/PENDING状态：存储AI预测的类别
    // CORRECTED状态：存储人工修正的类别
    // 这样所有状态都可以直接从issues表获取类别，无需JOIN查询
    private String humanCategoryLarge;
    
    private String humanCategorySub;
    
    private String reason; // 纠错原因（仅在CORRECTED状态时有值）
    
    private String status; // PENDING, VERIFIED, CORRECTED
    
    private Long operatorId; // 操作人ID
    
    private Date processedAt;
}

