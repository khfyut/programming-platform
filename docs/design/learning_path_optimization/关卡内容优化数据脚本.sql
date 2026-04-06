-- ============================================
-- 学习路径关卡内容优化数据脚本
-- 功能：更新关卡内容，绑定题目，完善学习路径结构
-- ============================================

-- 先清空现有的关卡-题目关联数据
DELETE FROM path_level_problem;

-- 更新关卡内容，添加知识点和题目关联
-- 路径1：Java编程基础入门

-- 章节1：Java基础语法
UPDATE path_level SET 
    knowledge_points = '变量声明,数据类型,赋值操作',
    problem_ids = '1,2,3'
WHERE id = 1; -- 变量和数据类型

UPDATE path_level SET 
    knowledge_points = 'if-else条件判断,逻辑判断,循环控制',
    problem_ids = '4,5,6'
WHERE id = 2; -- 控制流语句

UPDATE path_level SET 
    knowledge_points = '方法定义,参数传递,数组操作,递归',
    problem_ids = '7,8,9'
WHERE id = 3; -- 方法和数组

-- 章节2：Java面向对象
UPDATE path_level SET 
    knowledge_points = '类定义,属性,方法,构造方法,封装',
    problem_ids = '10'
WHERE id = 4; -- 类和对象

UPDATE path_level SET 
    knowledge_points = 'extends继承,方法重写,super关键字,多态',
    problem_ids = '11'
WHERE id = 5; -- 继承和多态

UPDATE path_level SET 
    knowledge_points = 'interface定义,implements实现,抽象方法,抽象类',
    problem_ids = '12'
WHERE id = 6; -- 接口和抽象类

-- 章节3：Java集合框架
UPDATE path_level SET 
    knowledge_points = 'ArrayList,LinkedList,泛型,List接口',
    problem_ids = '13'
WHERE id = 7; -- List集合

UPDATE path_level SET 
    knowledge_points = 'HashSet,TreeSet,HashMap,TreeMap,Set,Map',
    problem_ids = '14'
WHERE id = 8; -- Set和Map集合

UPDATE path_level SET 
    knowledge_points = 'Collections工具类,排序,迭代器',
    problem_ids = '13,14'
WHERE id = 9; -- 集合工具类

-- 章节4：Java基础项目实战
UPDATE path_level SET 
    knowledge_points = '类设计,集合使用,文件操作,综合应用',
    problem_ids = '15'
WHERE id = 10; -- 个人名片管理系统

UPDATE path_level SET 
    knowledge_points = '多类协作,数据管理,业务逻辑,综合应用',
    problem_ids = '15'
WHERE id = 11; -- 学生成绩管理系统

-- 路径2：Python编程基础入门

-- 章节1：Python基础语法
UPDATE path_level SET 
    knowledge_points = '变量,基本数据类型,类型转换,字符串',
    problem_ids = '1,2,3'
WHERE id = 12; -- 变量和数据类型

UPDATE path_level SET 
    knowledge_points = 'if-elif-else,for循环,while循环,range',
    problem_ids = '4,5,6'
WHERE id = 13; -- 控制流语句

UPDATE path_level SET 
    knowledge_points = '函数定义,def,return,参数,模块导入',
    problem_ids = '7,8,9'
WHERE id = 14; -- 函数和模块

-- 章节2：Python面向对象
UPDATE path_level SET 
    knowledge_points = 'class,__init__,self,属性,方法',
    problem_ids = '10'
WHERE id = 15; -- 类和对象

UPDATE path_level SET 
    knowledge_points = '继承,方法重写,super(),多态',
    problem_ids = '11'
WHERE id = 16; -- 继承和多态

UPDATE path_level SET 
    knowledge_points = '__str__,__repr__,__eq__,魔法方法,运算符重载',
    problem_ids = '10'
WHERE id = 17; -- 魔法方法

-- 章节3：Python集合
UPDATE path_level SET 
    knowledge_points = 'list,tuple,切片,列表推导式',
    problem_ids = '8,13'
WHERE id = 18; -- 列表和元组

UPDATE path_level SET 
    knowledge_points = 'dict,set,字典推导式,映射,集合',
    problem_ids = '14'
WHERE id = 19; -- 字典和集合

UPDATE path_level SET 
    knowledge_points = '列表推导式,生成器表达式,Pythonic',
    problem_ids = '8'
