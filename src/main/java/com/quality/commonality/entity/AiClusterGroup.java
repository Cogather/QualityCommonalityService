package com.quality.commonality.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("ai_cluster_groups")
public class AiClusterGroup {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long batchId;
    
    private String categoryLarge; // AI预测的大类（如：网络问题）
    
    private String categorySub; // AI预测的子类（如：连接超时）
    
    private String summary; // 聚类总结
    
    private Integer problemCount; // 该组包含的问题数
    
    private Date createdAt;
}

