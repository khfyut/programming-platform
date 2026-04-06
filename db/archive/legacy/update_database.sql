-- 个性化在线编程与答疑系统数据库更新脚本
-- 版本: 1.0
-- 日期: 2026-03-20
-- 描述: 根据数据库结构文档生成的完整SQL语句

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 用户相关表

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `role` INTEGER NOT NULL,
  `language` VARCHAR(20) NOT NULL,
  `ability_profile` VARCHAR(255),
  `status` INTEGER NOT NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_username` (`username`),
  INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建角色表
CREATE TABLE IF NOT EXISTS `role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255),
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建权限表
CREATE TABLE IF NOT EXISTS `permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `code` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255),
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_code` (`code`),
  UNIQUE INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_role_id` (`role_id`),
  INDEX `idx_permission_id` (`permission_id`),
  FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_role_id` (`role_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 题目相关表

-- 创建题目表
CREATE TABLE IF NOT EXISTS `problem` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL,
  `content` TEXT NOT NULL,
  `difficulty` INTEGER NOT NULL,
  `language` VARCHAR(20) NOT NULL,
  `input` TEXT,
  `output` TEXT,
  `time_limit` INTEGER NOT NULL,
  `memory_limit` INTEGER NOT NULL,
  `tags` VARCHAR(255),
  `knowledge_points` VARCHAR(255),
  `hints` TEXT,
  `sample_explanation` TEXT,
  `chapter_id` BIGINT,
  `level_id` BIGINT,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_difficulty` (`difficulty`),
  INDEX `idx_language` (`language`),
  INDEX `idx_chapter_id` (`chapter_id`),
  INDEX `idx_level_id` (`level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建测试用例表
CREATE TABLE IF NOT EXISTS `test_case` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `problem_id` BIGINT NOT NULL,
  `input` TEXT NOT NULL,
  `output` TEXT NOT NULL,
  `is_sample` BOOLEAN NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_problem_id` (`problem_id`),
  FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建提交表
CREATE TABLE IF NOT EXISTS `submit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `problem_id` BIGINT NOT NULL,
  `code` TEXT NOT NULL,
  `language` VARCHAR(20) NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `score` INTEGER,
  `execution_time` INTEGER,
  `memory_usage` INTEGER,
  `error_message` TEXT,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_problem_id` (`problem_id`),
  INDEX `idx_status` (`status`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建提交测试用例结果表
CREATE TABLE IF NOT EXISTS `submit_test_case_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `submit_id` BIGINT NOT NULL,
  `test_case_id` BIGINT NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `actual_output` TEXT,
  `execution_time` INTEGER,
  `memory_usage` INTEGER,
  PRIMARY KEY (`id`),
  INDEX `idx_submit_id` (`submit_id`),
  INDEX `idx_test_case_id` (`test_case_id`),
  FOREIGN KEY (`submit_id`) REFERENCES `submit` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`test_case_id`) REFERENCES `test_case` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 学习路径相关表

-- 创建学习路径表
CREATE TABLE IF NOT EXISTS `learning_path` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `direction` VARCHAR(50) NOT NULL,
  `language` VARCHAR(20) NOT NULL,
  `description` TEXT,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_language` (`language`),
  INDEX `idx_direction` (`direction`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建路径章节表
CREATE TABLE IF NOT EXISTS `path_chapter` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `path_id` BIGINT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `order_num` INTEGER NOT NULL,
  `description` TEXT,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_path_id` (`path_id`),
  FOREIGN KEY (`path_id`) REFERENCES `learning_path` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建路径关卡表
CREATE TABLE IF NOT EXISTS `path_level` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `chapter_id` BIGINT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `order_num` INTEGER NOT NULL,
  `problem_ids` VARCHAR(255),
  `knowledge_points` VARCHAR(255),
  `unlock_condition` VARCHAR(255),
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_chapter_id` (`chapter_id`),
  FOREIGN KEY (`chapter_id`) REFERENCES `path_chapter` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户学习路径进度表
CREATE TABLE IF NOT EXISTS `user_path_progress` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `path_id` BIGINT NOT NULL,
  `current_chapter_id` BIGINT,
  `current_level_id` BIGINT,
  `completed_chapters` VARCHAR(255),
  `completed_levels` VARCHAR(255),
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_path_id` (`path_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`path_id`) REFERENCES `learning_path` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`current_chapter_id`) REFERENCES `path_chapter` (`id`) ON DELETE SET NULL,
  FOREIGN KEY (`current_level_id`) REFERENCES `path_level` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 知识掌握度相关表

-- 创建知识点表
CREATE TABLE IF NOT EXISTS `knowledge_point` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `parent_id` BIGINT,
  `level` INTEGER NOT NULL,
  `description` TEXT,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_parent_id` (`parent_id`),
  FOREIGN KEY (`parent_id`) REFERENCES `knowledge_point` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建知识点关系表
CREATE TABLE IF NOT EXISTS `knowledge_relationship` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `source_id` BIGINT NOT NULL,
  `target_id` BIGINT NOT NULL,
  `relationship_type` VARCHAR(50) NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_source_id` (`source_id`),
  INDEX `idx_target_id` (`target_id`),
  FOREIGN KEY (`source_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`target_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户知识掌握度表
CREATE TABLE IF NOT EXISTS `user_knowledge_mastery` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `knowledge_id` BIGINT NOT NULL,
  `mastery_level` INTEGER NOT NULL,
  `score` INTEGER,
  `last_practice_time` DATETIME,
  `practice_count` INTEGER NOT NULL,
  `correct_count` INTEGER NOT NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_knowledge_id` (`knowledge_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`knowledge_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 错题本相关表

-- 创建错题本主表
CREATE TABLE IF NOT EXISTS `wrong_book` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_user_id` (`user_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建错题项表
CREATE TABLE IF NOT EXISTS `wrong_book_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `wrong_book_id` BIGINT NOT NULL,
  `problem_id` BIGINT NOT NULL,
  `submit_id` BIGINT,
  `code` TEXT,
  `language` VARCHAR(20),
  `error_message` TEXT,
  `knowledge_points` VARCHAR(255),
  `review_status` INTEGER NOT NULL,
  `last_review_time` DATETIME,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_wrong_book_id` (`wrong_book_id`),
  INDEX `idx_problem_id` (`problem_id`),
  FOREIGN KEY (`wrong_book_id`) REFERENCES `wrong_book` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`submit_id`) REFERENCES `submit` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建复习计划表
CREATE TABLE IF NOT EXISTS `review_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `wrong_item_id` BIGINT NOT NULL,
  `next_review_time` DATETIME NOT NULL,
  `review_count` INTEGER NOT NULL,
  `status` INTEGER NOT NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_wrong_item_id` (`wrong_item_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`wrong_item_id`) REFERENCES `wrong_book_item` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. AI相关表

-- 创建AI会话表
CREATE TABLE IF NOT EXISTS `ai_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` VARCHAR(100) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `topic` VARCHAR(255),
  `status` INTEGER NOT NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_session_id` (`session_id`),
  INDEX `idx_user_id` (`user_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建AI消息表
CREATE TABLE IF NOT EXISTS `ai_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` VARCHAR(100) NOT NULL,
  `role` VARCHAR(20) NOT NULL,
  `content` TEXT NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_session_id` (`session_id`),
  FOREIGN KEY (`session_id`) REFERENCES `ai_session` (`session_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建AI收藏表
CREATE TABLE IF NOT EXISTS `ai_collection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `session_id` VARCHAR(100) NOT NULL,
  `message_id` BIGINT NOT NULL,
  `content` TEXT NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_session_id` (`session_id`),
  INDEX `idx_message_id` (`message_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`session_id`) REFERENCES `ai_session` (`session_id`) ON DELETE CASCADE,
  FOREIGN KEY (`message_id`) REFERENCES `ai_message` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 其他表

-- 创建班级信息表
CREATE TABLE IF NOT EXISTS `class_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `description` TEXT,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户班级关联表
CREATE TABLE IF NOT EXISTS `user_class` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `class_id` BIGINT NOT NULL,
  `role` VARCHAR(20) NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_class_id` (`class_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建审计日志表
CREATE TABLE IF NOT EXISTS `audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT,
  `operation` VARCHAR(100) NOT NULL,
  `resource_type` VARCHAR(50) NOT NULL,
  `resource_id` BIGINT,
  `detail` TEXT,
  `ip_address` VARCHAR(50),
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建系统监控表
CREATE TABLE IF NOT EXISTS `system_monitor` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `metric_name` VARCHAR(100) NOT NULL,
  `metric_value` DOUBLE NOT NULL,
  `metric_unit` VARCHAR(20),
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_metric_name` (`metric_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建API指标表
CREATE TABLE IF NOT EXISTS `api_metric` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `api_path` VARCHAR(255) NOT NULL,
  `method` VARCHAR(10) NOT NULL,
  `response_time` INTEGER NOT NULL,
  `status_code` INTEGER NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_api_path` (`api_path`),
  INDEX `idx_method` (`method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建沙箱状态表
CREATE TABLE IF NOT EXISTS `sandbox_status` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `container_id` VARCHAR(100),
  `status` VARCHAR(20) NOT NULL,
  `cpu_usage` DOUBLE,
  `memory_usage` DOUBLE,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_container_id` (`container_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建告警配置表
CREATE TABLE IF NOT EXISTS `alert_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `metric_name` VARCHAR(100) NOT NULL,
  `threshold` DOUBLE NOT NULL,
  `operator` VARCHAR(10) NOT NULL,
  `alert_level` VARCHAR(20) NOT NULL,
  `enabled` BOOLEAN NOT NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_metric_name` (`metric_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建告警记录表
CREATE TABLE IF NOT EXISTS `alert_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `config_id` BIGINT NOT NULL,
  `metric_value` DOUBLE NOT NULL,
  `message` TEXT NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_config_id` (`config_id`),
  FOREIGN KEY (`config_id`) REFERENCES `alert_config` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建系统日志表
CREATE TABLE IF NOT EXISTS `system_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `level` VARCHAR(20) NOT NULL,
  `category` VARCHAR(50) NOT NULL,
  `message` TEXT NOT NULL,
  `stack_trace` TEXT,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_level` (`level`),
  INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建课程表
CREATE TABLE IF NOT EXISTS `course` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `description` TEXT,
  `language` VARCHAR(20) NOT NULL,
  `difficulty` VARCHAR(20) NOT NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_language` (`language`),
  INDEX `idx_difficulty` (`difficulty`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建课程章节表
CREATE TABLE IF NOT EXISTS `course_chapter` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `order_num` INTEGER NOT NULL,
  `description` TEXT,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_course_id` (`course_id`),
  FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建学习资源表
CREATE TABLE IF NOT EXISTS `learning_resource` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL,
  `type` VARCHAR(50) NOT NULL,
  `url` VARCHAR(500) NOT NULL,
  `description` TEXT,
  `knowledge_points` VARCHAR(255),
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户解决方案表
CREATE TABLE IF NOT EXISTS `user_solution` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `problem_id` BIGINT NOT NULL,
  `code` TEXT NOT NULL,
  `language` VARCHAR(20) NOT NULL,
  `is_optimized` BOOLEAN NOT NULL,
  `likes` INTEGER NOT NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_problem_id` (`problem_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建测评问题表
CREATE TABLE IF NOT EXISTS `assessment_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `question` TEXT NOT NULL,
  `options` TEXT NOT NULL,
  `correct_answer` VARCHAR(100) NOT NULL,
  `difficulty` INTEGER NOT NULL,
  `knowledge_points` VARCHAR(255),
  `language` VARCHAR(20) NOT NULL,
  `direction` VARCHAR(50) NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_language` (`language`),
  INDEX `idx_direction` (`direction`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户测评表
CREATE TABLE IF NOT EXISTS `user_assessment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `language` VARCHAR(20) NOT NULL,
  `direction` VARCHAR(50) NOT NULL,
  `score` INTEGER NOT NULL,
  `level` VARCHAR(20) NOT NULL,
  `suggestions` TEXT,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_language` (`language`),
  INDEX `idx_direction` (`direction`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建学习记录表
CREATE TABLE IF NOT EXISTS `learn_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `resource_type` VARCHAR(50) NOT NULL,
  `resource_id` BIGINT NOT NULL,
  `duration` INTEGER,
  `completed` BOOLEAN NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_resource_type` (`resource_type`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入初始数据

-- 插入默认角色
INSERT IGNORE INTO `role` (`name`, `description`, `create_time`, `update_time`) VALUES
('admin', '管理员', NOW(), NOW()),
('user', '普通用户', NOW(), NOW());

-- 插入默认权限
INSERT IGNORE INTO `permission` (`name`, `code`, `description`, `create_time`, `update_time`) VALUES
('用户管理', 'user:manage', '管理用户', NOW(), NOW()),
('题目管理', 'problem:manage', '管理题目', NOW(), NOW()),
('提交管理', 'submit:manage', '管理提交', NOW(), NOW()),
('学习路径管理', 'path:manage', '管理学习路径', NOW(), NOW()),
('系统管理', 'system:manage', '管理系统', NOW(), NOW());

-- 插入角色权限关联
INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`, `create_time`) VALUES
(1, 1, NOW()),
(1, 2, NOW()),
(1, 3, NOW()),
(1, 4, NOW()),
(1, 5, NOW());

-- 插入默认管理员用户
INSERT IGNORE INTO `user` (`username`, `password`, `role`, `language`, `ability_profile`, `status`, `create_time`, `update_time`) VALUES
('admin', '123456', 1, 'java', '{}', 0, NOW(), NOW());

-- 插入用户角色关联
INSERT IGNORE INTO `user_role` (`user_id`, `role_id`, `create_time`) VALUES
(1, 1, NOW());

SET FOREIGN_KEY_CHECKS = 1;

-- 优化表
OPTIMIZE TABLE `user`, `role`, `permission`, `role_permission`, `user_role`,
`problem`, `test_case`, `submit`, `submit_test_case_result`,
`learning_path`, `path_chapter`, `path_level`, `user_path_progress`,
`knowledge_point`, `knowledge_relationship`, `user_knowledge_mastery`,
`wrong_book`, `wrong_book_item`, `review_plan`,
`ai_session`, `ai_message`, `ai_collection`,
`class_info`, `user_class`, `audit_log`, `system_monitor`, `api_metric`,
`sandbox_status`, `alert_config`, `alert_record`, `system_log`,
`course`, `course_chapter`, `learning_resource`, `user_solution`,
`assessment_question`, `user_assessment`, `learn_record`;

-- 完成
SELECT '数据库更新完成' AS message;