WHERE id = 20; -- 列表推导式

-- 章节4：Python基础项目实战
UPDATE path_level SET 
    knowledge_points = '类设计,文件操作,数据结构,JSON,综合应用',
    problem_ids = '15'
WHERE id = 21; -- 个人名片管理系统

UPDATE path_level SET 
    knowledge_points = '类协作,数据管理,综合应用',
    problem_ids = '15'
WHERE id = 22; -- 学生成绩管理系统

-- 路径3：算法与数据结构基础

-- 章节1：算法基础
UPDATE path_level SET 
    knowledge_points = '时间复杂度,空间复杂度,大O表示法',
    problem_ids = '16'
WHERE id = 23; -- 算法复杂度分析

UPDATE path_level SET 
    knowledge_points = '冒泡排序,选择排序,插入排序,排序算法',
    problem_ids = '16'
WHERE id = 24; -- 排序算法基础

UPDATE path_level SET 
    knowledge_points = '线性搜索,二分搜索,搜索算法',
    problem_ids = '17'
WHERE id = 25; -- 搜索算法基础

-- 章节2：基础数据结构
UPDATE path_level SET 
    knowledge_points = '数组,链表,单链表,双链表,线性结构',
    problem_ids = '9,18'
WHERE id = 26; -- 数组和链表

UPDATE path_level SET 
    knowledge_points = '栈,队列,循环队列,后进先出,先进先出',
    problem_ids = '19'
WHERE id = 27; -- 栈和队列

UPDATE path_level SET 
    knowledge_points = '哈希表,哈希函数,冲突解决,散列表',
    problem_ids = '1,14'
WHERE id = 28; -- 哈希表

-- 章节3：算法项目实战
UPDATE path_level SET 
    knowledge_points = '排序,搜索,综合应用,算法实践',
    problem_ids = '16,17'
WHERE id = 29; -- 基础算法应用

UPDATE path_level SET 
    knowledge_points = '栈,队列,哈希表,综合应用,数据结构实践',
    problem_ids = '19,14'
WHERE id = 30; -- 数据结构应用

-- 路径4：算法与数据结构进阶

-- 章节1：高级数据结构
UPDATE path_level SET 
    knowledge_points = '二叉树,BST,树的遍历,前序,中序,后序',
    problem_ids = '20'
WHERE id = 31; -- 树和二叉树

UPDATE path_level SET 
    knowledge_points = '图,DFS,BFS,最短路径,图算法',
    problem_ids = '20'
WHERE id = 32; -- 图和图算法

UPDATE path_level SET 
    knowledge_points = '堆,优先队列,最大堆,最小堆,堆排序',
    problem_ids = '16'
WHERE id = 33; -- 堆和优先队列

-- 章节2：高级算法
UPDATE path_level SET 
    knowledge_points = '动态规划,DP,记忆化搜索,状态转移',
    problem_ids = '21'
WHERE id = 34; -- 动态规划

UPDATE path_level SET 
    knowledge_points = '贪心算法,贪心策略,最优子结构',
    problem_ids = '21'
WHERE id = 35; -- 贪心算法

UPDATE path_level SET 
    knowledge_points = '回溯算法,递归回溯,剪枝策略',
    problem_ids = '21'
WHERE id = 36; -- 回溯算法

-- 章节3：算法项目实战
UPDATE path_level SET 
    knowledge_points = 'DP,贪心,回溯,综合应用,高级算法实践',
    problem_ids = '21'
WHERE id = 37; -- 高级算法应用

UPDATE path_level SET 
    knowledge_points = '树,图,堆,综合应用,高级数据结构实践',
    problem_ids = '20'
WHERE id = 38; -- 复杂数据结构应用

-- 路径5：后端开发基础

-- 章节1：网络基础
UPDATE path_level SET 
    knowledge_points = 'HTTP协议,HTTP方法,状态码,请求响应',
    problem_ids = '22'
WHERE id = 39; -- HTTP协议

UPDATE path_level SET 
    knowledge_points = 'TCP/IP,Socket编程,网络通信',
    problem_ids = '22'
WHERE id = 40; -- TCP/IP基础

UPDATE path_level SET 
    knowledge_points = 'RESTful,API设计,版本控制,REST原则',
    problem_ids = '22'
