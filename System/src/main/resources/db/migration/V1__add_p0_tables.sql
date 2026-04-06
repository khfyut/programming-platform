-- 创建学习路径相关表
CREATE TABLE IF NOT EXISTS `learning_path` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `direction` VARCHAR(100) NOT NULL,
  `language` VARCHAR(50) NOT NULL,
  `description` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `path_chapter` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `path_id` BIGINT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `order_num` INT NOT NULL,
  `description` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`path_id`) REFERENCES `learning_path`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `path_level` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `chapter_id` BIGINT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `order_num` INT NOT NULL,
  `problem_ids` TEXT,
  `knowledge_points` TEXT,
  `unlock_condition` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`chapter_id`) REFERENCES `path_chapter`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_path_progress` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `path_id` BIGINT NOT NULL,
  `current_chapter_id` BIGINT,
  `current_level_id` BIGINT,
  `completed_chapters` TEXT,
  `completed_levels` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`path_id`) REFERENCES `learning_path`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `assessment_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `language` VARCHAR(50) NOT NULL,
  `direction` VARCHAR(100) NOT NULL,
  `question` TEXT NOT NULL,
  `options` TEXT,
  `correct_answer` VARCHAR(255),
  `difficulty` INT NOT NULL,
  `knowledge_point` VARCHAR(255),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_assessment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `language` VARCHAR(50) NOT NULL,
  `direction` VARCHAR(100) NOT NULL,
  `score` INT NOT NULL,
  `result` TEXT,
  `ability_level` VARCHAR(50),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建知识点掌握度相关表
