ALTER TABLE `user_problem_interaction`
ADD COLUMN `last_action_type` VARCHAR(50) NULL COMMENT '最近一次Agent动作类型',
ADD COLUMN `last_goal` VARCHAR(80) NULL COMMENT '最近一次教学目标',
ADD COLUMN `last_guidance_type` VARCHAR(50) NULL COMMENT '最近一次引导类型',
ADD COLUMN `explain_count` INT DEFAULT 0 COMMENT '讲解次数',
ADD COLUMN `recommend_count` INT DEFAULT 0 COMMENT '推荐次数',
ADD COLUMN `reflect_count` INT DEFAULT 0 COMMENT '复盘次数';

ALTER TABLE `learning_event_log`
ADD COLUMN `trigger_source` VARCHAR(50) NULL COMMENT '触发来源',
ADD COLUMN `user_intent` VARCHAR(80) NULL COMMENT '用户意图',
ADD COLUMN `pedagogical_goal` VARCHAR(80) NULL COMMENT '教学目标',
ADD COLUMN `content_type` VARCHAR(50) NULL COMMENT '内容类型',
ADD COLUMN `decision_reason` VARCHAR(500) NULL COMMENT '决策原因',
ADD COLUMN `executed` TINYINT DEFAULT 1 COMMENT '是否执行',
ADD COLUMN `blocked_reason` VARCHAR(500) NULL COMMENT '阻断原因';
