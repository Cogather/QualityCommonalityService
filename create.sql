CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `role` VARCHAR(20) DEFAULT 'GUEST' COMMENT '角色: ADMIN, USER, GUEST',
  `password_hash` VARCHAR(255) DEFAULT NULL COMMENT '密码哈希',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `batches` (
  `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
  `batch_uid` VARCHAR(50) NOT NULL COMMENT '批次号,如 B-231001-001',
  `file_name` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
  `total_count` INT DEFAULT 0 COMMENT '总数据量',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, ASSIGNED, COMPLETED',
  `assignee_id` BIGINT DEFAULT NULL COMMENT '当前处理人ID',
  `created_by` BIGINT DEFAULT NULL COMMENT '上传人ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_uid` (`batch_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批次任务表';

-- AI聚类组表
CREATE TABLE IF NOT EXISTS `ai_cluster_groups` (
  `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
  `batch_id` BIGINT NOT NULL COMMENT '关联批次ID',
  `category_large` VARCHAR(100) NOT NULL COMMENT 'AI预测的大类（如：网络问题）',
  `category_sub` VARCHAR(100) NOT NULL COMMENT 'AI预测的子类（如：连接超时）',
  `summary` TEXT COMMENT '聚类总结',
  `problem_count` INT DEFAULT 0 COMMENT '该组包含的问题数',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_category` (`category_large`, `category_sub`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聚类组表';

-- 问题表（只保留需要展示的字段：PROBLEM_DETAIL, RESOLUTION_DETAIL, ISSUE_DETAILS, ISSUE_NO, PROD_EN_NAME）
CREATE TABLE IF NOT EXISTS `issues` (
  `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
  `batch_id` BIGINT NOT NULL COMMENT '关联批次ID',
  `cluster_id` BIGINT NOT NULL COMMENT '关联AI聚类组ID',
  `problem_detail` TEXT COMMENT 'PROBLEM_DETAIL - 问题详情',
  `resolution_detail` TEXT COMMENT 'RESOLUTION_DETAIL - 解决方案详情',
  `issue_details` TEXT COMMENT 'ISSUE_DETAILS - 问题详细信息',
  `issue_no` VARCHAR(100) DEFAULT NULL COMMENT 'ISSUE_NO - 问题编号',
  `prod_en_name` VARCHAR(255) DEFAULT NULL COMMENT 'PROD_EN_NAME - 产品英文名称',
  
  `human_category_large` VARCHAR(100) DEFAULT NULL COMMENT '实际大类（VERIFIED/PENDING用AI预测，CORRECTED用人工修正）',
  `human_category_sub` VARCHAR(100) DEFAULT NULL COMMENT '实际子类（VERIFIED/PENDING用AI预测，CORRECTED用人工修正）',
  `reason` TEXT COMMENT '纠错原因（仅在CORRECTED状态时有值）',
  
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, VERIFIED, CORRECTED',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `processed_at` DATETIME DEFAULT NULL COMMENT '处理时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_cluster_id` (`cluster_id`),
  KEY `idx_operator_id` (`operator_id`, `status`),
  -- 优化筛选和导出查询：按human_category字段筛选（所有状态都使用此字段）
  KEY `idx_human_category` (`human_category_large`, `human_category_sub`),
  KEY `idx_status_human_category` (`status`, `human_category_large`, `human_category_sub`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='具体问题项表';

CREATE TABLE IF NOT EXISTS `access_requests` (
  `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '申请人ID',
  `admin_id` BIGINT NOT NULL COMMENT '审批管理员ID',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, APPROVED, REJECTED',
  `reason` VARCHAR(255) DEFAULT NULL COMMENT '申请备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限申请表';

-- 初始化一些测试数据
INSERT IGNORE INTO `users` (`username`, `role`, `password_hash`) VALUES 
('admin', 'ADMIN', '123456'),
('user1', 'USER', '123456'),
('user2', 'USER', '123456');