WHERE id = 41; -- RESTful API设计

-- 章节2：数据库基础
UPDATE path_level SET 
    knowledge_points = 'SQL,SELECT,INSERT,UPDATE,DELETE,JOIN',
    problem_ids = '23'
WHERE id = 42; -- SQL基础

UPDATE path_level SET 
    knowledge_points = '数据库设计,范式,ER图,表设计',
    problem_ids = '23'
WHERE id = 43; -- 数据库设计

UPDATE path_level SET 
    knowledge_points = 'ORM,实体映射,CRUD,JPA,Hibernate',
    problem_ids = '23'
WHERE id = 44; -- ORM基础

-- 章节3：后端项目实战
UPDATE path_level SET 
    knowledge_points = 'API设计,数据验证,错误处理,RESTful实现',
    problem_ids = '22'
WHERE id = 45; -- RESTful API开发

UPDATE path_level SET 
    knowledge_points = '数据库连接,事务管理,查询优化',
    problem_ids = '23'
WHERE id = 46; -- 数据库应用

-- 路径6：Java后端开发进阶

-- 章节1：Spring框架
UPDATE path_level SET 
    knowledge_points = 'Spring Boot,自动配置,起步依赖,配置文件',
    problem_ids = '22'
WHERE id = 47; -- Spring Boot基础

UPDATE path_level SET 
    knowledge_points = 'Spring MVC,Controller,RequestMapping,参数绑定',
    problem_ids = '22'
WHERE id = 48; -- Spring MVC

UPDATE path_level SET 
    knowledge_points = 'Spring Data JPA,Repository,查询方法,关联映射',
    problem_ids = '23'
WHERE id = 49; -- Spring Data JPA

-- 章节2：微服务架构
UPDATE path_level SET 
    knowledge_points = 'Spring Cloud,微服务,服务拆分,通信方式',
    problem_ids = '22'
WHERE id = 50; -- Spring Cloud基础

UPDATE path_level SET 
    knowledge_points = 'Eureka,服务注册,服务发现,负载均衡',
    problem_ids = '22'
WHERE id = 51; -- 服务注册与发现

UPDATE path_level SET 
    knowledge_points = 'Config Server,配置中心,配置刷新,环境管理',
    problem_ids = '22'
WHERE id = 52; -- 配置中心

-- 章节3：Java后端项目实战
UPDATE path_level SET 
    knowledge_points = '业务逻辑,数据模型,API设计,电商系统',
    problem_ids = '15'
WHERE id = 53; -- Spring Boot电商系统

UPDATE path_level SET 
    knowledge_points = '服务拆分,服务通信,服务治理,微服务实现',
    problem_ids = '22'
WHERE id = 54; -- 微服务架构Demo

-- 路径7：Python后端开发进阶

-- 章节1：Web框架
UPDATE path_level SET 
    knowledge_points = 'Flask,路由,视图函数,请求处理',
    problem_ids = '22'
WHERE id = 55; -- Flask基础

UPDATE path_level SET 
    knowledge_points = 'Django,MVC,ORM,Admin',
    problem_ids = '23'
WHERE id = 56; -- Django基础

UPDATE path_level SET 
    knowledge_points = 'SQLAlchemy,ORM框架,模型定义,查询操作',
    problem_ids = '23'
WHERE id = 57; -- ORM框架

-- 章节2：微服务架构
UPDATE path_level SET 
    knowledge_points = 'FastAPI,异步处理,自动文档,数据验证',
    problem_ids = '22'
WHERE id = 58; -- FastAPI基础

UPDATE path_level SET 
    knowledge_points = 'Consul,服务注册,服务发现,健康检查',
    problem_ids = '22'
WHERE id = 59; -- 服务注册与发现

UPDATE path_level SET 
    knowledge_points = '配置管理,动态刷新,环境隔离',
    problem_ids = '22'
WHERE id = 60; -- 配置中心

-- 章节3：Python后端项目实战
UPDATE path_level SET 
    knowledge_points = '业务逻辑,数据库设计,API实现,Flask电商',
    problem_ids = '15'
WHERE id = 61; -- Flask电商系统

UPDATE path_level SET 
    knowledge_points = '服务拆分,异步处理,API网关,FastAPI微服务',
    problem_ids = '22'
WHERE id = 62; -- FastAPI微服务Demo

