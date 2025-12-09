CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `role` VARCHAR(20) DEFAULT 'GUEST' COMMENT '角色: ADMIN, USER, GUEST',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, DISABLED',
  `target_approver_id` BIGINT DEFAULT NULL COMMENT '申请的审批人ID',
  `password_hash` VARCHAR(255) DEFAULT NULL COMMENT '密码哈希',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
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

CREATE TABLE IF NOT EXISTS `issues` (
  `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
  `batch_id` BIGINT NOT NULL COMMENT '关联批次ID',
  `title` VARCHAR(255) DEFAULT NULL COMMENT '问题标题/PROD_EN_NAME',
  `description` TEXT COMMENT '问题描述/ISSUE_DETAILS',
  `resolution` TEXT COMMENT '解决详情/RESOLUTION_DETAIL',
  `issue_type` VARCHAR(100) DEFAULT NULL COMMENT '问题类型',
  `spdt` VARCHAR(100) DEFAULT NULL COMMENT 'SPDT分组',
  `ipmt` VARCHAR(100) DEFAULT NULL COMMENT 'IPMT分组',
  
  `ai_category_large` VARCHAR(100) DEFAULT NULL COMMENT 'AI预测大类',
  `ai_category_sub` VARCHAR(100) DEFAULT NULL COMMENT 'AI预测子类',
  
  `human_category_large` VARCHAR(100) DEFAULT NULL COMMENT '人工修正大类',
  `human_category_sub` VARCHAR(100) DEFAULT NULL COMMENT '人工修正子类',
  `reason` TEXT COMMENT '纠错原因',
  
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, VERIFIED, CORRECTED',
  `processed_at` DATETIME DEFAULT NULL COMMENT '处理时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_batch_id` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='具体问题项表';

-- 初始化一些测试数据
INSERT IGNORE INTO `users` (`username`, `role`, `status`, `password_hash`, `created_at`) VALUES 
('admin', 'ADMIN', 'ACTIVE', '123456', NOW()),
('user1', 'USER', 'ACTIVE', '123456', NOW()),
('user2', 'USER', 'ACTIVE', '123456', NOW());
