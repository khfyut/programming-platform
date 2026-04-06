-- 学习路径合并优化SQL脚本

-- 1. 备份现有数据
CREATE TABLE IF NOT EXISTS learning_path_backup_20260321 AS SELECT * FROM learning_path;
CREATE TABLE IF NOT EXISTS path_chapter_backup_20260321 AS SELECT * FROM path_chapter;
CREATE TABLE IF NOT EXISTS path_level_backup_20260321 AS SELECT * FROM path_level;
CREATE TABLE IF NOT EXISTS path_level_problem_backup_20260321 AS SELECT * FROM path_level_problem;

-- 2. 清理现有路径关联
DELETE FROM path_level_problem;
DELETE FROM path_level;
DELETE FROM path_chapter;

-- 3. 更新学习路径
UPDATE learning_path SET 
    name = 'Java编程基础入门',
    direction = 'language',
    language = 'Java',
    difficulty = 0
WHERE id = 1;

UPDATE learning_path SET 
    name = 'Python编程基础入门',
    direction = 'language',
    language = 'Python',
    difficulty = 0
WHERE id = 11;

UPDATE learning_path SET 
    name = '算法与数据结构基础',
    direction = 'algorithm',
    language = '通用',
    difficulty = 0
WHERE id = 2;

UPDATE learning_path SET 
    name = '算法与数据结构进阶',
    direction = 'algorithm',
    language = '通用',
    difficulty = 2
WHERE id = 12;

UPDATE learning_path SET 
    name = '后端开发基础',
    direction = 'backend',
    language = '通用',
    difficulty = 1
WHERE id = 9;

UPDATE learning_path SET 
    name = 'Java后端开发进阶',
    direction = 'backend',
    language = 'Java',
    difficulty = 2
WHERE id = 19;

-- 4. 新增Python后端开发进阶路径
INSERT INTO learning_path (name, direction, language, difficulty) 
VALUES ('Python后端开发进阶', 'backend', 'Python', 2);

-- 5. 章节和关卡重建

-- Java编程基础入门章节
INSERT INTO path_chapter (name, path_id, order_num) VALUES
('Java基础语法', 1, 1),
('Java面向对象', 1, 2),
('Java集合框架', 1, 3),
('Java基础项目实战', 1, 4);

-- Java编程基础入门关卡
INSERT INTO path_level (name, chapter_id, order_num) VALUES
('变量和数据类型', 1, 1),
('控制流语句', 1, 2),
('方法和数组', 1, 3),
('类和对象', 2, 1),
('继承和多态', 2, 2),
('接口和抽象类', 2, 3),
('List集合', 3, 1),
('Set和Map集合', 3, 2),
('集合工具类', 3, 3),
('个人名片管理系统', 4, 1),
('学生成绩管理系统', 4, 2);

-- Python编程基础入门章节
INSERT INTO path_chapter (name, path_id, order_num) VALUES
('Python基础语法', 11, 1),
('Python面向对象', 11, 2),
('Python集合', 11, 3),
('Python基础项目实战', 11, 4);

-- Python编程基础入门关卡
INSERT INTO path_level (name, chapter_id, order_num) VALUES
('变量和数据类型', 5, 1),
('控制流语句', 5, 2),
('函数和模块', 5, 3),
('类和对象', 6, 1),
('继承和多态', 6, 2),
('魔法方法', 6, 3),
('列表和元组', 7, 1),
('字典和集合', 7, 2),
('列表推导式', 7, 3),
('个人名片管理系统', 8, 1),
('学生成绩管理系统', 8, 2);

-- 算法与数据结构基础章节
INSERT INTO path_chapter (name, path_id, order_num) VALUES
('算法基础', 2, 1),
('基础数据结构', 2, 2),
('算法项目实战', 2, 3);

-- 算法与数据结构基础关卡
INSERT INTO path_level (name, chapter_id, order_num) VALUES
('算法复杂度分析', 9, 1),
('排序算法基础', 9, 2),
('搜索算法基础', 9, 3),
('数组和链表', 10, 1),
('栈和队列', 10, 2),
('哈希表', 10, 3),
('基础算法应用', 11, 1),
('数据结构应用', 11, 2);

-- 算法与数据结构进阶章节
INSERT INTO path_chapter (name, path_id, order_num) VALUES
('高级数据结构', 12, 1),
('高级算法', 12, 2),
('算法项目实战', 12, 3);

-- 算法与数据结构进阶关卡
INSERT INTO path_level (name, chapter_id, order_num) VALUES
('树和二叉树', 12, 1),
('图和图算法', 12, 2),
('堆和优先队列', 12, 3),
('动态规划', 13, 1),
('贪心算法', 13, 2),
('回溯算法', 13, 3),
('高级算法应用', 14, 1),
('复杂数据结构应用', 14, 2);

-- 后端开发基础章节
INSERT INTO path_chapter (name, path_id, order_num) VALUES
('网络基础', 9, 1),
('数据库基础', 9, 2),
('后端项目实战', 9, 3);

-- 后端开发基础关卡
INSERT INTO path_level (name, chapter_id, order_num) VALUES
('HTTP协议', 15, 1),
('TCP/IP基础', 15, 2),
('RESTful API设计', 15, 3),
('SQL基础', 16, 1),
('数据库设计', 16, 2),
('ORM基础', 16, 3),
('RESTful API开发', 17, 1),
('数据库应用', 17, 2);

-- Java后端开发进阶章节
INSERT INTO path_chapter (name, path_id, order_num) VALUES
('Spring框架', 19, 1),
('微服务架构', 19, 2),
('Java后端项目实战', 19, 3);

-- Java后端开发进阶关卡
INSERT INTO path_level (name, chapter_id, order_num) VALUES
('Spring Boot基础', 18, 1),
('Spring MVC', 18, 2),
('Spring Data JPA', 18, 3),
('Spring Cloud基础', 19, 1),
('服务注册与发现', 19, 2),
('配置中心', 19, 3),
('Spring Boot电商系统', 20, 1),
('微服务架构Demo', 20, 2);

-- Python后端开发进阶章节
-- 先获取Python后端开发进阶路径的ID
SET @python_backend_path_id = (SELECT id FROM learning_path WHERE name = 'Python后端开发进阶' AND language = 'Python');

-- 插入Python后端开发进阶章节
INSERT INTO path_chapter (name, path_id, order_num) VALUES
('Flask/Django框架', @python_backend_path_id, 1),
('微服务架构', @python_backend_path_id, 2),
('Python后端项目实战', @python_backend_path_id, 3);

-- 插入Python后端开发进阶关卡
SET @python_backend_chapter1_id = (SELECT id FROM path_chapter WHERE name = 'Flask/Django框架' AND path_id = @python_backend_path_id);
SET @python_backend_chapter2_id = (SELECT id FROM path_chapter WHERE name = '微服务架构' AND path_id = @python_backend_path_id);
SET @python_backend_chapter3_id = (SELECT id FROM path_chapter WHERE name = 'Python后端项目实战' AND path_id = @python_backend_path_id);

INSERT INTO path_level (name, chapter_id, order_num) VALUES
('Flask基础', @python_backend_chapter1_id, 1),
('Django基础', @python_backend_chapter1_id, 2),
('ORM框架', @python_backend_chapter1_id, 3),
('FastAPI基础', @python_backend_chapter2_id, 1),
('服务注册与发现', @python_backend_chapter2_id, 2),
('配置中心', @python_backend_chapter2_id, 3),
('Flask电商系统', @python_backend_chapter3_id, 1),
('FastAPI微服务Demo', @python_backend_chapter3_id, 2);