-- ============================================
-- 插入关卡-题目关联数据
-- ============================================

-- 路径1：Java编程基础入门
-- 章节1：Java基础语法
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(1, 1, 1, NOW()), -- 变量和数据类型 - 变量声明与赋值
(1, 2, 2, NOW()), -- 变量和数据类型 - 数据类型转换
(1, 3, 3, NOW()), -- 变量和数据类型 - 字符串操作基础
(2, 4, 1, NOW()), -- 控制流语句 - 条件判断练习
(2, 5, 2, NOW()), -- 控制流语句 - 循环结构应用
(2, 6, 3, NOW()), -- 控制流语句 - 嵌套循环控制
(3, 7, 1, NOW()), -- 方法和数组 - 方法定义与调用
(3, 8, 2, NOW()), -- 方法和数组 - 数组基本操作
(3, 9, 3, NOW()); -- 方法和数组 - 数组算法应用

-- 章节2：Java面向对象
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(4, 10, 1, NOW()), -- 类和对象 - 类的设计与实现
(5, 11, 1, NOW()), -- 继承和多态 - 继承关系实现
(6, 12, 1, NOW()); -- 接口和抽象类 - 接口设计与实现

-- 章节3：Java集合框架
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(7, 13, 1, NOW()), -- List集合 - ArrayList操作
(8, 14, 1, NOW()), -- Set和Map集合 - HashMap应用
(9, 13, 1, NOW()), -- 集合工具类 - ArrayList操作
(9, 14, 2, NOW()); -- 集合工具类 - HashMap应用

-- 章节4：Java基础项目实战
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(10, 15, 1, NOW()), -- 个人名片管理系统
(11, 15, 1, NOW()); -- 学生成绩管理系统

-- 路径2：Python编程基础入门
-- 章节1：Python基础语法
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(12, 1, 1, NOW()), -- 变量和数据类型
(12, 2, 2, NOW()),
(12, 3, 3, NOW()),
(13, 4, 1, NOW()), -- 控制流语句
(13, 5, 2, NOW()),
(13, 6, 3, NOW()),
(14, 7, 1, NOW()), -- 函数和模块
(14, 8, 2, NOW()),
(14, 9, 3, NOW());

-- 章节2：Python面向对象
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(15, 10, 1, NOW()), -- 类和对象
(16, 11, 1, NOW()), -- 继承和多态
(17, 10, 1, NOW()); -- 魔法方法

-- 章节3：Python集合
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(18, 8, 1, NOW()), -- 列表和元组
(18, 13, 2, NOW()),
(19, 14, 1, NOW()), -- 字典和集合
(20, 8, 1, NOW()); -- 列表推导式

-- 章节4：Python基础项目实战
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(21, 15, 1, NOW()), -- 个人名片管理系统
(22, 15, 1, NOW()); -- 学生成绩管理系统

-- 路径3：算法与数据结构基础
-- 章节1：算法基础
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(23, 16, 1, NOW()), -- 算法复杂度分析 - 冒泡排序实现
(24, 16, 1, NOW()), -- 排序算法基础 - 冒泡排序实现
(25, 17, 1, NOW()); -- 搜索算法基础 - 二分查找

-- 章节2：基础数据结构
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(26, 9, 1, NOW()), -- 数组和链表
(26, 18, 2, NOW()),
(27, 19, 1, NOW()), -- 栈和队列
(28, 1, 1, NOW()), -- 哈希表
(28, 14, 2, NOW());

-- 章节3：算法项目实战
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(29, 16, 1, NOW()), -- 基础算法应用
(29, 17, 2, NOW()),
(30, 19, 1, NOW()), -- 数据结构应用
(30, 14, 2, NOW());

-- 路径4：算法与数据结构进阶
-- 章节1：高级数据结构
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(31, 20, 1, NOW()), -- 树和二叉树 - 二叉树遍历
(32, 20, 1, NOW()), -- 图和图算法
(33, 16, 1, NOW()); -- 堆和优先队列

-- 章节2：高级算法
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(34, 21, 1, NOW()), -- 动态规划
(35, 21, 1, NOW()), -- 贪心算法
(36, 21, 1, NOW()); -- 回溯算法

-- 章节3：算法项目实战
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(37, 21, 1, NOW()), -- 高级算法应用
(38, 20, 1, NOW()); -- 复杂数据结构应用

