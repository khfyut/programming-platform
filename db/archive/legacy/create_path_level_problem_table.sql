
-- 创建题目与关卡的多对多关联表
CREATE TABLE IF NOT EXISTS path_level_problem (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    level_id BIGINT NOT NULL COMMENT '关卡ID',
    problem_id BIGINT NOT NULL COMMENT '题目ID',
    order_num INT NOT NULL DEFAULT 1 COMMENT '排序号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_level_problem (level_id, problem_id) COMMENT '关卡-题目唯一索引',
    INDEX idx_level_id (level_id) COMMENT '关卡ID索引',
    INDEX idx_problem_id (problem_id) COMMENT '题目ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关卡-题目关联表';

-- 查看现有数据
SELECT 'path_level表数据' as info;
SELECT COUNT(*) as level_count FROM path_level;
SELECT * FROM path_level LIMIT 5;

SELECT 'path_chapter表数据' as info;
SELECT COUNT(*) as chapter_count FROM path_chapter;
SELECT * FROM path_chapter LIMIT 5;

SELECT 'learning_path表数据' as info;
SELECT COUNT(*) as path_count FROM learning_path;
SELECT * FROM learning_path LIMIT 5;

SELECT 'problem表数据' as info;
SELECT COUNT(*) as problem_count FROM problem;
SELECT language, COUNT(*) as count FROM problem GROUP BY language;
