package com.quality.commonality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quality.commonality.entity.Issue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface IssueMapper extends BaseMapper<Issue> {
    
    // 从ai_cluster_groups表获取类别统计（使用category_large和category_sub字段）
    @Select("SELECT CONCAT(acg.category_large, ' > ', acg.category_sub) as name, COUNT(*) as value " +
            "FROM issues i " +
            "INNER JOIN ai_cluster_groups acg ON i.cluster_id = acg.id " +
            "WHERE i.status != 'PENDING' " +
            "GROUP BY acg.category_large, acg.category_sub")
    List<Map<String, Object>> selectCategoryStats();
    
    // 获取Top 5错误类别（基于人工修正的类别）
    @Select("SELECT human_category_large as name, COUNT(*) as value " +
            "FROM issues " +
            "WHERE status = 'CORRECTED' AND human_category_large IS NOT NULL " +
            "GROUP BY human_category_large " +
            "ORDER BY value DESC LIMIT 5")
    List<Map<String, Object>> selectTopErrors();
}