-- 路径5：后端开发基础
-- 章节1：网络基础
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(39, 22, 1, NOW()), -- HTTP协议
(40, 22, 1, NOW()), -- TCP/IP基础
(41, 22, 1, NOW()); -- RESTful API设计

-- 章节2：数据库基础
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(42, 23, 1, NOW()), -- SQL基础
(43, 23, 1, NOW()), -- 数据库设计
(44, 23, 1, NOW()); -- ORM基础

-- 章节3：后端项目实战
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(45, 22, 1, NOW()), -- RESTful API开发
(46, 23, 1, NOW()); -- 数据库应用

-- 路径6：Java后端开发进阶
-- 章节1：Spring框架
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(47, 22, 1, NOW()), -- Spring Boot基础
(48, 22, 1, NOW()), -- Spring MVC
(49, 23, 1, NOW()); -- Spring Data JPA

-- 章节2：微服务架构
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(50, 22, 1, NOW()), -- Spring Cloud基础
(51, 22, 1, NOW()), -- 服务注册与发现
(52, 22, 1, NOW()); -- 配置中心

-- 章节3：Java后端项目实战
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(53, 15, 1, NOW()), -- Spring Boot电商系统
(54, 22, 1, NOW()); -- 微服务架构Demo

-- 路径7：Python后端开发进阶
-- 章节1：Web框架
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(55, 22, 1, NOW()), -- Flask基础
(56, 23, 1, NOW()), -- Django基础
(57, 23, 1, NOW()); -- ORM框架

-- 章节2：微服务架构
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(58, 22, 1, NOW()), -- FastAPI基础
(59, 22, 1, NOW()), -- 服务注册与发现
(60, 22, 1, NOW()); -- 配置中心

-- 章节3：Python后端项目实战
INSERT INTO path_level_problem (level_id, problem_id, order_num, create_time) VALUES
(61, 15, 1, NOW()), -- Flask电商系统
(62, 22, 1, NOW()); -- FastAPI微服务Demo

-- ============================================
-- 更新path_level_problem表的path_id和chapter_id
-- ============================================

UPDATE path_level_problem plp
JOIN path_level pl ON plp.level_id = pl.id
JOIN path_chapter pc ON pl.chapter_id = pc.id
SET plp.path_id = pc.path_id,
    plp.chapter_id = pl.chapter_id;

-- ============================================
-- 数据验证查询
-- ============================================

-- 验证关卡内容更新
SELECT 
    lp.id AS level_id,
    lp.name AS level_name,
    pc.name AS chapter_name,
    l.name AS path_name,
    lp.knowledge_points,
    lp.problem_ids
FROM path_level lp
JOIN path_chapter pc ON lp.chapter_id = pc.id
JOIN learning_path l ON pc.path_id = l.id
ORDER BY l.id, pc.order_num, lp.order_num;

-- 验证关卡-题目关联
SELECT 
    l.name AS path_name,
    pc.name AS chapter_name,
    pl.name AS level_name,
    p.title AS problem_title,
    plp.order_num
FROM path_level_problem plp
JOIN path_level pl ON plp.level_id = pl.id
JOIN path_chapter pc ON plp.chapter_id = pc.id
JOIN learning_path l ON plp.path_id = l.id
JOIN problem p ON plp.problem_id = p.id
ORDER BY l.id, pc.order_num, pl.order_num, plp.order_num;

-- 统计每个路径的题目数量
SELECT 
    l.name AS path_name,
    COUNT(DISTINCT plp.problem_id) AS problem_count
FROM path_level_problem plp
JOIN learning_path l ON plp.path_id = l.id
GROUP BY l.id, l.name
ORDER BY l.id;

-- 统计每个关卡的题目数量
SELECT 
    l.name AS path_name,
    pc.name AS chapter_name,
    pl.name AS level_name,
    COUNT(plp.problem_id) AS problem_count
FROM path_level pl
JOIN path_chapter pc ON pl.chapter_id = pc.id
JOIN learning_path l ON pc.path_id = l.id
LEFT JOIN path_level_problem plp ON pl.id = plp.level_id
GROUP BY l.id, l.name, pc.id, pc.name, pl.id, pl.name
ORDER BY l.id, pc.order_num, pl.order_num;
