-- 更新剩余关卡内容

-- 章节2：Java面向对象
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