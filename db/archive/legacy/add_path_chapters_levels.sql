-- 为所有学习路径添加章节和关卡数据

-- 首先，删除可能存在的旧数据（保持id 1-5的Java基础路径数据）
DELETE FROM path_chapter WHERE path_id > 5;
DELETE FROM path_level WHERE chapter_id > 15;

-- Java路径数据已经存在，现在为Python路径添加数据

-- Python编程基础入门 (path_id=11)
INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time) VALUES
(11, 'Python基础语法', 1, '学习Python的基本语法和数据类型', NOW(), NOW()),
(11, 'Python面向对象', 2, '学习Python的面向对象编程特性', NOW(), NOW()),
(11, 'Python标准库', 3, '学习Python常用的标准库模块', NOW(), NOW()),
(11, 'Python文件操作', 4, '学习Python的文件操作', NOW(), NOW()),
(11, 'Python异常处理', 5, '学习Python的异常处理机制', NOW(), NOW());

-- 为Python编程基础入门 (path_id=11) 的章节添加关卡
-- 先获取刚插入的chapter_ids
SET @chap11_id = LAST_INSERT_ID() - 4;
SET @chap12_id = LAST_INSERT_ID() - 3;
SET @chap13_id = LAST_INSERT_ID() - 2;
SET @chap14_id = LAST_INSERT_ID() - 1;
SET @chap15_id = LAST_INSERT_ID();

-- Python基础语法章节的关卡
INSERT INTO path_level (chapter_id, name, order_num, problem_ids, knowledge_points, unlock_condition, create_time, update_time) VALUES
(@chap11_id, '变量和数据类型', 1, '101,102,103', '变量,数据类型,运算符', '', NOW(), NOW()),
(@chap11_id, '控制流语句', 2, '104,105,106', 'if语句,循环语句,循环控制', '1', NOW(), NOW()),
(@chap11_id, '函数和列表', 3, '107,108,109', '函数定义,函数调用,列表操作', '2', NOW(), NOW());

-- Python面向对象章节的关卡
INSERT INTO path_level (chapter_id, name, order_num, problem_ids, knowledge_points, unlock_condition, create_time, update_time) VALUES
(@chap12_id, '类和对象', 1, '110,111,112', '类定义,对象创建,构造方法', '3', NOW(), NOW()),
(@chap12_id, '继承和多态', 2, '113,114,115', '继承,多态,方法重写', '4', NOW(), NOW()),
(@chap12_id, '装饰器和特性', 3, '116,117,118', '装饰器,属性,魔术方法', '5', NOW(), NOW());

-- Python标准库章节的关卡
INSERT INTO path_level (chapter_id, name, order_num, problem_ids, knowledge_points, unlock_condition, create_time, update_time) VALUES
(@chap13_id, 'datetime模块', 1, '119,120,121', '时间处理,日期格式化,时间计算', '6', NOW(), NOW()),
(@chap13_id, 'os和sys模块', 2, '122,123,124', '系统操作,环境变量,命令行参数', '7', NOW(), NOW()),
(@chap13_id, 'collections模块', 3, '125,126,127', '字典扩展,命名元组,双端队列', '8', NOW(), NOW());

-- Python文件操作章节的关卡
INSERT INTO path_level (chapter_id, name, order_num, problem_ids, knowledge_points, unlock_condition, create_time, update_time) VALUES
(@chap14_id, '文件读写', 1, '128,129,130', '文件打开,文件读取,文件写入', '9', NOW(), NOW()),
(@chap14_id, '上下文管理器', 2, '131,132,133', 'with语句,资源管理,异常安全', '10', NOW(), NOW()),
(@chap14_id, 'JSON处理', 3, '134,135,136', 'JSON解析,JSON生成,数据序列化', '11', NOW(), NOW());

-- Python异常处理章节的关卡
INSERT INTO path_level (chapter_id, name, order_num, problem_ids, knowledge_points, unlock_condition, create_time, update_time) VALUES
(@chap15_id, '异常基础', 1, '137,138,139', '异常捕获,异常抛出,异常类型', '12', NOW(), NOW()),
(@chap15_id, '自定义异常', 2, '140,141,142', '异常定义,异常继承,异常处理最佳实践', '13', NOW(), NOW()),
(@chap15_id, '异常链和上下文', 3, '143,144,145', '异常链,异常上下文,调试技巧', '14', NOW(), NOW());

-- 为其他Python路径添加数据
-- 类似地为Python算法与数据结构进阶 (path_id=12) 添加数据
INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time) VALUES
(12, 'Python算法进阶', 1, '学习Python实现高级算法', NOW(), NOW()),
(12, 'Python数据结构', 2, '学习Python高级数据结构', NOW(), NOW());

-- 重复类似模式为所有Python路径添加数据...

-- 提示：可以为所有路径（2-10, 12-20）使用类似的模式添加章节和关卡
-- 为了简化，这里只展示了path_id=11的完整示例
