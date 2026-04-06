-- =====================================================
-- 学习路径体系重构 - 数据库表结构设计
-- =====================================================

-- 创建知识模块表
CREATE TABLE IF NOT EXISTS knowledge_module (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '知识模块ID',
    name VARCHAR(255) NOT NULL COMMENT '模块名称',
    description TEXT COMMENT '模块描述',
    parent_id BIGINT COMMENT '父模块ID，用于建立层级关系',
    level INT NOT NULL COMMENT '层级：1=基础层，2=方向层，3=进阶层',
    language VARCHAR(50) COMMENT '编程语言：java/python/all',
    direction VARCHAR(50) COMMENT '学习方向：algorithm/data-structure/backend/all',
    difficulty INT DEFAULT 1 COMMENT '难度：1=入门，2=基础，3=进阶，4=高级，5=专家',
    order_num INT NOT NULL DEFAULT 0 COMMENT '排序号',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_parent_id (parent_id),
    INDEX idx_level (level),
    INDEX idx_language (language),
    INDEX idx_direction (direction),
    INDEX idx_difficulty (difficulty),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识模块表';

-- 创建模块依赖表
CREATE TABLE IF NOT EXISTS module_dependency (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '依赖关系ID',
    module_id BIGINT NOT NULL COMMENT '当前模块ID',
    prerequisite_module_id BIGINT NOT NULL COMMENT '前置模块ID',
    dependency_type VARCHAR(20) DEFAULT 'required' COMMENT '依赖类型：required=必须，optional=可选',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_module_id (module_id),
    INDEX idx_prerequisite_id (prerequisite_module_id),
    UNIQUE KEY uk_module_prerequisite (module_id, prerequisite_module_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模块依赖表';

-- 修改学习路径表，添加知识模块引用
ALTER TABLE learning_path 
ADD COLUMN is_template TINYINT(1) DEFAULT 0 COMMENT '是否为模板路径',
ADD COLUMN knowledge_module_ids TEXT COMMENT '包含的知识模块ID列表（JSON格式）',
ADD COLUMN difficulty INT DEFAULT 1 COMMENT '难度：1=入门，2=基础，3=进阶，4=高级，5=专家';

-- 创建学习路径与知识模块关联表
CREATE TABLE IF NOT EXISTS learning_path_module (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    path_id BIGINT NOT NULL COMMENT '学习路径ID',
    module_id BIGINT NOT NULL COMMENT '知识模块ID',
    order_num INT NOT NULL DEFAULT 0 COMMENT '在路径中的排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_path_id (path_id),
    INDEX idx_module_id (module_id),
    UNIQUE KEY uk_path_module (path_id, module_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习路径与知识模块关联表';

-- =====================================================
-- 插入基础知识模块数据
-- =====================================================

-- Java基础知识模块
INSERT INTO knowledge_module (name, description, parent_id, level, language, direction, difficulty, order_num) VALUES
-- 第一层：基础知识层
('Java基础语法', 'Java语言的基础语法知识', NULL, 1, 'java', 'all', 1, 1),
('Java面向对象编程', 'Java面向对象编程核心概念', NULL, 1, 'java', 'all', 2, 2),
('Java异常处理', 'Java异常处理机制', NULL, 1, 'java', 'all', 2, 3),
('Java集合框架', 'Java常用集合类', NULL, 1, 'java', 'all', 2, 4),

-- Java基础语法的子模块
('变量与数据类型', '学习Java变量定义和基本数据类型', 1, 1, 'java', 'all', 1, 1),
('运算符与表达式', '学习Java各类运算符', 1, 1, 'java', 'all', 1, 2),
('流程控制语句', '学习条件语句和循环语句', 1, 1, 'java', 'all', 1, 3),
('数组基础', '学习数组的定义和使用', 1, 1, 'java', 'all', 1, 4),

-- Java面向对象的子模块
('类与对象基础', '理解类和对象的概念', 2, 1, 'java', 'all', 1, 1),
('封装与继承', '学习封装和继承特性', 2, 1, 'java', 'all', 2, 2),
('多态与接口', '学习多态和接口', 2, 1, 'java', 'all', 2, 3),
('抽象类与内部类', '学习抽象类和内部类', 2, 1, 'java', 'all', 2, 4),

-- 第二层：方向核心层 - 算法方向
('基础算法', '排序、查找等基础算法', NULL, 2, 'java', 'algorithm', 2, 1),
('高级算法', '动态规划、贪心等高级算法', NULL, 2, 'java', 'algorithm', 3, 2),
('算法复杂度分析', '算法时间和空间复杂度分析', NULL, 2, 'java', 'algorithm', 2, 3),

-- 第二层：方向核心层 - 数据结构方向
('线性数据结构', '数组、链表、栈、队列', NULL, 2, 'java', 'data-structure', 2, 1),
('树状数据结构', '二叉树、B树、红黑树', NULL, 2, 'java', 'data-structure', 3, 2),
('图结构', '图的表示、遍历、最短路径', NULL, 2, 'java', 'data-structure', 3, 3),

-- 第二层：方向核心层 - 后端方向
('Spring Boot基础', 'Spring Boot框架入门', NULL, 2, 'java', 'backend', 2, 1),
('数据库操作', 'JDBC和MyBatis', NULL, 2, 'java', 'backend', 2, 2),
('API开发与安全', 'RESTful API和JWT认证', NULL, 2, 'java', 'backend', 3, 3),
('系统架构基础', '分层架构和缓存策略', NULL, 2, 'java', 'backend', 3, 4),

-- 第三层：专业进阶层
('微服务架构', 'Spring Cloud和微服务', NULL, 3, 'java', 'backend', 4, 1),
('分布式系统', '分布式事务和消息队列', NULL, 3, 'java', 'backend', 4, 2),
('性能优化', 'JVM调优和SQL优化', NULL, 3, 'java', 'all', 4, 3),
('架构设计', '高可用和高并发架构', NULL, 3, 'java', 'backend', 5, 4);

-- Python基础知识模块
INSERT INTO knowledge_module (name, description, parent_id, level, language, direction, difficulty, order_num) VALUES
-- 第一层：基础知识层
('Python基础语法', 'Python语言的基础语法知识', NULL, 1, 'python', 'all', 1, 1),
('Python面向对象编程', 'Python面向对象编程核心概念', NULL, 1, 'python', 'all', 2, 2),
('Python异常处理', 'Python异常处理机制', NULL, 1, 'python', 'all', 2, 3),
('Python数据结构', 'Python内置数据结构', NULL, 1, 'python', 'all', 2, 4);

-- 建立模块依赖关系
INSERT INTO module_dependency (module_id, prerequisite_module_id, dependency_type) VALUES
-- Java基础语法的依赖
(2, 1, 'required'),  -- 面向对象依赖基础语法
(3, 1, 'required'),  -- 异常处理依赖基础语法
(4, 2, 'required'),  -- 集合框架依赖面向对象

-- 算法方向的依赖
(13, 4, 'required'),  -- 基础算法依赖集合框架
(14, 13, 'required'), -- 高级算法依赖基础算法
(15, 13, 'required'), -- 复杂度分析依赖基础算法

-- 数据结构方向的依赖
(16, 4, 'required'),  -- 线性结构依赖集合框架
(17, 16, 'required'), -- 树状结构依赖线性结构
(18, 17, 'required'), -- 图结构依赖树状结构

-- 后端方向的依赖
(19, 4, 'required'),  -- Spring Boot依赖集合框架
(20, 19, 'required'), -- 数据库操作依赖Spring Boot
(21, 20, 'required'), -- API开发依赖数据库操作
(22, 21, 'required'), -- 系统架构依赖API开发

-- 进阶层的依赖
(23, 22, 'required'), -- 微服务依赖系统架构
(24, 23, 'required'), -- 分布式系统依赖微服务
(25, 22, 'required'), -- 性能优化依赖系统架构
(26, 24, 'required'); -- 架构设计依赖分布式系统

-- =====================================================
-- 创建8条新的学习路径
-- =====================================================

-- 清空旧的学习路径相关数据（先备份！）
-- 注意：实际执行前请确保已备份数据库

-- Java学习路径
INSERT INTO learning_path (name, direction, language, description, difficulty, is_template) VALUES
('Java编程基础入门', 'algorithm', 'java', '从零开始学习Java编程语言，适合零基础学习者', 1, 1),
('Java算法与数据结构进阶', 'algorithm', 'java', '深入学习Java算法和数据结构，提升编程能力', 3, 1),
('Java后端开发实战', 'backend', 'java', '学习Java后端开发技术，包括Spring Boot、数据库等', 3, 1),
('Java技术专家之路', 'backend', 'java', '研究Java前沿技术，成为技术专家', 5, 1);

-- Python学习路径
INSERT INTO learning_path (name, direction, language, description, difficulty, is_template) VALUES
('Python编程基础入门', 'algorithm', 'python', '从零开始学习Python编程语言，适合零基础学习者', 1, 1),
('Python算法与数据结构进阶', 'algorithm', 'python', '深入学习Python算法和数据结构，提升编程能力', 3, 1),
('Python后端开发实战', 'backend', 'python', '学习Python后端开发技术，包括Django、Flask等', 3, 1),
('Python技术专家之路', 'backend', 'python', '研究Python前沿技术，成为技术专家', 5, 1);

-- =====================================================
-- 完成！
-- =====================================================