CREATE TABLE IF NOT EXISTS `knowledge_point` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `parent_id` BIGINT,
  `level` INT NOT NULL,
  `description` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `knowledge_relationship` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `source_id` BIGINT NOT NULL,
  `target_id` BIGINT NOT NULL,
  `relation_type` VARCHAR(50),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`source_id`) REFERENCES `knowledge_point`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`target_id`) REFERENCES `knowledge_point`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_knowledge_mastery` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `knowledge_id` BIGINT NOT NULL,
  `mastery_level` INT NOT NULL,
  `score` INT NOT NULL,
  `last_practice_time` DATETIME,
  `practice_count` INT DEFAULT 0,
  `correct_count` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_knowledge` (`user_id`, `knowledge_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`knowledge_id`) REFERENCES `knowledge_point`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建错题本相关表
CREATE TABLE IF NOT EXISTS `wrong_book` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `wrong_book_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `wrong_book_id` BIGINT NOT NULL,
  `problem_id` BIGINT NOT NULL,
  `submit_id` BIGINT NOT NULL,
  `code` TEXT NOT NULL,
  `language` VARCHAR(50) NOT NULL,
  `error_message` TEXT,
  `knowledge_points` TEXT,
  `review_status` INT DEFAULT 0,
  `last_review_time` DATETIME,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`wrong_book_id`) REFERENCES `wrong_book`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`problem_id`) REFERENCES `problem`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `review_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `wrong_item_id` BIGINT NOT NULL,
  `next_review_time` DATETIME NOT NULL,
  `review_count` INT DEFAULT 0,
  `status` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`wrong_item_id`) REFERENCES `wrong_book_item`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建AI对话相关表
CREATE TABLE IF NOT EXISTS `ai_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` VARCHAR(255) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `topic` VARCHAR(255),
  `status` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` VARCHAR(255) NOT NULL,
  `role` VARCHAR(50) NOT NULL,
  `content` TEXT NOT NULL,
  `related_problem_id` BIGINT,
  `related_submit_id` BIGINT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`session_id`) REFERENCES `ai_session`(`session_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_collection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `session_id` VARCHAR(255) NOT NULL,
  `content` TEXT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`session_id`) REFERENCES `ai_session`(`session_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建管理员相关表
CREATE TABLE IF NOT EXISTS `role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `description` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `code` VARCHAR(100) NOT NULL,
  `description` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  FOREIGN KEY (`role_id`) REFERENCES `role`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`permission_id`) REFERENCES `permission`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`role_id`) REFERENCES `role`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `class_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `teacher_id` BIGINT NOT NULL,
  `description` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`teacher_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_class` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `class_id` BIGINT NOT NULL,
  `role` VARCHAR(50) NOT NULL,
  `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_class` (`user_id`, `class_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`class_id`) REFERENCES `class_info`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `operation` VARCHAR(255) NOT NULL,
  `target_type` VARCHAR(100),
  `target_id` BIGINT,
  `details` TEXT,
  `ip_address` VARCHAR(50),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建内容管理相关表
CREATE TABLE IF NOT EXISTS `course` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `language` VARCHAR(50) NOT NULL,
  `path_id` BIGINT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`path_id`) REFERENCES `learning_path`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `course_chapter` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `order_num` INT NOT NULL,
  `content` TEXT,
  `video_url` VARCHAR(500),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`course_id`) REFERENCES `course`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `learning_resource` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `type` VARCHAR(100) NOT NULL,
  `url` VARCHAR(500) NOT NULL,
  `description` TEXT,
  `knowledge_points` TEXT,
  `visibility` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_solution` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `problem_id` BIGINT NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `content` TEXT NOT NULL,
  `code` TEXT,
  `language` VARCHAR(50),
  `status` INT DEFAULT 0,
  `view_count` INT DEFAULT 0,
  `like_count` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`problem_id`) REFERENCES `problem`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建系统监控相关表
CREATE TABLE IF NOT EXISTS `system_monitor` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `cpu_usage` DOUBLE,
  `memory_usage` DOUBLE,
  `disk_usage` DOUBLE,
  `network_in` DOUBLE,
  `network_out` DOUBLE,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `api_metric` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `api_path` VARCHAR(500) NOT NULL,
  `method` VARCHAR(20) NOT NULL,
  `response_time` DOUBLE NOT NULL,
  `status_code` INT NOT NULL,
  `request_count` INT DEFAULT 1,
  `error_count` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sandbox_status` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `container_id` VARCHAR(255),
  `status` VARCHAR(50) NOT NULL,
  `cpu_usage` DOUBLE,
  `memory_usage` DOUBLE,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `alert_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `metric` VARCHAR(100) NOT NULL,
  `threshold` DOUBLE NOT NULL,
  `operator` VARCHAR(10) NOT NULL,
  `enabled` INT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `alert_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `alert_config_id` BIGINT NOT NULL,
  `metric_value` DOUBLE NOT NULL,
  `threshold` DOUBLE NOT NULL,
  `status` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`alert_config_id`) REFERENCES `alert_config`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `system_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `level` VARCHAR(20) NOT NULL,
  `category` VARCHAR(100) NOT NULL,
  `message` TEXT NOT NULL,
  `details` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 为现有表添加字段
ALTER TABLE `user` ADD COLUMN `ability_profile` TEXT;
ALTER TABLE `user` ADD COLUMN `status` INT DEFAULT 1;

ALTER TABLE `problem` ADD COLUMN `chapter_id` BIGINT;
ALTER TABLE `problem` ADD COLUMN `level_id` BIGINT;

-- 插入初始数据
INSERT INTO `role` (`name`, `description`) VALUES
('admin', '系统管理员'),
('teacher', '教师'),
('assistant', '助教'),
('content_operation', '内容运营'),
('operation_admin', '运维管理员');

INSERT INTO `permission` (`name`, `code`, `description`) VALUES
('用户管理', 'user_manage', '管理用户账号'),
('题目管理', 'problem_manage', '管理编程题目'),
('学习路径管理', 'path_manage', '管理学习路径'),
('知识点管理', 'knowledge_manage', '管理知识点'),
('数据分析', 'data_analysis', '查看数据分析报表'),
('系统监控', 'system_monitor', '监控系统运行状态'),
('内容审核', 'content_audit', '审核用户内容');

-- 为管理员角色分配所有权限
INSERT INTO `role_permission` (`role_id`, `permission_id`) 
SELECT r.id, p.id FROM `role` r, `permission` p WHERE r.name = 'admin';

-- 创建管理员用户（如果不存在）
INSERT IGNORE INTO `user` (`username`, `password`, `role`, `language`, `status`) 
VALUES ('admin', '$2a$10$V5GUCA94q/77XndOaGxFjuNZ4GYAZEe3Fdial7fPN6KmVNC.fA6Nm', 1, 'java', 1);
