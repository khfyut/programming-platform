-- 为用户题目交互状态表添加学习语义字段
ALTER TABLE `user_problem_interaction`
ADD COLUMN `error_tag` VARCHAR(50) NULL COMMENT '最近错误标签：ARRAY_BOUNDARY, TIME_COMPLEXITY, LOGIC_ERROR等',
ADD COLUMN `weak_points` TEXT NULL COMMENT '薄弱知识点列表（JSON格式）',
ADD COLUMN `learning_stage` VARCHAR(30) DEFAULT 'FIRST_TRY' COMMENT '学习阶段：FIRST_TRY, HINTED, DIAGNOSED, EXPLAINED, MASTERED';

-- 添加索引便于查询
CREATE INDEX `idx_error_tag` ON `user_problem_interaction` (`error_tag`);
CREATE INDEX `idx_learning_stage` ON `user_problem_interaction` (`learning_stage`);
