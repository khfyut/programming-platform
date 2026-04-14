CREATE TABLE IF NOT EXISTS `agent_feedback_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `request_id` VARCHAR(100) NOT NULL COMMENT 'Agent请求ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `problem_id` BIGINT NOT NULL COMMENT '题目ID',
  `submit_id` BIGINT NULL COMMENT '提交ID',
  `action_type` VARCHAR(50) NOT NULL COMMENT 'Agent动作类型',
  `feedback_type` VARCHAR(50) NOT NULL COMMENT '反馈类型',
  `metadata_json` TEXT NULL COMMENT '反馈补充信息',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_request_id` (`request_id`),
  KEY `idx_user_problem` (`user_id`, `problem_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent反馈日志';
