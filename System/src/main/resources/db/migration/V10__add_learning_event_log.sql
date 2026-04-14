-- 创建Agent学习事件日志表
-- 用于记录Agent决策产生的动作执行历史

CREATE TABLE IF NOT EXISTS `learning_event_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `problem_id` BIGINT NOT NULL COMMENT '题目ID',
  `submit_id` BIGINT COMMENT '关联的提交ID',
  `action_type` VARCHAR(50) NOT NULL COMMENT '动作类型：HINT, DIAGNOSE, EXPLAIN, RECOMMEND, REVEAL_ANSWER',
  `action_id` VARCHAR(100) COMMENT '动作唯一标识',
  `strategy` VARCHAR(50) COMMENT '使用的策略',
  `content` TEXT COMMENT '动作内容（提示文本、诊断结果等）',
  `request_id` VARCHAR(100) COMMENT 'Agent请求ID，用于追踪',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_problem` (`user_id`, `problem_id`),
  KEY `idx_submit` (`submit_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent学习事件日志';

-- 创建用户题目交互状态表
-- 用于记录用户对某道题的交互状态（是否已提示、已诊断等）

CREATE TABLE IF NOT EXISTS `user_problem_interaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `problem_id` BIGINT NOT NULL COMMENT '题目ID',
  `hint_count` INT DEFAULT 0 COMMENT '已使用提示次数',
  `diagnose_count` INT DEFAULT 0 COMMENT '已诊断次数',
  `has_viewed_answer` TINYINT DEFAULT 0 COMMENT '是否已查看答案',
  `consecutive_failures` INT DEFAULT 0 COMMENT '连续失败次数',
  `last_submit_id` BIGINT COMMENT '最后一次提交ID',
  `last_action_time` DATETIME COMMENT '最后一次Agent动作时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_problem` (`user_id`, `problem_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_problem` (`problem_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户题目交互状态';
