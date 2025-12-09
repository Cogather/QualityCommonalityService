package com.quality.commonality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quality.commonality.entity.Issue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface IssueMapper extends BaseMapper<Issue> {
    
    @Select("SELECT ai_category_large as name, COUNT(*) as value FROM issues WHERE status != 'PENDING' GROUP BY ai_category_large")
    List<Map<String, Object>> selectCategoryStats();
    
    @Select("SELECT ai_category_sub as name, COUNT(*) as value FROM issues WHERE status != 'PENDING' GROUP BY ai_category_sub ORDER BY value DESC LIMIT 5")
    List<Map<String, Object>> selectTopErrors();
}

