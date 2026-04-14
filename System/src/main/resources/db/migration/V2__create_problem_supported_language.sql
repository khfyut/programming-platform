CREATE TABLE IF NOT EXISTS `problem_supported_language` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `problem_id` BIGINT NOT NULL,
  `language_code` VARCHAR(50) NOT NULL,
  `is_default` TINYINT(1) NOT NULL DEFAULT 0,
  `starter_code` MEDIUMTEXT NULL,
  `starter_filename` VARCHAR(255) NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `sort_order` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_problem_language` (`problem_id`, `language_code`),
  KEY `idx_problem_default` (`problem_id`, `is_default`),
  KEY `idx_problem_language_status` (`problem_id`, `status`),
  KEY `idx_language_code` (`language_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
