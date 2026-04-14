ALTER TABLE `learning_event_log`
MODIFY COLUMN `problem_id` BIGINT NULL COMMENT '题目ID，非题目入口可为空',
ADD COLUMN `entry_ref_type` VARCHAR(50) NULL COMMENT 'Agent入口引用类型：PROBLEM, WRONG_BOOK, LEARNING_PATH_LEVEL',
ADD COLUMN `entry_ref_id` BIGINT NULL COMMENT 'Agent入口引用ID',
ADD COLUMN `path_id` BIGINT NULL COMMENT '学习路径ID',
ADD COLUMN `level_id` BIGINT NULL COMMENT '学习路径关卡ID',
ADD COLUMN `wrong_item_id` BIGINT NULL COMMENT '错题本条目ID';

ALTER TABLE `agent_feedback_log`
MODIFY COLUMN `problem_id` BIGINT NULL COMMENT '题目ID，非题目入口可为空',
ADD COLUMN `entry_ref_type` VARCHAR(50) NULL COMMENT 'Agent入口引用类型：PROBLEM, WRONG_BOOK, LEARNING_PATH_LEVEL',
ADD COLUMN `entry_ref_id` BIGINT NULL COMMENT 'Agent入口引用ID',
ADD COLUMN `path_id` BIGINT NULL COMMENT '学习路径ID',
ADD COLUMN `level_id` BIGINT NULL COMMENT '学习路径关卡ID',
ADD COLUMN `wrong_item_id` BIGINT NULL COMMENT '错题本条目ID';

CREATE INDEX `idx_learning_event_entry_ref` ON `learning_event_log` (`entry_ref_type`, `entry_ref_id`);
CREATE INDEX `idx_learning_event_path_level` ON `learning_event_log` (`path_id`, `level_id`);
CREATE INDEX `idx_agent_feedback_entry_ref` ON `agent_feedback_log` (`entry_ref_type`, `entry_ref_id`);
