package com.quality.commonality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quality.commonality.entity.Batch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface BatchMapper extends BaseMapper<Batch> {
    // Custom query to join user table for assignee name
    @Select("SELECT b.id, b.batch_uid as batchUid, b.file_name as fileName, b.total_count as totalCount, " +
            "b.status, b.assignee_id as assigneeId, b.created_by as createdBy, b.created_at as createdAt, " +
            "u.username as assigneeName " +
            "FROM batches b LEFT JOIN users u ON b.assignee_id = u.id ORDER BY b.created_at DESC")
    List<Map<String, Object>> selectBatchesWithAssignee();
}

