-- 创建测试用例表
CREATE TABLE IF NOT EXISTS `test_case` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `problem_id` BIGINT NOT NULL COMMENT '题目ID',
  `input` TEXT COMMENT '输入数据',
  `output` TEXT COMMENT '期望输出',
  `is_sample` TINYINT(1) DEFAULT 1 COMMENT '是否为示例测试用例（1-是，0-否）',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_problem_id` (`problem_id`),
  KEY `idx_is_sample` (`is_sample`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试用例表';

-- 创建提交测试用例结果表
CREATE TABLE IF NOT EXISTS `submit_test_case_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `submit_id` BIGINT NOT NULL COMMENT '提交记录ID',
  `test_case_id` BIGINT NOT NULL COMMENT '测试用例ID',
  `result` INT COMMENT '执行结果（0-成功，1-失败，2-超时，3-内存超限，4-编译错误）',
  `time_cost` INT COMMENT '时间消耗（毫秒）',
  `memory_cost` INT COMMENT '内存消耗（KB）',
  `actual_output` TEXT COMMENT '实际输出',
  `error_message` TEXT COMMENT '错误信息',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_submit_id` (`submit_id`),
  KEY `idx_test_case_id` (`test_case_id`),
  KEY `idx_result` (`result`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提交测试用例结果表';

-- 为 problem 表添加新字段以增强题目复杂度定义
-- 添加执行限制字段
ALTER TABLE `problem` ADD COLUMN `time_limit` INT DEFAULT 1000 COMMENT '时间限制（毫秒）';
ALTER TABLE `problem` ADD COLUMN `memory_limit` INT DEFAULT 256 COMMENT '内存限制（MB）';

-- 添加分类标签字段
ALTER TABLE `problem` ADD COLUMN `tags` VARCHAR(500) COMMENT '题目标签，逗号分隔';
ALTER TABLE `problem` ADD COLUMN `knowledge_points` VARCHAR(500) COMMENT '知识点，逗号分隔';

-- 添加辅助学习字段
ALTER TABLE `problem` ADD COLUMN `hints` TEXT COMMENT '提示信息，JSON格式';
ALTER TABLE `problem` ADD COLUMN `sample_explanation` TEXT COMMENT '示例解释';

-- 添加索引优化查询性能
CREATE INDEX `idx_tags` ON `problem` (`tags`(255));
CREATE INDEX `idx_difficulty` ON `problem` (`difficulty`);