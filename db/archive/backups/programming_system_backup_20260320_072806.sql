-- Database backup for programming_system
-- Created at: 20260320_072806

SET FOREIGN_KEY_CHECKS = 0;

-- Table structure for ai_collection
DROP TABLE IF EXISTS `ai_collection`;
CREATE TABLE `ai_collection` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `session_id` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `session_id` (`session_id`),
  CONSTRAINT `ai_collection_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `ai_collection_ibfk_2` FOREIGN KEY (`session_id`) REFERENCES `ai_session` (`session_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for ai_collection

-- Table structure for ai_message
DROP TABLE IF EXISTS `ai_message`;
CREATE TABLE `ai_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(255) NOT NULL,
  `role` varchar(50) NOT NULL,
  `content` text NOT NULL,
  `related_problem_id` bigint DEFAULT NULL,
  `related_submit_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `session_id` (`session_id`),
  CONSTRAINT `ai_message_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `ai_session` (`session_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for ai_message
INSERT INTO `ai_message` (`id`, `session_id`, `role`, `content`, `related_problem_id`, `related_submit_id`, `create_time`) VALUES (11, '0e1359ef-d624-4203-958c-658789f0d87f', 'user', '你好
', NULL, NULL, '2026-03-20 03:00:15');
INSERT INTO `ai_message` (`id`, `session_id`, `role`, `content`, `related_problem_id`, `related_submit_id`, `create_time`) VALUES (12, '0e1359ef-d624-4203-958c-658789f0d87f', 'assistant', '你好！很高兴见到你。有什么我可以帮你的吗？', NULL, NULL, '2026-03-20 03:00:15');

-- Table structure for ai_session
DROP TABLE IF EXISTS `ai_session`;
CREATE TABLE `ai_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  `topic` varchar(255) DEFAULT NULL,
  `status` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `ai_session_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for ai_session
INSERT INTO `ai_session` (`id`, `session_id`, `user_id`, `topic`, `status`, `create_time`, `update_time`) VALUES (4, '0e1359ef-d624-4203-958c-658789f0d87f', 1, '你好
', NULL, '2026-03-20 03:00:15', '2026-03-20 03:00:15');

-- Table structure for alert_config
DROP TABLE IF EXISTS `alert_config`;
CREATE TABLE `alert_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `metric` varchar(100) NOT NULL,
  `threshold` double NOT NULL,
  `operator` varchar(10) NOT NULL,
  `enabled` int DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for alert_config

-- Table structure for alert_record
DROP TABLE IF EXISTS `alert_record`;
CREATE TABLE `alert_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `alert_config_id` bigint NOT NULL,
  `metric_value` double NOT NULL,
  `threshold` double NOT NULL,
  `status` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `alert_config_id` (`alert_config_id`),
  CONSTRAINT `alert_record_ibfk_1` FOREIGN KEY (`alert_config_id`) REFERENCES `alert_config` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for alert_record

-- Table structure for api_metric
DROP TABLE IF EXISTS `api_metric`;
CREATE TABLE `api_metric` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `api_path` varchar(500) NOT NULL,
  `method` varchar(20) NOT NULL,
  `response_time` double NOT NULL,
  `status_code` int NOT NULL,
  `request_count` int DEFAULT '1',
  `error_count` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for api_metric

-- Table structure for assessment_question
DROP TABLE IF EXISTS `assessment_question`;
CREATE TABLE `assessment_question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `language` varchar(50) NOT NULL,
  `direction` varchar(100) NOT NULL,
  `question` text NOT NULL,
  `options` text,
  `correct_answer` varchar(255) DEFAULT NULL,
  `difficulty` int NOT NULL,
  `knowledge_point` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=91 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for assessment_question
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (1, 'java', 'algorithm', '以下哪种排序算法的平均时间复杂度为O(n log n)?', '["冒泡排序", "插入排序", "快速排序", "选择排序"]', '2', 2, '排序算法', '2026-03-20 03:15:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (2, 'java', 'algorithm', '以下关于递归的说法，正确的是？', '["递归一定比迭代效率高", "递归会增加内存使用", "递归不能解决任何问题", "递归不需要终止条件"]', '1', 1, '递归', '2026-03-20 03:15:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (3, 'java', 'algorithm', '二分查找的时间复杂度是？', '["O(n)", "O(n log n)", "O(log n)", "O(n²)"]', '2', 2, '查找算法', '2026-03-20 03:15:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (4, 'java', 'algorithm', '以下哪种数据结构适合实现队列？', '["二叉树", "栈", "链表", "哈希表"]', '2', 2, '数据结构', '2026-03-20 03:15:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (5, 'java', 'algorithm', '快速排序的最坏情况时间复杂度是？', '["O(n)", "O(n log n)", "O(log n)", "O(n²)"]', '3', 3, '排序算法', '2026-03-20 03:15:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (6, 'java', 'algorithm', '以下关于动态规划的说法，正确的是？', '["动态规划只适用于解决数学问题", "动态规划通过记忆化避免重复计算", "动态规划的时间复杂度一定比贪心算法高", "动态规划不需要状态转移方程"]', '1', 3, '动态规划', '2026-03-20 03:15:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (7, 'java', 'algorithm', '图的深度优先搜索使用哪种数据结构？', '["队列", "栈", "链表", "哈希表"]', '1', 2, '图算法', '2026-03-20 03:15:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (8, 'java', 'algorithm', '以下哪种算法不是贪心算法的应用？', '["霍夫曼编码", "Dijkstra算法", "Kruskal算法", "背包问题"]', '3', 3, '贪心算法', '2026-03-20 03:15:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (9, 'java', 'algorithm', '分治算法的基本思想是？', '["将大问题分解为小问题", "只解决问题的一部分", "使用贪心策略", "依赖动态规划"]', '0', 2, '分治算法', '2026-03-20 03:15:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (10, 'java', 'algorithm', '以下关于时间复杂度的说法，正确的是？', '["O(n)比O(log n)更高效", "时间复杂度只考虑最坏情况", "时间复杂度与输入规模无关", "O(1)表示常数时间"]', '3', 1, '时间复杂度', '2026-03-20 03:15:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (11, 'java', 'algorithm', '以下哪种排序算法的平均时间复杂度为O(n log n)?', '["冒泡排序", "插入排序", "快速排序", "选择排序"]', '2', 2, '排序算法', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (12, 'java', 'algorithm', '以下关于递归的说法，正确的是？', '["递归一定比迭代效率高", "递归会增加内存使用", "递归不能解决任何问题", "递归不需要终止条件"]', '1', 1, '递归', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (13, 'java', 'algorithm', '二分查找的时间复杂度是？', '["O(n)", "O(n log n)", "O(log n)", "O(n²)"]', '2', 2, '查找算法', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (14, 'java', 'algorithm', '快速排序的最坏情况时间复杂度是？', '["O(n)", "O(n log n)", "O(log n)", "O(n²)"]', '3', 3, '排序算法', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (15, 'java', 'algorithm', '以下哪种算法不是贪心算法的应用？', '["霍夫曼编码", "Dijkstra算法", "Kruskal算法", "背包问题"]', '3', 3, '贪心算法', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (16, 'java', 'algorithm', '分治算法的基本思想是？', '["将大问题分解为小问题", "只解决问题的一部分", "使用贪心策略", "依赖动态规划"]', '0', 2, '分治算法', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (17, 'java', 'algorithm', '图的深度优先搜索使用哪种数据结构？', '["队列", "栈", "链表", "哈希表"]', '1', 2, '图算法', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (18, 'java', 'algorithm', '以下关于动态规划的说法，正确的是？', '["动态规划只适用于解决数学问题", "动态规划通过记忆化避免重复计算", "动态规划的时间复杂度一定比贪心算法高", "动态规划不需要状态转移方程"]', '1', 3, '动态规划', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (19, 'java', 'algorithm', '以下关于时间复杂度的说法，正确的是？', '["O(n)比O(log n)更高效", "时间复杂度只考虑最坏情况", "时间复杂度与输入规模无关", "O(1)表示常数时间"]', '3', 1, '时间复杂度', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (20, 'java', 'algorithm', 'KMP算法的主要用途是？', '["排序", "查找子串", "图遍历", "最短路径"]', '1', 3, '字符串算法', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (21, 'java', 'data-structure', '以下哪种数据结构适合实现栈？', '["二叉树", "链表", "队列", "哈希表"]', '1', 2, '栈', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (22, 'java', 'data-structure', '以下哪种数据结构适合实现队列？', '["二叉树", "栈", "链表", "哈希表"]', '2', 2, '队列', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (23, 'java', 'data-structure', '哈希表的平均查找时间复杂度是？', '["O(n)", "O(n log n)", "O(log n)", "O(1)"]', '3', 2, '哈希表', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (24, 'java', 'data-structure', '二叉搜索树的平均查找时间复杂度是？', '["O(n)", "O(n log n)", "O(log n)", "O(1)"]', '2', 2, '二叉搜索树', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (25, 'java', 'data-structure', '以下关于链表的说法，正确的是？', '["链表的随机访问时间复杂度是O(1)", "链表的插入操作时间复杂度是O(1)", "链表的空间复杂度比数组低", "链表不需要指针"]', '1', 2, '链表', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (26, 'java', 'data-structure', '堆的时间复杂度是？', '["O(n)", "O(n log n)", "O(log n)", "O(1)"]', '2', 3, '堆', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (27, 'java', 'data-structure', '图的邻接矩阵表示法的空间复杂度是？', '["O(n)", "O(n²)", "O(n log n)", "O(1)"]', '1', 2, '图', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (28, 'java', 'data-structure', '以下哪种数据结构可以实现优先队列？', '["栈", "队列", "堆", "链表"]', '2', 2, '优先队列', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (29, 'java', 'data-structure', '红黑树的时间复杂度是？', '["O(n)", "O(n log n)", "O(log n)", "O(1)"]', '2', 3, '红黑树', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (30, 'java', 'data-structure', '以下关于数组的说法，正确的是？', '["数组的插入操作时间复杂度是O(1)", "数组的随机访问时间复杂度是O(1)", "数组的空间复杂度比链表低", "数组不需要连续的内存空间"]', '1', 1, '数组', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (31, 'java', 'frontend', '以下哪个不是前端开发的核心技术？', '["HTML", "CSS", "JavaScript", "Java"]', '3', 1, '前端技术', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (32, 'java', 'frontend', '以下哪个是CSS的预处理器？', '["React", "Vue", "Sass", "Angular"]', '2', 2, 'CSS', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (33, 'java', 'frontend', '以下哪个是JavaScript的框架？', '["Spring", "Django", "React", "Flask"]', '2', 2, 'JavaScript框架', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (34, 'java', 'frontend', '以下哪个是前端构建工具？', '["Maven", "Webpack", "Gradle", "Ant"]', '1', 2, '构建工具', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (35, 'java', 'frontend', '以下哪个是前端状态管理库？', '["Redux", "Spring Boot", "Django", "Flask"]', '0', 2, '状态管理', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (36, 'java', 'frontend', '以下关于HTML5的说法，正确的是？', '["HTML5不支持语义化标签", "HTML5不支持本地存储", "HTML5支持Canvas绘图", "HTML5不需要DOCTYPE声明"]', '2', 2, 'HTML5', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (37, 'java', 'frontend', '以下哪个是CSS框架？', '["Bootstrap", "Spring", "Django", "Flask"]', '0', 2, 'CSS框架', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (38, 'java', 'frontend', '以下关于JavaScript的说法，正确的是？', '["JavaScript是编译型语言", "JavaScript只能在浏览器中运行", "JavaScript是弱类型语言", "JavaScript不支持面向对象编程"]', '2', 2, 'JavaScript', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (39, 'java', 'frontend', '以下哪个是前端测试框架？', '["JUnit", "Mocha", "TestNG", "PyTest"]', '1', 3, '测试框架', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (40, 'java', 'frontend', '以下关于响应式设计的说法，正确的是？', '["响应式设计只适用于移动端", "响应式设计不需要媒体查询", "响应式设计可以适应不同屏幕尺寸", "响应式设计会增加页面加载时间"]', '2', 2, '响应式设计', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (41, 'java', 'backend', '以下哪个是Java的Web框架？', '["React", "Vue", "Spring Boot", "Angular"]', '2', 2, 'Java框架', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (42, 'java', 'backend', '以下哪个是Java的数据库连接池？', '["HikariCP", "Redis", "MongoDB", "Elasticsearch"]', '0', 2, '连接池', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (43, 'java', 'backend', '以下哪个是Java的ORM框架？', '["MyBatis", "jQuery", "React", "Vue"]', '0', 2, 'ORM框架', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (44, 'java', 'backend', '以下哪个是Java的依赖管理工具？', '["NPM", "Maven", "Yarn", "Webpack"]', '1', 2, '依赖管理', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (45, 'java', 'backend', '以下关于Spring Boot的说法，正确的是？', '["Spring Boot需要手动配置Tomcat", "Spring Boot内置了Tomcat", "Spring Boot不支持自动配置", "Spring Boot不是Spring的一部分"]', '1', 2, 'Spring Boot', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (46, 'java', 'backend', '以下哪个是Java的缓存框架？', '["Redis", "EhCache", "Memcached", "MongoDB"]', '1', 2, '缓存框架', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (47, 'java', 'backend', '以下哪个是Java的消息队列？', '["Kafka", "Redis", "MongoDB", "Elasticsearch"]', '0', 3, '消息队列', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (48, 'java', 'backend', '以下关于RESTful API的说法，正确的是？', '["RESTful API必须使用XML", "RESTful API必须使用POST方法", "RESTful API使用HTTP方法表达操作", "RESTful API不支持JSON"]', '2', 2, 'RESTful API', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (49, 'java', 'backend', '以下哪个是Java的安全框架？', '["Spring Security", "jQuery", "React", "Vue"]', '0', 3, '安全框架', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (50, 'java', 'backend', '以下关于微服务的说法，正确的是？', '["微服务架构会增加系统复杂度", "微服务架构不支持独立部署", "微服务架构不需要服务发现", "微服务架构不能使用RESTful API"]', '0', 3, '微服务', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (51, 'python', 'algorithm', '以下哪种排序算法的平均时间复杂度为O(n log n)?', '["冒泡排序", "插入排序", "快速排序", "选择排序"]', '2', 2, '排序算法', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (52, 'python', 'algorithm', '以下关于递归的说法，正确的是？', '["递归一定比迭代效率高", "递归会增加内存使用", "递归不能解决任何问题", "递归不需要终止条件"]', '1', 1, '递归', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (53, 'python', 'algorithm', '二分查找的时间复杂度是？', '["O(n)", "O(n log n)", "O(log n)", "O(n²)"]', '2', 2, '查找算法', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (54, 'python', 'algorithm', '快速排序的最坏情况时间复杂度是？', '["O(n)", "O(n log n)", "O(log n)", "O(n²)"]', '3', 3, '排序算法', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (55, 'python', 'algorithm', '以下哪种算法不是贪心算法的应用？', '["霍夫曼编码", "Dijkstra算法", "Kruskal算法", "背包问题"]', '3', 3, '贪心算法', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (56, 'python', 'algorithm', '分治算法的基本思想是？', '["将大问题分解为小问题", "只解决问题的一部分", "使用贪心策略", "依赖动态规划"]', '0', 2, '分治算法', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (57, 'python', 'algorithm', '图的深度优先搜索使用哪种数据结构？', '["队列", "栈", "链表", "哈希表"]', '1', 2, '图算法', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (58, 'python', 'algorithm', '以下关于动态规划的说法，正确的是？', '["动态规划只适用于解决数学问题", "动态规划通过记忆化避免重复计算", "动态规划的时间复杂度一定比贪心算法高", "动态规划不需要状态转移方程"]', '1', 3, '动态规划', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (59, 'python', 'algorithm', '以下关于时间复杂度的说法，正确的是？', '["O(n)比O(log n)更高效", "时间复杂度只考虑最坏情况", "时间复杂度与输入规模无关", "O(1)表示常数时间"]', '3', 1, '时间复杂度', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (60, 'python', 'algorithm', 'KMP算法的主要用途是？', '["排序", "查找子串", "图遍历", "最短路径"]', '1', 3, '字符串算法', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (61, 'python', 'data-structure', '以下哪种数据结构适合实现栈？', '["二叉树", "链表", "队列", "哈希表"]', '1', 2, '栈', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (62, 'python', 'data-structure', '以下哪种数据结构适合实现队列？', '["二叉树", "栈", "链表", "哈希表"]', '2', 2, '队列', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (63, 'python', 'data-structure', '哈希表的平均查找时间复杂度是？', '["O(n)", "O(n log n)", "O(log n)", "O(1)"]', '3', 2, '哈希表', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (64, 'python', 'data-structure', '二叉搜索树的平均查找时间复杂度是？', '["O(n)", "O(n log n)", "O(log n)", "O(1)"]', '2', 2, '二叉搜索树', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (65, 'python', 'data-structure', '以下关于链表的说法，正确的是？', '["链表的随机访问时间复杂度是O(1)", "链表的插入操作时间复杂度是O(1)", "链表的空间复杂度比数组低", "链表不需要指针"]', '1', 2, '链表', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (66, 'python', 'data-structure', '堆的时间复杂度是？', '["O(n)", "O(n log n)", "O(log n)", "O(1)"]', '2', 3, '堆', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (67, 'python', 'data-structure', '图的邻接矩阵表示法的空间复杂度是？', '["O(n)", "O(n²)", "O(n log n)", "O(1)"]', '1', 2, '图', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (68, 'python', 'data-structure', '以下哪种数据结构可以实现优先队列？', '["栈", "队列", "堆", "链表"]', '2', 2, '优先队列', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (69, 'python', 'data-structure', '红黑树的时间复杂度是？', '["O(n)", "O(n log n)", "O(log n)", "O(1)"]', '2', 3, '红黑树', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (70, 'python', 'data-structure', '以下关于数组的说法，正确的是？', '["数组的插入操作时间复杂度是O(1)", "数组的随机访问时间复杂度是O(1)", "数组的空间复杂度比链表低", "数组不需要连续的内存空间"]', '1', 1, '数组', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (71, 'python', 'frontend', '以下哪个不是前端开发的核心技术？', '["HTML", "CSS", "JavaScript", "Python"]', '3', 1, '前端技术', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (72, 'python', 'frontend', '以下哪个是CSS的预处理器？', '["React", "Vue", "Sass", "Angular"]', '2', 2, 'CSS', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (73, 'python', 'frontend', '以下哪个是JavaScript的框架？', '["Django", "Flask", "React", "Pyramid"]', '2', 2, 'JavaScript框架', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (74, 'python', 'frontend', '以下哪个是前端构建工具？', '["Pip", "Webpack", "Conda", "Virtualenv"]', '1', 2, '构建工具', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (75, 'python', 'frontend', '以下哪个是前端状态管理库？', '["Redux", "Django", "Flask", "Pyramid"]', '0', 2, '状态管理', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (76, 'python', 'frontend', '以下关于HTML5的说法，正确的是？', '["HTML5不支持语义化标签", "HTML5不支持本地存储", "HTML5支持Canvas绘图", "HTML5不需要DOCTYPE声明"]', '2', 2, 'HTML5', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (77, 'python', 'frontend', '以下哪个是CSS框架？', '["Bootstrap", "Django", "Flask", "Pyramid"]', '0', 2, 'CSS框架', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (78, 'python', 'frontend', '以下关于JavaScript的说法，正确的是？', '["JavaScript是编译型语言", "JavaScript只能在浏览器中运行", "JavaScript是弱类型语言", "JavaScript不支持面向对象编程"]', '2', 2, 'JavaScript', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (79, 'python', 'frontend', '以下哪个是前端测试框架？', '["PyTest", "Mocha", "unittest", "pytest"]', '1', 3, '测试框架', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (80, 'python', 'frontend', '以下关于响应式设计的说法，正确的是？', '["响应式设计只适用于移动端", "响应式设计不需要媒体查询", "响应式设计可以适应不同屏幕尺寸", "响应式设计会增加页面加载时间"]', '2', 2, '响应式设计', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (81, 'python', 'backend', '以下哪个是Python的Web框架？', '["Django", "Spring Boot", "Express", "Flask"]', '0', 2, 'Python框架', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (82, 'python', 'backend', '以下哪个是Python的数据库连接库？', '["psycopg2", "HikariCP", "JDBC", "ODBC"]', '0', 2, '数据库连接', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (83, 'python', 'backend', '以下哪个是Python的ORM框架？', '["SQLAlchemy", "MyBatis", "Hibernate", "Entity Framework"]', '0', 2, 'ORM框架', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (84, 'python', 'backend', '以下哪个是Python的依赖管理工具？', '["Pip", "Maven", "Yarn", "Webpack"]', '0', 2, '依赖管理', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (85, 'python', 'backend', '以下关于Django的说法，正确的是？', '["Django是轻量级框架", "Django内置了管理后台", "Django不支持ORM", "Django不是MVC框架"]', '1', 2, 'Django', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (86, 'python', 'backend', '以下哪个是Python的缓存库？', '["Redis-py", "EhCache", "Memcached", "MongoDB"]', '0', 2, '缓存', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (87, 'python', 'backend', '以下哪个是Python的消息队列？', '["Celery", "Kafka", "RabbitMQ", "ActiveMQ"]', '0', 3, '消息队列', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (88, 'python', 'backend', '以下关于RESTful API的说法，正确的是？', '["RESTful API必须使用XML", "RESTful API必须使用POST方法", "RESTful API使用HTTP方法表达操作", "RESTful API不支持JSON"]', '2', 2, 'RESTful API', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (89, 'python', 'backend', '以下哪个是Python的安全库？', '["PyJWT", "Spring Security", "Passport", "JWT"]', '0', 3, '安全', '2026-03-20 03:31:49');
INSERT INTO `assessment_question` (`id`, `language`, `direction`, `question`, `options`, `correct_answer`, `difficulty`, `knowledge_point`, `create_time`) VALUES (90, 'python', 'backend', '以下关于微服务的说法，正确的是？', '["微服务架构会增加系统复杂度", "微服务架构不支持独立部署", "微服务架构不需要服务发现", "微服务架构不能使用RESTful API"]', '0', 3, '微服务', '2026-03-20 03:31:49');

-- Table structure for audit_log
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `operation` varchar(255) NOT NULL,
  `target_type` varchar(100) DEFAULT NULL,
  `target_id` bigint DEFAULT NULL,
  `details` text,
  `ip_address` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `audit_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for audit_log

-- Table structure for class_info
DROP TABLE IF EXISTS `class_info`;
CREATE TABLE `class_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `teacher_id` bigint NOT NULL,
  `description` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `class_info_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for class_info

-- Table structure for course
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` text,
  `language` varchar(50) NOT NULL,
  `path_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `path_id` (`path_id`),
  CONSTRAINT `course_ibfk_1` FOREIGN KEY (`path_id`) REFERENCES `learning_path` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for course

-- Table structure for course_chapter
DROP TABLE IF EXISTS `course_chapter`;
CREATE TABLE `course_chapter` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `order_num` int NOT NULL,
  `content` text,
  `video_url` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `course_id` (`course_id`),
  CONSTRAINT `course_chapter_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for course_chapter

-- Table structure for knowledge_point
DROP TABLE IF EXISTS `knowledge_point`;
CREATE TABLE `knowledge_point` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `level` int NOT NULL,
  `description` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for knowledge_point

-- Table structure for knowledge_relationship
DROP TABLE IF EXISTS `knowledge_relationship`;
CREATE TABLE `knowledge_relationship` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `source_id` bigint NOT NULL,
  `target_id` bigint NOT NULL,
  `relation_type` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `source_id` (`source_id`),
  KEY `target_id` (`target_id`),
  CONSTRAINT `knowledge_relationship_ibfk_1` FOREIGN KEY (`source_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE,
  CONSTRAINT `knowledge_relationship_ibfk_2` FOREIGN KEY (`target_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for knowledge_relationship

-- Table structure for learn_record
DROP TABLE IF EXISTS `learn_record`;
CREATE TABLE `learn_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `problem_count` int DEFAULT '0' COMMENT '做题总数',
  `correct_count` int DEFAULT '0' COMMENT '正确数',
  `last_problem_id` bigint DEFAULT '0' COMMENT '最后做题ID',
  `last_learn_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后学习时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学习记录表';

-- Data for learn_record
INSERT INTO `learn_record` (`id`, `user_id`, `problem_count`, `correct_count`, `last_problem_id`, `last_learn_time`) VALUES (1, 1, 10, 0, 9, '2026-03-13 19:40:19');
INSERT INTO `learn_record` (`id`, `user_id`, `problem_count`, `correct_count`, `last_problem_id`, `last_learn_time`) VALUES (2, 2, 4, 0, 9, '2026-03-12 21:18:59');

-- Table structure for learning_path
DROP TABLE IF EXISTS `learning_path`;
CREATE TABLE `learning_path` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `direction` varchar(100) NOT NULL,
  `language` varchar(50) NOT NULL,
  `description` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for learning_path
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (1, 'Java编程基础入门', 'algorithm', 'java', '适合零基础学习者的Java编程入门路径，从语法开始，逐步深入算法基础', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (2, 'Java算法与数据结构进阶', 'algorithm', 'java', '深入学习Java算法和数据结构，提升编程能力', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (3, 'Java高级编程技术', 'algorithm', 'java', '学习Java高级编程技术和设计模式，准备进入专业开发', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (4, 'Java专家级技术研究', 'algorithm', 'java', '研究Java前沿技术，参与开源项目，成为技术专家', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (5, 'Java数据结构基础', 'data-structure', 'java', '学习Java数据结构基础知识，包括数组、链表、栈、队列等', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (6, 'Java数据结构进阶', 'data-structure', 'java', '深入学习Java高级数据结构，包括树、图、堆等', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (7, 'Java前端开发基础', 'frontend', 'java', '学习Java相关的前端开发技术，包括HTML、CSS、JavaScript等', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (8, 'Java前端开发进阶', 'frontend', 'java', '深入学习Java前端框架和工具，提升前端开发能力', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (9, 'Java后端开发基础', 'backend', 'java', '学习Java后端开发基础，包括Spring Boot、数据库等', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (10, 'Java后端开发进阶', 'backend', 'java', '深入学习Java后端高级技术，包括微服务、分布式系统等', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (11, 'Python编程基础入门', 'algorithm', 'python', '适合零基础学习者的Python编程入门路径，从语法开始，逐步深入算法基础', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (12, 'Python算法与数据结构进阶', 'algorithm', 'python', '深入学习Python算法和数据结构，提升编程能力', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (13, 'Python高级编程技术', 'algorithm', 'python', '学习Python高级编程技术和设计模式，准备进入专业开发', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (14, 'Python专家级技术研究', 'algorithm', 'python', '研究Python前沿技术，参与开源项目，成为技术专家', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (15, 'Python数据结构基础', 'data-structure', 'python', '学习Python数据结构基础知识，包括列表、字典、栈、队列等', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (16, 'Python数据结构进阶', 'data-structure', 'python', '深入学习Python高级数据结构，包括树、图、堆等', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (17, 'Python前端开发基础', 'frontend', 'python', '学习Python相关的前端开发技术，包括HTML、CSS、JavaScript等', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (18, 'Python前端开发进阶', 'frontend', 'python', '深入学习Python前端框架和工具，提升前端开发能力', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (19, 'Python后端开发基础', 'backend', 'python', '学习Python后端开发基础，包括Django、Flask、数据库等', '2026-03-20 03:36:20', '2026-03-20 03:36:20');
INSERT INTO `learning_path` (`id`, `name`, `direction`, `language`, `description`, `create_time`, `update_time`) VALUES (20, 'Python后端开发进阶', 'backend', 'python', '深入学习Python后端高级技术，包括微服务、分布式系统等', '2026-03-20 03:36:20', '2026-03-20 03:36:20');

-- Table structure for learning_resource
DROP TABLE IF EXISTS `learning_resource`;
CREATE TABLE `learning_resource` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `type` varchar(100) NOT NULL,
  `url` varchar(500) NOT NULL,
  `description` text,
  `knowledge_points` text,
  `visibility` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for learning_resource

-- Table structure for path_chapter
DROP TABLE IF EXISTS `path_chapter`;
CREATE TABLE `path_chapter` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `path_id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `order_num` int NOT NULL,
  `description` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `path_id` (`path_id`),
  CONSTRAINT `path_chapter_ibfk_1` FOREIGN KEY (`path_id`) REFERENCES `learning_path` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=139 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for path_chapter
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (1, 1, 'Java基础语法', 1, '学习Java的基本语法和数据类型', '2026-03-20 03:36:49', '2026-03-20 03:36:49');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (2, 1, 'Java面向对象', 2, '学习Java的面向对象编程特性', '2026-03-20 03:36:49', '2026-03-20 03:36:49');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (3, 1, 'Java集合框架', 3, '学习Java的集合框架和常用数据结构', '2026-03-20 03:36:49', '2026-03-20 03:36:49');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (4, 1, 'Java IO操作', 4, '学习Java的输入输出操作', '2026-03-20 03:36:49', '2026-03-20 03:36:49');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (5, 1, 'Java多线程', 5, '学习Java的多线程编程', '2026-03-20 03:36:49', '2026-03-20 03:36:49');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (6, 2, 'Java基础语法', 1, '学习Java的基本语法和数据类型', '2026-03-20 04:35:05', '2026-03-20 04:35:05');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (7, 2, 'Java面向对象', 2, '学习Java的面向对象编程特性', '2026-03-20 04:35:05', '2026-03-20 04:35:05');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (8, 2, 'Java集合框架', 3, '学习Java的集合框架和常用数据结构', '2026-03-20 04:35:05', '2026-03-20 04:35:05');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (9, 2, 'Java IO操作', 4, '学习Java的输入输出操作', '2026-03-20 04:35:05', '2026-03-20 04:35:05');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (10, 2, 'Java多线程', 5, '学习Java的多线程编程', '2026-03-20 04:35:05', '2026-03-20 04:35:05');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (13, 3, 'Java基础语法', 1, '学习Java的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (14, 3, 'Java面向对象', 2, '学习Java的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (15, 3, 'Java集合框架', 3, '学习Java的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (16, 3, 'Java IO操作', 4, '学习Java的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (17, 3, 'Java多线程', 5, '学习Java的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (20, 4, 'Java基础语法', 1, '学习Java的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (21, 4, 'Java面向对象', 2, '学习Java的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (22, 4, 'Java集合框架', 3, '学习Java的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (23, 4, 'Java IO操作', 4, '学习Java的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (24, 4, 'Java多线程', 5, '学习Java的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (27, 5, 'Java基础语法', 1, '学习Java的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (28, 5, 'Java面向对象', 2, '学习Java的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (29, 5, 'Java集合框架', 3, '学习Java的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (30, 5, 'Java IO操作', 4, '学习Java的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (31, 5, 'Java多线程', 5, '学习Java的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (34, 6, 'Java基础语法', 1, '学习Java的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (35, 6, 'Java面向对象', 2, '学习Java的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (36, 6, 'Java集合框架', 3, '学习Java的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (37, 6, 'Java IO操作', 4, '学习Java的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (38, 6, 'Java多线程', 5, '学习Java的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (41, 7, 'Java基础语法', 1, '学习Java的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (42, 7, 'Java面向对象', 2, '学习Java的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (43, 7, 'Java集合框架', 3, '学习Java的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (44, 7, 'Java IO操作', 4, '学习Java的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (45, 7, 'Java多线程', 5, '学习Java的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (48, 8, 'Java基础语法', 1, '学习Java的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (49, 8, 'Java面向对象', 2, '学习Java的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (50, 8, 'Java集合框架', 3, '学习Java的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (51, 8, 'Java IO操作', 4, '学习Java的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (52, 8, 'Java多线程', 5, '学习Java的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (55, 9, 'Java基础语法', 1, '学习Java的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (56, 9, 'Java面向对象', 2, '学习Java的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (57, 9, 'Java集合框架', 3, '学习Java的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (58, 9, 'Java IO操作', 4, '学习Java的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (59, 9, 'Java多线程', 5, '学习Java的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (62, 10, 'Java基础语法', 1, '学习Java的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (63, 10, 'Java面向对象', 2, '学习Java的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (64, 10, 'Java集合框架', 3, '学习Java的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (65, 10, 'Java IO操作', 4, '学习Java的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (66, 10, 'Java多线程', 5, '学习Java的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (69, 11, 'Python基础语法', 1, '学习Python的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (70, 11, 'Python面向对象', 2, '学习Python的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (71, 11, 'Python集合框架', 3, '学习Python的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (72, 11, 'Python IO操作', 4, '学习Python的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (73, 11, 'Python多线程', 5, '学习Python的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (76, 12, 'Python基础语法', 1, '学习Python的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (77, 12, 'Python面向对象', 2, '学习Python的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (78, 12, 'Python集合框架', 3, '学习Python的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (79, 12, 'Python IO操作', 4, '学习Python的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (80, 12, 'Python多线程', 5, '学习Python的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (83, 13, 'Python基础语法', 1, '学习Python的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (84, 13, 'Python面向对象', 2, '学习Python的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (85, 13, 'Python集合框架', 3, '学习Python的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (86, 13, 'Python IO操作', 4, '学习Python的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (87, 13, 'Python多线程', 5, '学习Python的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (90, 14, 'Python基础语法', 1, '学习Python的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (91, 14, 'Python面向对象', 2, '学习Python的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (92, 14, 'Python集合框架', 3, '学习Python的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (93, 14, 'Python IO操作', 4, '学习Python的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (94, 14, 'Python多线程', 5, '学习Python的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (97, 15, 'Python基础语法', 1, '学习Python的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (98, 15, 'Python面向对象', 2, '学习Python的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (99, 15, 'Python集合框架', 3, '学习Python的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (100, 15, 'Python IO操作', 4, '学习Python的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (101, 15, 'Python多线程', 5, '学习Python的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (104, 16, 'Python基础语法', 1, '学习Python的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (105, 16, 'Python面向对象', 2, '学习Python的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (106, 16, 'Python集合框架', 3, '学习Python的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (107, 16, 'Python IO操作', 4, '学习Python的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (108, 16, 'Python多线程', 5, '学习Python的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (111, 17, 'Python基础语法', 1, '学习Python的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (112, 17, 'Python面向对象', 2, '学习Python的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (113, 17, 'Python集合框架', 3, '学习Python的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (114, 17, 'Python IO操作', 4, '学习Python的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (115, 17, 'Python多线程', 5, '学习Python的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (118, 18, 'Python基础语法', 1, '学习Python的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (119, 18, 'Python面向对象', 2, '学习Python的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (120, 18, 'Python集合框架', 3, '学习Python的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (121, 18, 'Python IO操作', 4, '学习Python的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (122, 18, 'Python多线程', 5, '学习Python的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (125, 19, 'Python基础语法', 1, '学习Python的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (126, 19, 'Python面向对象', 2, '学习Python的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (127, 19, 'Python集合框架', 3, '学习Python的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (128, 19, 'Python IO操作', 4, '学习Python的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (129, 19, 'Python多线程', 5, '学习Python的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (132, 20, 'Python基础语法', 1, '学习Python的基本语法和数据类型', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (133, 20, 'Python面向对象', 2, '学习Python的面向对象编程特性', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (134, 20, 'Python集合框架', 3, '学习Python的集合框架和常用数据结构', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (135, 20, 'Python IO操作', 4, '学习Python的输入输出操作', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_chapter` (`id`, `path_id`, `name`, `order_num`, `description`, `create_time`, `update_time`) VALUES (136, 20, 'Python多线程', 5, '学习Python的多线程编程', '2026-03-20 04:35:06', '2026-03-20 04:35:06');

-- Table structure for path_level
DROP TABLE IF EXISTS `path_level`;
CREATE TABLE `path_level` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chapter_id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `order_num` int NOT NULL,
  `problem_ids` text,
  `knowledge_points` text,
  `unlock_condition` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `chapter_id` (`chapter_id`),
  CONSTRAINT `path_level_ibfk_1` FOREIGN KEY (`chapter_id`) REFERENCES `path_chapter` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=299 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for path_level
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (1, 1, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 03:37:03', '2026-03-20 03:37:03');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (2, 1, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 03:37:03', '2026-03-20 03:37:03');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (3, 1, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 03:37:03', '2026-03-20 03:37:03');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (4, 2, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 03:37:03', '2026-03-20 03:37:03');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (5, 2, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 03:37:03', '2026-03-20 03:37:03');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (6, 2, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 03:37:03', '2026-03-20 03:37:03');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (7, 3, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 03:37:03', '2026-03-20 03:37:03');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (8, 3, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 03:37:03', '2026-03-20 03:37:03');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (9, 3, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 03:37:03', '2026-03-20 03:37:03');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (10, 4, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 03:37:03', '2026-03-20 03:37:03');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (11, 4, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 03:37:03', '2026-03-20 03:37:03');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (12, 5, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 03:37:03', '2026-03-20 03:37:03');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (13, 5, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 03:37:03', '2026-03-20 03:37:03');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (14, 6, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (15, 6, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (16, 6, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (17, 7, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (18, 7, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (19, 7, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (20, 8, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (21, 8, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (22, 8, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (23, 9, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (24, 9, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (26, 10, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (27, 10, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (29, 13, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (30, 13, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (31, 13, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (32, 14, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (33, 14, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (34, 14, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (35, 15, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (36, 15, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (37, 15, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (38, 16, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (39, 16, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (41, 17, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (42, 17, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (44, 20, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (45, 20, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (46, 20, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (47, 21, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (48, 21, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (49, 21, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (50, 22, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (51, 22, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (52, 22, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (53, 23, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (54, 23, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (56, 24, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (57, 24, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (59, 27, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (60, 27, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (61, 27, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (62, 28, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (63, 28, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (64, 28, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (65, 29, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (66, 29, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (67, 29, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (68, 30, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (69, 30, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (71, 31, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (72, 31, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (74, 34, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (75, 34, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (76, 34, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (77, 35, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (78, 35, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (79, 35, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (80, 36, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (81, 36, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (82, 36, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (83, 37, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (84, 37, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (86, 38, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (87, 38, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (89, 41, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (90, 41, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (91, 41, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (92, 42, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (93, 42, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (94, 42, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (95, 43, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (96, 43, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (97, 43, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (98, 44, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (99, 44, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (101, 45, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (102, 45, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (104, 48, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (105, 48, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (106, 48, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (107, 49, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (108, 49, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (109, 49, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (110, 50, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (111, 50, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (112, 50, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (113, 51, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (114, 51, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (116, 52, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (117, 52, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (119, 55, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (120, 55, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (121, 55, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (122, 56, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (123, 56, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (124, 56, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (125, 57, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (126, 57, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (127, 57, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (128, 58, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (129, 58, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (131, 59, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (132, 59, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (134, 62, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (135, 62, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (136, 62, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (137, 63, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (138, 63, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (139, 63, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (140, 64, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (141, 64, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (142, 64, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (143, 65, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (144, 65, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (146, 66, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (147, 66, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (149, 69, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (150, 69, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (151, 69, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (152, 70, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (153, 70, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (154, 70, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (155, 71, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (156, 71, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (157, 71, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (158, 72, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (159, 72, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (161, 73, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (162, 73, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (164, 76, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (165, 76, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (166, 76, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (167, 77, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (168, 77, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (169, 77, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (170, 78, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (171, 78, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (172, 78, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (173, 79, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (174, 79, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (176, 80, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (177, 80, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (179, 83, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (180, 83, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (181, 83, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (182, 84, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (183, 84, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (184, 84, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (185, 85, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (186, 85, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (187, 85, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (188, 86, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (189, 86, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (191, 87, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (192, 87, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (194, 90, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (195, 90, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (196, 90, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (197, 91, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (198, 91, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (199, 91, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (200, 92, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (201, 92, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (202, 92, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (203, 93, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (204, 93, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (206, 94, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (207, 94, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (209, 97, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (210, 97, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (211, 97, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (212, 98, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (213, 98, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (214, 98, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (215, 99, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (216, 99, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (217, 99, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (218, 100, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (219, 100, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (221, 101, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (222, 101, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (224, 104, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (225, 104, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (226, 104, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (227, 105, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (228, 105, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (229, 105, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (230, 106, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (231, 106, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (232, 106, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (233, 107, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (234, 107, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (236, 108, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (237, 108, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (239, 111, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (240, 111, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (241, 111, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (242, 112, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (243, 112, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (244, 112, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (245, 113, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (246, 113, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (247, 113, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (248, 114, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (249, 114, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (251, 115, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (252, 115, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (254, 118, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (255, 118, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (256, 118, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (257, 119, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (258, 119, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (259, 119, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (260, 120, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (261, 120, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (262, 120, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (263, 121, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (264, 121, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (266, 122, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (267, 122, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (269, 125, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (270, 125, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (271, 125, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (272, 126, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (273, 126, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (274, 126, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (275, 127, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (276, 127, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (277, 127, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (278, 128, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (279, 128, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (281, 129, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (282, 129, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (284, 132, '变量和数据类型', 1, '1,2,3', '变量,数据类型,运算符', '', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (285, 132, '控制流语句', 2, '4,5,6', 'if语句,循环语句,switch语句', '1', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (286, 132, '方法和数组', 3, '7,8,9', '方法定义,方法调用,数组', '2', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (287, 133, '类和对象', 1, '10,11,12', '类定义,对象创建,构造方法', '3', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (288, 133, '继承和多态', 2, '13,14,15', '继承,多态,抽象类', '4', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (289, 133, '接口和枚举', 3, '16,17,18', '接口定义,接口实现,枚举', '5', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (290, 134, 'List集合', 1, '19,20,21', 'ArrayList,LinkedList,List接口', '6', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (291, 134, 'Set和Map集合', 2, '22,23,24', 'HashSet,TreeSet,HashMap', '7', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (292, 134, '集合工具类', 3, '25,26,27', 'Collections,Arrays', '8', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (293, 135, '文件操作', 1, '28,29,30', 'File类,文件读写,异常处理', '9', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (294, 135, '流操作', 2, '31,32,33', '字节流,字符流,缓冲流', '10', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (296, 136, '线程基础', 1, '34,35,36', 'Thread类,Runnable接口,线程创建', '11', '2026-03-20 04:35:06', '2026-03-20 04:35:06');
INSERT INTO `path_level` (`id`, `chapter_id`, `name`, `order_num`, `problem_ids`, `knowledge_points`, `unlock_condition`, `create_time`, `update_time`) VALUES (297, 136, '线程同步', 2, '37,38,39', 'synchronized,volatile,线程安全', '12', '2026-03-20 04:35:06', '2026-03-20 04:35:06');

-- Table structure for path_level_problem
DROP TABLE IF EXISTS `path_level_problem`;
CREATE TABLE `path_level_problem` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `level_id` bigint NOT NULL,
  `problem_id` bigint NOT NULL,
  `order_num` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level_problem` (`level_id`,`problem_id`),
  KEY `idx_level_id` (`level_id`),
  KEY `idx_problem_id` (`problem_id`),
  CONSTRAINT `path_level_problem_ibfk_1` FOREIGN KEY (`level_id`) REFERENCES `path_level` (`id`) ON DELETE CASCADE,
  CONSTRAINT `path_level_problem_ibfk_2` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data for path_level_problem

-- Table structure for permission
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `code` varchar(100) NOT NULL,
  `description` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for permission
INSERT INTO `permission` (`id`, `name`, `code`, `description`, `create_time`) VALUES (1, '用户管理', 'user_manage', '管理用户账号', '2026-03-20 00:21:13');
INSERT INTO `permission` (`id`, `name`, `code`, `description`, `create_time`) VALUES (2, '题目管理', 'problem_manage', '管理编程题目', '2026-03-20 00:21:13');
INSERT INTO `permission` (`id`, `name`, `code`, `description`, `create_time`) VALUES (3, '学习路径管理', 'path_manage', '管理学习路径', '2026-03-20 00:21:13');
INSERT INTO `permission` (`id`, `name`, `code`, `description`, `create_time`) VALUES (4, '知识点管理', 'knowledge_manage', '管理知识点', '2026-03-20 00:21:13');
INSERT INTO `permission` (`id`, `name`, `code`, `description`, `create_time`) VALUES (5, '数据分析', 'data_analysis', '查看数据分析报表', '2026-03-20 00:21:13');
INSERT INTO `permission` (`id`, `name`, `code`, `description`, `create_time`) VALUES (6, '系统监控', 'system_monitor', '监控系统运行状态', '2026-03-20 00:21:13');
INSERT INTO `permission` (`id`, `name`, `code`, `description`, `create_time`) VALUES (7, '内容审核', 'content_audit', '审核用户内容', '2026-03-20 00:21:13');

-- Table structure for problem
DROP TABLE IF EXISTS `problem`;
CREATE TABLE `problem` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `title` varchar(100) NOT NULL COMMENT '题目名称',
  `content` text NOT NULL COMMENT '题目描述',
  `input` varchar(200) DEFAULT '' COMMENT '输入示例',
  `output` varchar(200) NOT NULL COMMENT '输出示例（标准答案）',
  `difficulty` tinyint DEFAULT '0' COMMENT '难度：0-简单，1-中等，2-困难',
  `language` varchar(20) DEFAULT 'java' COMMENT '编程语言',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `time_limit` int DEFAULT '1000' COMMENT '时间限制（毫秒）',
  `memory_limit` int DEFAULT '256' COMMENT '内存限制（MB）',
  `tags` varchar(500) DEFAULT NULL COMMENT '题目标签，逗号分隔',
  `knowledge_points` varchar(500) DEFAULT NULL COMMENT '知识点，逗号分隔',
  `hints` text COMMENT '提示信息，JSON格式',
  `sample_explanation` text COMMENT '示例解释',
  `chapter_id` bigint DEFAULT NULL,
  `level_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tags` (`tags`(255)),
  KEY `idx_difficulty` (`difficulty`)
) ENGINE=InnoDB AUTO_INCREMENT=93 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题库表';

-- Data for problem
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (1, '两数之和', '给定两个整数a和b，计算它们的和并输出。', '两个整数，用空格分隔', '一个整数', 0, 'java', '2026-03-20 05:29:40', '2026-03-20 05:29:40', 1000, 256, '基础语法,数学运算', '变量,数据类型,输入输出', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (2, '求最大值', '给定两个整数a和b，输出较大的那个数。', '两个整数，用空格分隔', '一个整数', 0, 'java', '2026-03-20 05:29:40', '2026-03-20 05:29:40', 1000, 256, '基础语法,条件判断', '条件判断,if语句', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (3, '求绝对值', '给定一个整数，输出它的绝对值。', '一个整数', '一个整数', 0, 'java', '2026-03-20 05:29:40', '2026-03-20 05:29:40', 1000, 256, '基础语法,数学运算', '条件判断,数学运算', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (4, '判断闰年', '给定一个年份，判断它是否是闰年。', '一个整数，表示年份', '是或否', 0, 'java', '2026-03-20 05:29:40', '2026-03-20 05:29:40', 1000, 256, '基础语法,条件判断', '条件判断,逻辑运算', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (5, '判断奇偶', '给定一个整数，判断它是奇数还是偶数。', '一个整数', '奇数或偶数', 0, 'java', '2026-03-20 05:29:40', '2026-03-20 05:29:40', 1000, 256, '基础语法,条件判断', '取模运算,条件判断', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (6, '斐波那契数列', '给定正整数n，输出斐波那契数列的第n项。', '一个正整数n', '一个整数', 1, 'java', '2026-03-20 05:29:40', '2026-03-20 05:29:40', 1000, 256, '循环,递归', '循环,递归', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (7, '素数判断', '给定一个正整数，判断它是否是素数。', '一个正整数', '是或否', 1, 'java', '2026-03-20 05:29:40', '2026-03-20 05:29:40', 1000, 256, '循环,数学运算', '循环,取模运算', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (8, '冒泡排序', '输入n个整数，用冒泡排序算法将其从小到大排序后输出。', '第一行是n，第二行是n个整数，用空格分隔', 'n个整数，用空格分隔', 2, 'java', '2026-03-20 05:29:40', '2026-03-20 05:29:40', 1000, 256, '排序,数组', '冒泡排序,双重循环', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (9, '两数之和', '给定两个整数a和b，计算它们的和并输出。', '两个整数，用空格分隔', '一个整数', 0, 'python', '2026-03-20 05:29:40', '2026-03-20 05:29:40', 1000, 256, '基础语法,数学运算', '变量,数据类型,输入输出', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (10, '求最大值', '给定两个整数a和b，输出较大的那个数。', '两个整数，用空格分隔', '一个整数', 0, 'python', '2026-03-20 05:29:40', '2026-03-20 05:29:40', 1000, 256, '基础语法,条件判断', '条件判断,if语句', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (11, '字符串反转', '给定一个字符串，输出其反转后的结果。', '一个字符串', '一个字符串', 1, 'python', '2026-03-20 05:29:40', '2026-03-20 05:29:40', 1000, 256, '字符串,切片', '字符串操作,切片', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (12, '列表求和', '给定n个整数，求它们的和。', '第一行是n，第二行是n个整数，用空格分隔', '一个整数', 0, 'python', '2026-03-20 05:29:40', '2026-03-20 05:29:40', 1000, 256, '列表,循环', '列表操作,累加', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (13, '求最小值', '给定两个整数a和b，输出较小的那个数。', '两个整数，用空格分隔', '一个整数', 0, 'java', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '基础语法,条件判断', '条件判断,if语句', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (14, '交换两个数', '给定两个整数a和b，交换它们的值后输出。', '两个整数，用空格分隔', '两个整数，用空格分隔', 0, 'java', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '基础语法,变量', '变量,赋值语句', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (15, '三数之和', '给定三个整数a、b、c，计算它们的和并输出。', '三个整数，用空格分隔', '一个整数', 0, 'java', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '基础语法,数学运算', '变量,数据类型,输入输出', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (16, '三数最大值', '给定三个整数a、b、c，输出最大的那个数。', '三个整数，用空格分隔', '一个整数', 0, 'java', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '基础语法,条件判断', '条件判断,if语句', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (17, '阶乘计算', '给定正整数n，计算n的阶乘（n!）。', '一个正整数n', '一个整数', 1, 'java', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '循环,数学运算', 'for循环,累乘', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (18, '水仙花数', '判断一个三位数是否是水仙花数（各位立方和等于自身）。', '一个三位数', '是或否', 1, 'java', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '数学运算,条件判断', '取模运算,条件判断', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (19, '数组最大值', '给定n个整数，找出其中的最大值。', '第一行是n，第二行是n个整数，用空格分隔', '一个整数', 1, 'java', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '数组,循环', '数组遍历,比较', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (20, '数组平均值', '给定n个整数，计算它们的平均值。', '第一行是n，第二行是n个整数，用空格分隔', '一个浮点数', 1, 'java', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '数组,循环', '数组操作,类型转换', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (21, '选择排序', '输入n个整数，用选择排序算法将其从小到大排序后输出。', '第一行是n，第二行是n个整数，用空格分隔', 'n个整数，用空格分隔', 2, 'java', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '排序,数组', '选择排序,双重循环', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (22, '插入排序', '输入n个整数，用插入排序算法将其从小到大排序后输出。', '第一行是n，第二行是n个整数，用空格分隔', 'n个整数，用空格分隔', 2, 'java', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '排序,数组', '插入排序,双重循环', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (23, '求绝对值', '给定一个整数，输出它的绝对值。', '一个整数', '一个整数', 0, 'python', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '基础语法,数学运算', '条件判断,数学运算', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (24, '判断闰年', '给定一个年份，判断它是否是闰年。', '一个整数，表示年份', '是或否', 0, 'python', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '基础语法,条件判断', '条件判断,逻辑运算', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (25, '判断奇偶', '给定一个整数，判断它是奇数还是偶数。', '一个整数', '奇数或偶数', 0, 'python', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '基础语法,条件判断', '取模运算,条件判断', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (26, '斐波那契数列', '给定正整数n，输出斐波那契数列的第n项。', '一个正整数n', '一个整数', 1, 'python', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '循环,递归', '循环,递归', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (27, '素数判断', '给定一个正整数，判断它是否是素数。', '一个正整数', '是或否', 1, 'python', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '循环,数学运算', '循环,取模运算', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (28, '列表最大值', '给定n个整数，找出其中的最大值。', '第一行是n，第二行是n个整数，用空格分隔', '一个整数', 1, 'python', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '列表,循环', '列表遍历,比较', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (29, '列表最小值', '给定n个整数，找出其中的最小值。', '第一行是n，第二行是n个整数，用空格分隔', '一个整数', 1, 'python', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '列表,循环', '列表遍历,比较', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (30, '列表排序', '给定n个整数，将它们从小到大排序后输出。', '第一行是n，第二行是n个整数，用空格分隔', 'n个整数，用空格分隔', 1, 'python', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '列表,排序', '列表操作,sort方法', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (31, '阶乘计算', '给定正整数n，计算n的阶乘（n!）。', '一个正整数n', '一个整数', 1, 'python', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '循环,数学运算', 'for循环,累乘', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (32, '冒泡排序', '输入n个整数，用冒泡排序算法将其从小到大排序后输出。', '第一行是n，第二行是n个整数，用空格分隔', 'n个整数，用空格分隔', 2, 'python', '2026-03-20 05:31:07', '2026-03-20 05:31:07', 1000, 256, '排序,列表', '冒泡排序,双重循环', NULL, NULL, NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (33, '变量声明与初始化', '声明一个整型变量age并初始化为18，声明一个字符串变量name并初始化为"张三"，然后输出这两个变量的值。', '', '18
张三', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,变量', '变量', '使用int声明整型，使用String声明字符串类型', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (34, '变量交换', '给定两个整数a和b，交换它们的值并输出。', '3 5', '5 3', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,变量', '变量', '可以使用临时变量来实现交换', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (35, '基本数据类型练习', '分别声明byte、short、int、long、float、double、boolean、char类型的变量各一个，并给它们赋合理的值，然后输出。', '', '10
100
1000
10000
3.14
3.1415926
true
A', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,数据类型', '数据类型', '注意不同数据类型的取值范围', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (36, '读取输入并输出', '从控制台读取一个整数和一个字符串，然后按格式输出："你输入的整数是：x，字符串是：y"。', '100 Hello', '你输入的整数是：100，字符串是：Hello', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,输入输出', '输入输出', '使用Scanner类来读取输入', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (37, '格式化输出', '输入一个浮点数，表示圆的半径，计算圆的面积并保留2位小数输出。', '5.0', '78.54', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,输入输出', '输入输出', '使用String.format或System.out.printf进行格式化', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (38, '判断正数负数', '输入一个整数，判断它是正数、负数还是零，并输出相应的提示。', '-5', '这是一个负数', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,条件判断', '条件判断', '使用if-else语句进行判断', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (39, '成绩等级判断', '输入一个0-100的分数，根据分数输出等级：90-100为A，80-89为B，70-79为C，60-69为D，60以下为E。', '85', 'B', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,条件判断', '条件判断', '使用if-else if-else语句', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (40, '闰年判断', '输入一个年份，判断它是否是闰年。闰年的判断条件：能被4整除但不能被100整除，或者能被400整除。', '2024', '2024是闰年', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,条件判断', 'if语句', '使用逻辑运算符&amp;&amp;和||', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (41, '计算1到n的和', '输入一个正整数n，计算1+2+3+...+n的和并输出。', '10', '55', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,循环', '循环', '使用for循环累加', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (42, '输出乘法表', '输入一个正整数n，输出n×n的乘法表。', '5', '1×1=1
1×2=2 2×2=4
...', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,循环', '循环', '使用双重for循环', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (43, '求阶乘', '输入一个非负整数n，计算n的阶乘（n! = 1×2×3×...×n）。', '5', '120', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,循环', 'for循环', '注意0! = 1', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (44, '数字反转', '输入一个正整数，将其各位数字反转后输出。', '12345', '54321', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,循环', 'while循环', '使用取模和除法操作', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (45, '数组求和', '输入一个整数数组，计算数组中所有元素的和并输出。', '5
1 2 3 4 5', '15', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '数组,基础语法', '数组', '先读取数组长度，再读取数组元素', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (46, '找数组最大值', '输入一个整数数组，找出数组中的最大值并输出。', '6
3 7 2 9 5 1', '9', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '数组,基础语法', '数组', '假设第一个元素是最大值，然后依次比较', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (47, '数组逆序', '输入一个整数数组，将数组元素逆序排列后输出。', '5
1 2 3 4 5', '5 4 3 2 1', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '数组,基础语法', '一维数组', '交换首尾元素，向中间靠拢', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (48, '字符串长度', '输入一个字符串，输出它的长度。', 'Hello World', '11', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '字符串,基础语法', '字符串', '使用String的length()方法', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (49, '字符串拼接', '输入两个字符串，将它们拼接后输出。', 'Hello
World', 'HelloWorld', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '字符串,基础语法', '字符串', '使用+运算符进行字符串拼接', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (50, '字符串大小写转换', '输入一个字符串，将其全部转换为大写和小写后分别输出。', 'Hello', 'HELLO
hello', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '字符串,String类', 'String类', '使用toUpperCase()和toLowerCase()方法', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (51, '判断子串', '输入两个字符串，判断第一个字符串是否包含第二个字符串作为子串。', 'Hello World
World', '包含', 1, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '字符串,String类', 'String类', '使用contains()方法', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (52, '统计字符出现次数', '输入一个字符串和一个字符，统计该字符在字符串中出现的次数。', 'abracadabra
a', '5', 2, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '字符串,字符串操作', '字符串操作', '遍历字符串的每个字符进行比较', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (53, '定义求和方法', '定义一个方法sum(int a, int b)，返回两个整数的和。在main方法中调用该方法并输出结果。', '3 5', '8', 2, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '方法,基础语法', '方法', '方法的返回类型为int', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (54, '判断素数方法', '定义一个方法isPrime(int n)，判断n是否是素数，返回boolean类型。', '17', 'true', 2, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '方法,方法定义', '方法定义', '素数只能被1和它本身整除', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (55, '数组作为参数', '定义一个方法printArray(int[] arr)，打印数组的所有元素。', '5
1 2 3 4 5', '1 2 3 4 5', 2, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '方法,方法参数', '方法参数', '数组作为参数传递时传递的是引用', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (56, '返回数组最大值', '定义一个方法findMax(int[] arr)，返回数组中的最大值。', '5
3 7 2 9 5', '9', 2, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '方法,返回值', '返回值', '遍历数组找到最大值后返回', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (57, '重载求和方法', '重载sum方法：一个接受两个int参数，一个接受两个double参数，分别返回它们的和。', '3 5
2.5 3.5', '8
6.0', 2, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '方法,方法重载', '方法重载', '方法名相同但参数列表不同', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (58, '递归求阶乘', '使用递归计算n的阶乘。', '5', '120', 2, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '递归,算法', '递归', 'n! = n × (n-1)!，递归出口是0! = 1', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (59, '斐波那契数列', '使用递归求斐波那契数列的第n项。', '10', '55', 2, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '递归,算法', '递归', 'F(n) = F(n-1) + F(n-2)，F(1)=1, F(2)=1', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (60, '定义学生类', '定义一个Student类，包含name和age属性，以及一个显示信息的方法showInfo()。', '张三 18', '姓名：张三，年龄：18', 2, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '面向对象,类', '类', '使用class关键字定义类', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (61, '创建和使用对象', '创建Student类的对象，设置其属性，然后调用方法。', '李四 20', '姓名：李四，年龄：20', 2, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '面向对象,对象', '对象', '使用new关键字创建对象', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (62, '使用getter和setter', '修改Student类，将属性私有化，提供getter和setter方法。', '王五 22', '姓名：王五，年龄：22', 2, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '面向对象,封装', '封装', '使用private修饰属性，public修饰getter/setter', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (63, '理解访问修饰符', '创建一个类，演示public、protected、default、private四种访问修饰符的使用。', '', '演示完成', 2, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '面向对象,访问修饰符', '访问修饰符', 'public最开放，private最严格', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (64, '定义继承关系', '定义Person类作为父类，Student类继承Person类，添加学号属性。', '张三 18 2024001', '姓名：张三，年龄：18，学号：2024001', 3, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 2000, 512, '面向对象,继承', '继承', '使用extends关键字实现继承', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (65, '方法重写', '在子类中重写父类的方法，演示多态的使用。', '', '这是子类的方法', 3, 'java', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 2000, 512, '面向对象,多态', '多态', '使用@Override注解', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (66, '变量赋值与输出', '定义一个整数变量age = 18，一个字符串变量name = "张三"，然后输出这两个变量。', '', '18
张三', 1, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,变量', '变量', 'Python变量不需要声明类型', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (67, 'Python基本数据类型', '分别创建整数、浮点数、字符串、布尔值、列表、字典类型的变量并输出。', '', '10
3.14
Hello
True
[1, 2, 3]
{"name": "张三"}', 1, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,数据类型', '数据类型', 'Python是动态类型语言', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (68, '读取输入', '从控制台读取一个整数和一个字符串，然后输出。', '100 Hello', '整数：100，字符串：Hello', 1, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,输入输出', '输入输出', '使用input()函数读取输入', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (69, '判断大小', '输入两个数，判断第一个数是否大于第二个数。', '5 3', '5大于3', 1, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,条件判断', '条件判断', '使用if-else语句', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (70, '判断季节', '输入月份(1-12)，判断所属季节。', '3', '春季', 1, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,条件判断', 'if语句', '3-5月为春季，6-8月为夏季，9-11月为秋季，12-2月为冬季', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (71, '累加求和', '计算1到100的和。', '', '5050', 1, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,循环', '循环', '使用for循环和range()函数', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (72, '遍历列表', '创建一个列表，使用for循环遍历并输出每个元素。', '', 'apple
banana
cherry', 1, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,循环', 'for循环', 'for item in list:', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (73, '猜数字游戏', '使用while循环实现一个简单的猜数字游戏。', '50', '恭喜你猜对了！', 1, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '基础语法,循环', 'while循环', '设置一个答案，让用户输入猜测', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (74, '列表操作', '创建一个列表，进行添加、删除、修改操作。', '', '[1, 2, 3, 4]
[1, 2, 4]', 1, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '数据结构,列表', '列表', '使用append()、pop()等方法', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (75, '列表切片', '使用切片操作获取列表的子列表。', '', '[2, 3, 4]', 1, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '数据结构,列表', '列表', 'list[1:4]', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (76, '列表切片操作', '对列表[1,2,3,4,5,6,7,8,9,10]进行切片，获取索引2到7的元素。', '', '[3, 4, 5, 6, 7, 8]', 1, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '数据结构,列表,切片', '切片', '注意切片是左闭右开区间', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (77, '字典的使用', '创建一个字典存储学生信息，包括姓名、年龄、学号，然后输出。', '', '姓名：张三，年龄：18，学号：2024001', 1, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '数据结构,字典', '字典', '使用键值对存储数据', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (78, '元组操作', '创建一个元组，演示元组的解包操作。', '', 'a=1, b=2, c=3', 1, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '数据结构,元组', '元组', '元组是不可变的', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (79, '字符串反转', '输入一个字符串，输出它的反转字符串。', 'Hello', 'olleH', 1, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '字符串,基础语法', '字符串', '使用切片s[::-1]', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (80, '统计词频', '输入一段英文文本，统计每个单词出现的次数。', 'hello world hello python', 'hello: 2
world: 1
python: 1', 2, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '字符串,字符串操作', '字符串操作', '使用字典统计', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (81, '定义函数', '定义一个函数add(a, b)，返回两个数的和。', '3 5', '8', 1, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1000, 256, '函数,基础语法', '函数', '使用def关键字定义函数', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (82, '函数默认参数', '定义一个带有默认参数的函数。', '', 'Hello, World!', 2, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '函数,函数定义', '函数定义', 'def greet(name="World"):', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (83, '可变参数', '定义一个函数，接受任意数量的参数并返回它们的和。', '1 2 3 4 5', '15', 2, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '函数,函数参数', '函数参数', '使用*args', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (84, '返回多个值', '定义一个函数，返回两个值。', '10 3', '商：3，余数：1', 2, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '函数,返回值', '返回值', 'Python函数可以返回元组', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (85, '使用lambda', '使用lambda函数定义一个简单的加法函数。', '5 3', '8', 2, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '函数,lambda函数', 'lambda函数', 'add = lambda x, y: x + y', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (86, '递归计算阶乘', '使用递归计算n的阶乘。', '5', '120', 2, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '递归,算法', '递归', 'n! = n * (n-1)!', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (87, '定义类', '定义一个Person类，包含name和age属性。', '张三 18', '姓名：张三，年龄：18', 2, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '面向对象,类', '类', '使用class关键字', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (88, '创建对象', '创建Person类的实例并使用。', '李四 20', '姓名：李四，年龄：20', 2, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '面向对象,对象', '对象', 'p = Person("李四", 20)', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (89, '使用属性装饰器', '使用@property装饰器实现getter和setter。', '25', '年龄：25', 2, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 1500, 384, '面向对象,封装', '封装', '@property和@age.setter', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (90, '类的继承', '定义Animal类，然后定义Dog类继承Animal。', '', '汪汪汪', 3, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 2000, 512, '面向对象,继承', '继承', 'class Dog(Animal):', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (91, '方法重写', '在子类中重写父类的方法。', '', '这是Dog的speak方法', 3, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 2000, 512, '面向对象,多态', '多态', 'Python支持动态多态', '', NULL, NULL);
INSERT INTO `problem` (`id`, `title`, `content`, `input`, `output`, `difficulty`, `language`, `create_time`, `update_time`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `chapter_id`, `level_id`) VALUES (92, '实现__str__方法', '在类中实现__str__方法，使打印对象时有友好的输出。', '', 'Person: 张三, 18', 3, 'python', '2026-03-20 06:21:23', '2026-03-20 06:21:23', 2000, 512, '面向对象,魔术方法', '魔术方法', 'def __str__(self):', '', NULL, NULL);

-- Table structure for question
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '问答ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content` text NOT NULL COMMENT '用户问题',
  `answer` text NOT NULL COMMENT 'AI回答',
  `code` text COMMENT '关联代码',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI问答记录表';

-- Data for question
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (1, 1, '如何实现冒泡排序？', 'AI模拟回答：这是一个很好的编程问题！根据你的代码，我建议你检查语法和逻辑。如果有具体的错误信息，请提供给我，我会帮你分析解决。', 'for i in range(n): for j in range(i, n): if arr[j] > arr[i]: arr[j], arr[i] = arr[i]', '2026-03-12 00:42:22');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (2, 1, '如何实现冒泡排序？', '你提供的代码片段是冒泡排序的一个初步尝试，但还存在一些问题。我们来逐步分析并改进它。

---

### 一、理解冒泡排序的原理

冒泡排序的基本思想是：

- 从数组的第一个元素开始，依次比较相邻的两个元素。
- 如果前一个元素比后一个元素大，就交换它们的位置。
- 重复这个过程，直到没有需要交换的元素为止（即数组已经有序）。
- 每一轮遍历会将最大的元素“冒泡”到数组的末尾。

---

### 二、你的代码分析

你写的代码如下：

```python
for i in range(n):
    for j in range(i, n):
        if arr[j] > arr[i]:
            arr[j], arr[i] = arr[i], arr[j]
```

#### 存在的问题：

1. **外层循环 `i` 的范围应该不是 `range(n)`**，而是 `range(n - 1)`，因为最后一位已经是最大的了，不需要再比较。
2. **内层循环 `j` 的起始位置应该是 `0` 而不是 `i`**，否则无法正确比较相邻元素。
3. **逻辑错误**：你是在比较 `arr[j]` 和 `arr[i]`，而不是 `arr[j]` 和 `arr[j+1]`，这会导致逻辑错误。

---

### 三、正确的冒泡排序思路

1. 外层循环：从第一个元素到最后一个元素（不包括最后一个，因为每次都会把最大的“冒泡”到后面）。
2. 内层循环：从第一个元素到当前未排序部分的最后一个元素。
3. 在内层循环中，比较相邻元素，如果前面的比后面的大，就交换它们。

---

### 四、示例代码结构（提示）

你可以参考以下结构来编写代码：

```python
for i in range(len(arr) - 1):  # 外层循环控制轮数
    for j in range(0, len(arr) - i - 1):  # 内层循环比较相邻元素
        if arr[j] > arr[j + 1]:
            arr[j], arr[j + 1] = arr[j + 1], arr[j]
```

---

### 五、思考问题（帮助你理解）

- 为什么外层循环只需要 `len(arr) - 1` 次？
- 为什么内层循环的上限是 `len(arr) - i - 1`？
- 如果数组已经有序，如何优化算法？

---

如果你能根据这些提示写出自己的代码，那你就真正掌握了冒泡排序的原理！需要进一步解释的话，可以随时问我。', 'for i in range(n): for j in range(i, n): if arr[j] > arr[i]: arr[j], arr[i] = arr[i]', '2026-03-12 00:45:42');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (3, 1, '做一个自我介绍', '好的，我们可以一步一步来实现“自我介绍”的功能。首先，我们需要明确“自我介绍”具体要包含哪些信息，比如：姓名、年龄、爱好、专业等。然后，我们可以通过编程将这些信息组织起来并输出。

### 思路分析：

1. **确定需要展示的信息**  
   例如：姓名、年龄、爱好、学校、专业等。

2. **使用变量存储信息**  
   可以用字符串（`str`）或其它数据类型保存这些信息。

3. **输出信息**  
   使用 `print()` 函数将信息打印出来。

---

### 示例代码结构（以 Python 为例）：

```python
# 定义个人信息
name = "张三"
age = 20
hobby = "打篮球"
school = "XX大学"
major = "计算机科学"

# 输出自我介绍
print("大家好，我叫" + name + "，今年" + str(age) + "岁。")
print("我的爱好是" + hobby + "，目前在" + school + "学习" + major + "。")
```

### 小提示：
- 如果你使用的是其他语言（如 Java、C++、JavaScript 等），也可以按照类似的思路进行。
- 注意类型转换：如果要将数字和字符串拼接，需要先将数字转为字符串（如 `str(age)`）。

你可以根据自己的需求修改这些信息，或者添加更多内容！需要我帮你扩展功能吗？比如让用户输入信息再输出？', ' ', '2026-03-12 00:47:28');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (4, 1, 'Java循环为什么死循环？', '这是一个典型的**死循环**问题，我们来分析一下原因，并引导你理解如何避免它。

---

### 一、问题分析

你的代码是：

```java
for(int i=0; i>=0; ) {}
```

这个 `for` 循环的结构是：

```java
for(初始化; 条件; 步进) {
    // 循环体
}
```

在这个例子中：
- **初始化**：`int i = 0`
- **条件**：`i >= 0`
- **步进**：没有写（即空）

---

### 二、为什么是死循环？

1. **初始值**：`i = 0`，满足 `i >= 0`，所以第一次进入循环。
2. **循环体**：里面什么都没有（空语句 `{}`）。
3. **步进**：没有改变 `i` 的值，因此 `i` 始终是 `0`。
4. **条件判断**：`i >= 0` 永远为真，循环不会停止。

👉 所以这个循环会一直执行下去，形成**死循环**。

---

### 三、如何理解循环结构？

你可以这样思考循环的执行流程：

```
初始化 → 判断条件 → 执行循环体 → 执行步进 → 再次判断条件
```

如果在**步进**部分没有修改变量，就会导致条件永远为真，从而进入死循环。

---

### 四、如何修改成正常循环？

举个例子，如果你想让循环执行一定次数，比如 5 次，可以这样写：

```java
for(int i = 0; i < 5; i++) {
    System.out.println(i);
}
```

或者如果你想用 `while` 实现类似功能：

```java
int i = 0;
while (i < 5) {
    System.out.println(i);
    i++;
}
```

---

### 五、总结

- **死循环**是因为循环条件永远为真，且没有更新变量。
- 在 `for` 循环中，**步进部分**非常重要，不要漏掉。
- 如果循环体为空，也要注意是否会导致死循环。

---

如果你有其他循环问题，也可以继续提问！', 'for(int i=0;i>=0;){}', '2026-03-12 14:37:51');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (5, 1, '运行错误：Main.java:5: error: class, interface, or enum expected
}1
 ^
1 error
解决报错', '这个错误信息 `Main.java:5: error: class, interface, or enum expected` 通常表示 Java 编译器在代码中发现了语法错误，特别是在类、接口或枚举的定义上。

### 错误分析：
你提供的代码看起来是正确的，但根据错误提示，编译器在第 5 行（即 `}`）附近发现了问题。这可能是因为：

1. **文件中存在多余的字符或隐藏符号**：例如，在代码末尾多了一个 `1` 或其他非代码字符。
2. **文件编码或格式问题**：有时复制粘贴时会引入不可见字符。
3. **Java 文件与实际代码不一致**：比如你保存的文件内容和实际运行的代码不一致。

---

### 解决思路：

1. **检查代码是否完整**  
   确保你的代码没有多余的内容，比如结尾的 `1` 或其他无关字符。你提供的代码中最后一行是 `}1`，这可能是导致错误的原因。

2. **删除多余内容**  
   删除所有非代码内容，只保留以下内容：

   ```java
   public class Main {
       public static void main(String[] args) {
           System.out.println("Hello, World!");
       }
   }
   ```

3. **重新保存文件**  
   用文本编辑器（如 Notepad++、VS Code、IntelliJ IDEA）打开文件，确保文件名是 `Main.java`，并且没有额外的字符。

4. **重新编译和运行**  
   使用命令行或 IDE 重新编译并运行程序。

---

### 示例操作步骤：

- 打开 `Main.java`
- 检查最后一行是否为 `}`，而不是 `}1`
- 如果有 `}1`，请删除它
- 保存文件
- 在终端执行：
  ```bash
  javac Main.java
  java Main
  ```

---

### 总结：
这个错误通常是由于代码中存在多余字符或格式问题引起的。检查并删除这些多余内容后，应该可以正常编译和运行。', 'public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}1', '2026-03-12 18:21:40');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (6, 1, '运行错误：Main.java:5: error: class, interface, or enum expected
}1
 ^
1 error
解决报错', '这个错误信息 `Main.java:5: error: class, interface, or enum expected` 通常表示 Java 编译器在代码中发现了语法错误，特别是在类、接口或枚举的定义位置出现了不符合预期的内容。

### 错误分析：
你提供的代码中，最后一行是 `}1`，这显然不是一个合法的 Java 语法结构。Java 编译器在解析到 `}` 后，期望看到的是类、接口或枚举的定义，但这里却出现了 `1`，导致编译器报错。

### 解决思路：
1. **检查代码末尾**：确保代码的结尾没有多余的字符（如 `}1`）。
2. **检查括号匹配**：确保所有的 `{` 和 `}` 是成对出现的，并且正确闭合。
3. **删除多余内容**：如果 `}1` 是误输入或多余内容，直接删除即可。

### 示例修正：
你的原始代码应该是这样的：

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

而你实际的代码中可能多了一个 `}1`，变成这样：

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}1
```

### 修正方法：
删除最后一行的 `}1`，只保留正确的类定义和方法结构即可。

---

如果你还有其他问题，比如代码中有多个错误或不确定哪里出错了，可以继续提供完整代码，我会帮你一步步排查！', 'public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}1', '2026-03-12 21:04:46');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (7, 1, '你好', '你好！我是你的编程助教，可以帮你解决编程相关的问题。请告诉我你遇到的编程问题，我会给你清晰的思路和示例代码，帮助你理解原理。', NULL, '2026-03-13 04:00:47');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (8, 1, '做一个自我介绍

', '好的，我们可以从编程的角度来“做一个自我介绍”，这其实是一个很基础的程序设计问题。你可以把它理解为：**如何用代码表达一个对象的基本信息和行为**。

### 思路分析：

1. **定义一个类（Class）**：比如 `Person`，用来表示“人”这个实体。
2. **构造函数（Constructor）**：在初始化对象时，设置一些属性，比如姓名、年龄、兴趣等。
3. **方法（Method）**：比如 `introduce()`，用来输出自我介绍的内容。
4. **使用面向对象的思想**：将数据和操作封装在一起。

---

### 示例思路（以 Python 为例）：

```python
class Person:
    def __init__(self, name, age, hobby):
        # 初始化属性
        self.name = name
        self.age = age
        self.hobby = hobby

    def introduce(self):
        # 输出自我介绍
        print(f"大家好，我叫 {self.name}，今年 {self.age} 岁，我喜欢 {self.hobby}。")

# 创建一个 Person 对象
me = Person("张三", 25, "编程")
me.introduce()
```

### 你也可以尝试扩展它：
- 添加更多属性（如性别、职业）
- 添加更多方法（如说笑话、唱歌）

---

如果你有特定的编程语言或更具体的需求（比如用 Java、C++ 或者想加入图形界面），可以告诉我，我可以进一步帮你设计。', NULL, '2026-03-13 04:02:07');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (9, 1, '做一个自我介绍', '好的，我们可以用编程的方式做一个“自我介绍”。这个问题可以锻炼你对变量、字符串操作以及输出语句的理解。

### 思路分析：

1. **定义变量**：你可以用变量存储你的名字、年龄、爱好等信息。
2. **使用字符串拼接或格式化**：将这些变量组合成一个完整的句子。
3. **输出结果**：使用 `print()` 函数把自我介绍打印出来。

### 示例代码思路（以 Python 为例）：

```python
# 定义变量
name = "张三"
age = 20
hobby = "打篮球"

# 使用字符串格式化输出
print(f"大家好，我叫{name}，今年{age}岁，我喜欢{hobby}。")
```

### 你可以尝试：
- 改变变量的值，看看输出是否变化？
- 尝试使用其他方式（如 `+` 拼接字符串）来实现同样的功能？

如果你有特定的编程语言要求，也可以告诉我，我可以提供对应语言的示例。', NULL, '2026-03-13 04:15:08');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (10, 1, '做一个自我介绍', '好的，我们可以用编程的方式做一个“自我介绍”。这个问题可以锻炼你对变量、字符串操作以及输出语句的理解。

### 思路分析：

1. **定义变量**：你可以用变量存储你的名字、年龄、爱好等信息。
2. **使用字符串拼接或格式化**：将这些变量组合成一个完整的句子。
3. **输出结果**：使用 `print()` 函数把自我介绍打印出来。

### 示例代码思路（以 Python 为例）：

```python
# 定义变量
name = "张三"
age = 20
hobby = "打篮球"

# 使用字符串格式化输出
print(f"大家好，我叫{name}，今年{age}岁，我喜欢{hobby}。")
```

### 你可以尝试：
- 改变变量的值，看看输出是否变化？
- 尝试使用其他方式（如 `+` 拼接字符串）来实现同样的功能？

如果你有特定的编程语言要求，也可以告诉我，我可以提供对应语言的示例。', NULL, '2026-03-13 04:17:31');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (11, 1, '请详细解释以下代码的功能和逻辑：

```
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
```', '这段代码是一个简单的 Java 程序，它的功能是打印出 "Hello World" 字符串。我们来逐步分析它的结构和逻辑：

---

### 1. **类的定义：`public class Main`**
- Java 是一种面向对象的语言，所有的代码都必须包含在**类**中。
- `public` 是一个访问修饰符，表示这个类可以被其他类访问。
- `Main` 是类的名字，通常用于主程序入口。

---

### 2. **主方法：`public static void main(String[] args)`**
- 这是 Java 程序的**入口点**，程序从这里开始执行。
- `public`：表示该方法可以被外部访问（Java 虚拟机调用）。
- `static`：表示这是一个静态方法，可以在不创建类实例的情况下直接调用。
- `void`：表示该方法没有返回值。
- `main`：是方法名，Java 虚拟机知道从这个方法开始运行程序。
- `String[] args`：是命令行参数，用于接收用户在运行程序时输入的参数（例如：`java Main arg1 arg2`）。

---

### 3. **语句：`System.out.println("Hello World");`**
- `System.out` 是标准输出流（通常是控制台）。
- `println` 是 `PrintStream` 类的一个方法，用于打印文本并换行。
- `"Hello World"` 是一个字符串，表示要打印的内容。

---

### 4. **整体逻辑**
- 当你运行这个程序时，Java 虚拟机会找到 `Main` 类中的 `main` 方法。
- 然后执行 `System.out.println("Hello World");` 这一行代码。
- 最终在控制台上输出：  
  ```
  Hello World
  ```

---

### 小提示（扩展理解）
- 如果你想要修改输出内容，只需要把 `"Hello World"` 改成你想打印的任何字符串即可。
- 你可以尝试添加更多 `System.out.println(...);` 语句来输出更多内容。

---

如果你有类似的问题或想了解更复杂的 Java 代码逻辑，可以继续提问！', NULL, '2026-03-13 04:42:01');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (12, 1, '请详细解释以下代码的功能和逻辑：

```
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
```', '这段代码是一个简单的 Java 程序，它的功能是打印出 "Hello World" 字符串。我们来逐步分析它的结构和逻辑：

---

### 1. **类的定义：`public class Main`**
- Java 是一种面向对象的语言，所有的代码都必须包含在**类**中。
- `public` 是一个访问修饰符，表示这个类可以被其他类访问。
- `Main` 是类的名字，通常用于主程序入口。

---

### 2. **主方法：`public static void main(String[] args)`**
- 这是 Java 程序的**入口点**，程序从这里开始执行。
- `public`：表示该方法可以被外部访问（Java 虚拟机调用）。
- `static`：表示这是一个静态方法，可以在不创建类实例的情况下直接调用。
- `void`：表示该方法没有返回值。
- `main`：是方法名，Java 虚拟机知道从这个方法开始运行程序。
- `String[] args`：是命令行参数，用于接收用户在运行程序时输入的参数（例如：`java Main arg1 arg2`）。

---

### 3. **语句：`System.out.println("Hello World");`**
- `System.out` 是标准输出流（通常是控制台）。
- `println` 是 `PrintStream` 类的一个方法，用于打印文本并换行。
- `"Hello World"` 是一个字符串，表示要打印的内容。

---

### 4. **整体逻辑**
- 当你运行这个程序时，Java 虚拟机会找到 `Main` 类中的 `main` 方法。
- 然后执行 `System.out.println("Hello World");` 这一行代码。
- 最终在控制台上输出：  
  ```
  Hello World
  ```

---

### 小提示（扩展理解）
- 如果你想要修改输出内容，只需要把 `"Hello World"` 改成你想打印的任何字符串即可。
- 你可以尝试添加更多 `System.out.println(...);` 语句来输出更多内容。

---

如果你有类似的问题或想了解更复杂的 Java 代码逻辑，可以继续提问！', NULL, '2026-03-13 04:46:30');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (13, 1, '请详细解释以下代码的功能和逻辑：

```
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
```', '这段代码是一个简单的 Java 程序，它的功能是输出字符串 "Hello World" 到控制台。我们可以逐步分析它的结构和逻辑：

---

### 1. **类的定义：`public class Main`**
- Java 是一种面向对象的语言，所有的代码都必须包含在**类**中。
- `public` 是一个访问修饰符，表示这个类可以被其他类访问。
- `Main` 是类的名字，通常用于作为程序的入口点。

---

### 2. **主方法：`public static void main(String[] args)`**
- 这是 Java 程序的**入口点**，即程序运行时首先执行的方法。
- `public` 表示该方法可以被外部调用。
- `static` 表示该方法属于类本身，而不是类的实例（不需要创建对象就可以调用）。
- `void` 表示该方法没有返回值。
- `main` 是方法名，Java 要求程序从 `main` 方法开始执行。
- `String[] args` 是传递给程序的命令行参数，但在这个例子中没有使用到。

---

### 3. **输出语句：`System.out.println("Hello World");`**
- `System.out` 是标准输出流，通常指向控制台。
- `println` 是 `PrintStream` 类的一个方法，用于打印文本并换行。
- `"Hello World"` 是要输出的字符串。

---

### 4. **整体逻辑流程**
1. 程序启动时，JVM（Java 虚拟机）会查找 `Main` 类中的 `main` 方法。
2. 找到后，JVM 会调用 `main` 方法。
3. 在 `main` 方法中，执行 `System.out.println("Hello World");` 语句。
4. 输出 "Hello World" 到控制台，并自动换行。

---

### 5. **思考练习**
- 如果你将 `println` 改为 `print`，会发生什么？
- 如果你修改类名为 `MyProgram`，还能运行吗？为什么？

---

你可以尝试修改代码，观察输出结果的变化，从而加深对 Java 程序结构的理解。', NULL, '2026-03-13 04:52:55');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (14, 1, '请详细解释以下代码的功能和逻辑：

```
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
```', '这段代码是一个简单的 Java 程序，它的功能是打印出 "Hello World" 字符串。我们来逐步分析它的结构和逻辑：

---

### 1. **类的定义：`public class Main`**
- Java 是一种面向对象的语言，所有的代码都必须包含在**类**中。
- `public` 是一个访问修饰符，表示这个类可以被其他类访问。
- `Main` 是类的名字，通常用于主程序入口。

---

### 2. **主方法：`public static void main(String[] args)`**
- 这是 Java 程序的**入口点**，程序从这里开始执行。
- `public`：表示该方法可以被外部访问（Java 虚拟机调用）。
- `static`：表示这是一个静态方法，可以在不创建类实例的情况下直接调用。
- `void`：表示该方法没有返回值。
- `main`：是方法名，Java 虚拟机知道从这个方法开始运行程序。
- `String[] args`：是命令行参数，用于接收用户在运行程序时输入的参数（例如：`java Main arg1 arg2`）。

---

### 3. **语句：`System.out.println("Hello World");`**
- `System.out` 是标准输出流（通常是控制台）。
- `println` 是 `PrintStream` 类的一个方法，用于打印文本并换行。
- `"Hello World"` 是一个字符串，表示要打印的内容。

---

### 4. **整体逻辑**
- 当你运行这个程序时，Java 虚拟机会找到 `Main` 类中的 `main` 方法。
- 然后执行 `System.out.println("Hello World");` 这一行代码。
- 最终在控制台上输出：  
  ```
  Hello World
  ```

---

### 小提示（扩展理解）
- 如果你想要修改输出内容，只需要把 `"Hello World"` 改成你想打印的任何字符串即可。
- 你可以尝试添加更多 `System.out.println(...);` 语句来输出更多内容。

---

如果你有类似的问题或想了解更复杂的 Java 代码逻辑，可以继续提问！', NULL, '2026-03-13 04:58:12');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (15, 1, '请详细解释以下代码的功能和逻辑：

```
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
```', '这段代码是一个简单的 Java 程序，它的功能是打印出 "Hello World" 字符串。我们来逐步分析它的结构和逻辑：

---

### 1. **类的定义：`public class Main`**
- Java 是一种面向对象的语言，所有的代码都必须包含在**类**中。
- `public` 是一个访问修饰符，表示这个类可以被其他类访问。
- `Main` 是类的名字，通常用于主程序入口。

---

### 2. **主方法：`public static void main(String[] args)`**
- 这是 Java 程序的**入口点**，程序从这里开始执行。
- `public`：表示该方法可以被外部访问（Java 虚拟机调用）。
- `static`：表示这是一个静态方法，可以在不创建类实例的情况下直接调用。
- `void`：表示该方法没有返回值。
- `main`：是方法名，Java 虚拟机知道从这个方法开始运行程序。
- `String[] args`：是命令行参数，用于接收用户在运行程序时输入的参数（例如：`java Main arg1 arg2`）。

---

### 3. **语句：`System.out.println("Hello World");`**
- `System.out` 是标准输出流（通常是控制台）。
- `println` 是 `PrintStream` 类的一个方法，用于打印文本并换行。
- `"Hello World"` 是要输出的字符串。

---

### 总结逻辑：
1. 定义了一个名为 `Main` 的公共类。
2. 在类中定义了 `main` 方法作为程序的入口。
3. 在 `main` 方法中，通过 `System.out.println` 输出字符串 "Hello World"。

---

### 示例理解思路：
如果你将这段代码保存为 `Main.java`，然后使用 Java 编译器编译它，并运行生成的字节码文件，你将会看到控制台上输出：

```
Hello World
```

这是一段最基础的 Java 程序，常用于学习 Java 语法和运行环境的配置。', NULL, '2026-03-13 04:58:45');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (16, 1, '请为以下代码提供改进建议和最佳实践：

```
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
```', '这段代码是一个非常基础的 Java 程序，用于输出 “Hello World”。虽然它能正常运行，但可以从**可维护性、可读性、规范性**等方面进行改进。以下是几个改进建议和最佳实践方向：

---

### ✅ 1. **使用更清晰的类名和方法名**
- 当前类名为 `Main`，虽然在某些情况下是合理的（如入口类），但在更复杂的项目中，建议使用更具描述性的名称。
- 如果这个类仅用于启动程序，可以考虑将其命名为 `Application` 或 `Launcher`。

---

### ✅ 2. **添加注释或文档说明**
- 在代码中添加注释，尤其是对类或方法的功能进行说明，有助于他人理解代码逻辑。

```java
/**
 * 该类用于演示一个简单的 Java 程序，输出 "Hello World"。
 */
public class Application {
    public static void main(String[] args) {
        // 输出 "Hello World" 到控制台
        System.out.println("Hello World");
    }
}
```

---

### ✅ 3. **遵循 Java 编码规范**
- 类名应使用 **大驼峰命名法（PascalCase）**，如 `Application` 而不是 `Main`。
- 方法名应使用 **小驼峰命名法（camelCase）**，如 `main` 是符合规范的。

---

### ✅ 4. **考虑模块化设计（未来扩展）**
- 如果将来需要添加更多功能，可以将输出逻辑封装成方法，提高复用性和可测试性。

```java
public class Application {
    public static void main(String[] args) {
        greetUser();
    }

    /**
     * 打印欢迎信息到控制台
     */
    private static void greetUser() {
        System.out.println("Hello World");
    }
}
```

---

### ✅ 5. **使用现代 Java 特性（可选）**
- 如果你使用的是较新的 Java 版本（如 Java 8+），可以考虑使用更现代的写法，例如通过 `System.out::println` 作为函数式接口使用，但这对于简单示例来说可能并不必要。

---

### ✅ 6. **考虑异常处理（如果适用）**
- 如果程序将来会涉及文件读写、网络请求等操作，建议添加 `try-catch` 块以增强健壮性。

---

### 总结：改进后的代码结构建议

```java
/**
 * 应用程序入口类，用于展示基本的 Java 输出功能。
 */
public class Application {
    public static void main(String[] args) {
        greetUser();
    }

    /**
     * 输出欢迎信息到控制台。
     */
    private static void greetUser() {
        System.out.println("Hello World");
    }
}
```

---

如果你有更具体的场景或需求（比如这个类是某个更大项目的组成部分），我可以进一步提供更有针对性的建议。', NULL, '2026-03-13 05:09:59');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (17, 1, '你好', '你好！我是你的编程助教，可以帮你解决编程相关的问题。请告诉我你遇到的具体问题，比如你正在学习的编程语言、遇到的错误信息、或者你想要实现的功能等。我会为你提供清晰的思路和示例代码，帮助你理解原理。', NULL, '2026-03-20 00:09:57');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (18, 1, '请详细解释以下代码的功能和逻辑：

```
main(String[] args) {
        System.out.println("Hello World");
    }
```', '这段代码是 Java 语言中一个简单的程序，它的功能是输出 "Hello World" 到控制台。下面我将从**功能**和**逻辑**两个方面来详细解释。

---

### 一、功能解释

这段代码的功能非常明确：  
在程序运行时，它会打印出字符串 `"Hello World"` 到控制台（通常是终端或命令行窗口）。

这是 Java 中最经典的入门示例，用来验证开发环境是否正常，以及学习如何编写和运行 Java 程序。

---

### 二、逻辑解释

我们逐行分析代码的结构和逻辑：

```java
main(String[] args) {
    System.out.println("Hello World");
}
```

#### 1. `main(String[] args)`  
- 这是一个**主方法（main method）**，是 Java 程序的入口点。
- Java 虚拟机（JVM）在运行程序时，会从 `main` 方法开始执行。
- `String[] args` 是传递给程序的**命令行参数**，可以用于接收用户输入或其他信息。

> 注意：Java 的 `main` 方法必须写成 `public static void main(String[] args)`，而你给出的代码缺少了 `public static` 修饰符，因此不能直接运行。这是不完整的代码。

#### 2. `System.out.println("Hello World");`  
- `System.out` 是 Java 中的标准输出流对象，通常指向控制台。
- `println()` 是 `PrintStream` 类的一个方法，用于打印字符串并换行。
- `"Hello World"` 是要输出的内容，是一个字符串字面量。

---

### 三、完整正确的代码示例

为了能运行这段代码，你需要补全 `main` 方法的定义：

```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
```

#### 说明：
- `public class HelloWorld`：定义一个类，类名与文件名相同（如 `HelloWorld.java`）。
- `public static void main(String[] args)`：Java 程序的入口方法。
- `System.out.println(...)`：输出语句。

---

### 四、总结

| 部分         | 说明                                     |
|--------------|------------------------------------------|
| `main`       | 程序入口点                               |
| `String[] args` | 命令行参数列表                           |
| `System.out.println(...)` | 打印字符串到控制台                       |

如果你有进一步的问题，比如如何编译和运行这个程序，或者想了解 `args` 的使用方式，欢迎继续提问！', NULL, '2026-03-20 00:10:13');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (19, 1, '这段代码有什么bug？', '好的，请将你认为有 bug 的代码贴出来，我会帮你分析问题所在，并引导你理解其中的原理和解决思路。请提供具体的代码片段。', NULL, '2026-03-20 02:36:59');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (20, 1, '这段代码有什么bug？', '好的，请将你认为有 bug 的代码粘贴过来，我会帮助你分析问题所在，并给出调试思路和示例代码供你参考。请确保代码是完整的，并附上你遇到的问题现象（如错误信息、程序行为与预期不符的地方等）。', NULL, '2026-03-20 02:37:06');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (21, 1, '请解释这个概念', '好的，请告诉我你需要解释的编程概念是什么？例如：变量、函数、循环、类、递归、算法、数据结构、闭包、装饰器、异步编程、内存管理等。我会详细为你解释其原理，并提供示例代码帮助理解。', NULL, '2026-03-20 02:37:33');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (22, 1, '请为以下代码提供改进建议和最佳实践：

```
id main(String[] args) {
        System.out.println("Hello Worl
```', '你提供的代码存在一些语法错误和不规范的地方。我们可以一步步来分析并改进它。

---

### 一、问题分析

你的代码如下：

```java
id main(String[] args) {
        System.out.println("Hello Worl
```

#### 1. **方法定义错误**
- `id` 应该是 `public static void`，Java 中主方法的正确格式是：
  
  ```java
  public static void main(String[] args) { ... }
  ```

#### 2. **字符串拼写错误**
- `"Hello Worl"` 缺少一个 `d`，应该是 `"Hello World"`。

#### 3. **缺少右括号 `}`**
- 方法体没有闭合，导致编译错误。

---

### 二、改进建议和最佳实践

#### ✅ 建议 1：使用标准的 Java 主方法格式
Java 程序的入口点必须是 `public static void main(String[] args)`。

#### ✅ 建议 2：保持良好的代码风格
- 每行代码尽量清晰。
- 使用正确的缩进（建议使用 4 个空格）。
- 保证字符串内容完整。

#### ✅ 建议 3：添加注释（可选）
在复杂逻辑中添加注释有助于理解。

---

### 三、示例代码（改进版）

```java
public class Main {
    public static void main(String[] args) {
        // 打印 "Hello World" 到控制台
        System.out.println("Hello World");
    }
}
```

---

### 四、思考练习

你可以尝试以下问题来加深理解：

1. 如果将 `main` 方法改为 `private`，会发生什么？为什么？
2. 如果 `String[] args` 改为 `String args[]`，是否可以？为什么？
3. 为什么 `System.out.println` 是 Java 中打印输出的标准方式？

---

如果你有其他代码需要分析或优化，欢迎继续提问！', NULL, '2026-03-20 02:37:46');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (23, 1, '你好', '你好！我是你的编程助教，可以帮你解决编程相关的问题。请告诉我你遇到的具体问题，比如你正在学习的编程语言、遇到的错误信息、或者你想实现的功能等。我会给你清晰的思路和示例代码，帮助你理解原理。', NULL, '2026-03-20 02:50:26');
INSERT INTO `question` (`id`, `user_id`, `content`, `answer`, `code`, `create_time`) VALUES (24, 1, '这段代码有什么bug？', '好的，请将你认为有 bug 的代码贴出来，我会帮你分析问题所在，并引导你理解其中的原理和解决思路。请提供具体的代码内容。', NULL, '2026-03-20 02:50:32');

-- Table structure for review_plan
DROP TABLE IF EXISTS `review_plan`;
CREATE TABLE `review_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `wrong_item_id` bigint NOT NULL,
  `next_review_time` datetime NOT NULL,
  `review_count` int DEFAULT '0',
  `status` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `wrong_item_id` (`wrong_item_id`),
  CONSTRAINT `review_plan_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `review_plan_ibfk_2` FOREIGN KEY (`wrong_item_id`) REFERENCES `wrong_book_item` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for review_plan

-- Table structure for role
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for role
INSERT INTO `role` (`id`, `name`, `description`, `create_time`, `update_time`) VALUES (1, 'admin', '系统管理员', '2026-03-20 00:21:13', '2026-03-20 00:21:13');
INSERT INTO `role` (`id`, `name`, `description`, `create_time`, `update_time`) VALUES (2, 'teacher', '教师', '2026-03-20 00:21:13', '2026-03-20 00:21:13');
INSERT INTO `role` (`id`, `name`, `description`, `create_time`, `update_time`) VALUES (3, 'assistant', '助教', '2026-03-20 00:21:13', '2026-03-20 00:21:13');
INSERT INTO `role` (`id`, `name`, `description`, `create_time`, `update_time`) VALUES (4, 'content_operation', '内容运营', '2026-03-20 00:21:13', '2026-03-20 00:21:13');
INSERT INTO `role` (`id`, `name`, `description`, `create_time`, `update_time`) VALUES (5, 'operation_admin', '运维管理员', '2026-03-20 00:21:13', '2026-03-20 00:21:13');

-- Table structure for role_permission
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `permission_id` (`permission_id`),
  CONSTRAINT `role_permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
  CONSTRAINT `role_permission_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for role_permission
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (1, 1, 7, '2026-03-20 00:21:13');
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (2, 1, 5, '2026-03-20 00:21:13');
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (3, 1, 4, '2026-03-20 00:21:13');
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (4, 1, 3, '2026-03-20 00:21:13');
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (5, 1, 2, '2026-03-20 00:21:13');
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (6, 1, 6, '2026-03-20 00:21:13');
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (7, 1, 1, '2026-03-20 00:21:13');

-- Table structure for sandbox_status
DROP TABLE IF EXISTS `sandbox_status`;
CREATE TABLE `sandbox_status` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `container_id` varchar(255) DEFAULT NULL,
  `status` varchar(50) NOT NULL,
  `cpu_usage` double DEFAULT NULL,
  `memory_usage` double DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for sandbox_status

-- Table structure for submit
DROP TABLE IF EXISTS `submit`;
CREATE TABLE `submit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提交ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `problem_id` bigint NOT NULL COMMENT '题目ID',
  `code` text NOT NULL COMMENT '提交代码',
  `language` varchar(20) DEFAULT 'java' COMMENT '编程语言',
  `result` tinyint NOT NULL COMMENT '结果：0-AC(通过)，1-WA(答案错误)，2-RE(运行错误)，3-TLE(超时)，4-MLE(内存超限)',
  `time_cost` int DEFAULT '0' COMMENT '耗时（ms）',
  `memory_cost` int DEFAULT '0' COMMENT '内存消耗（字节）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_problem_id` (`problem_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='提交记录表';

-- Data for submit
INSERT INTO `submit` (`id`, `user_id`, `problem_id`, `code`, `language`, `result`, `time_cost`, `memory_cost`, `create_time`) VALUES (1, 1, 1, 'public class Main{...}', 'java', 1, 7674, 0, '2026-03-12 16:22:06');
INSERT INTO `submit` (`id`, `user_id`, `problem_id`, `code`, `language`, `result`, `time_cost`, `memory_cost`, `create_time`) VALUES (2, 1, 1, 'public class Main{123}', 'java', 1, 7221, 0, '2026-03-12 16:25:05');
INSERT INTO `submit` (`id`, `user_id`, `problem_id`, `code`, `language`, `result`, `time_cost`, `memory_cost`, `create_time`) VALUES (3, 1, 1, 'public class Main{123}', 'java', 3, 7155, 0, '2026-03-12 17:16:59');
INSERT INTO `submit` (`id`, `user_id`, `problem_id`, `code`, `language`, `result`, `time_cost`, `memory_cost`, `create_time`) VALUES (4, 2, 9, 'def bubble_sort(arr):
    """冒泡排序算法：从小到大排序"""
    n = len(arr)
    # 外层循环：控制排序轮数
    for i in range(n):
        # 标记本轮是否发生交换（优化：若未交换则已排序完成）
        swapped = False
        # 内层循环：比较相邻元素，每轮将最大元素“冒泡”到末尾
        for j in range(0, n - i - 1):
            if arr[j] > arr[j + 1]:
                # 交换元素
                arr[j], arr[j + 1] = arr[j + 1], arr[j]
                swapped = True
        # 若本轮未交换，说明数组已有序，提前退出
        if not swapped:
            break
    return arr

# 输入处理：读取n个整数
try:
    # 读取一行输入，按空格分割并转换为整数列表
    input_str = input("请输入n个整数（用空格分隔）：")
    num_list = list(map(int, input_str.split()))
    
    if not num_list:
        print("输入不能为空！")
    else:
        # 执行冒泡排序
        sorted_list = bubble_sort(num_list)
        # 输出结果：用空格分隔
        print("排序后结果：", " ".join(map(str, sorted_list)))
except ValueError:
    print("输入格式错误，请输入整数！")', 'python', 3, 6725, 0, '2026-03-12 21:16:57');
INSERT INTO `submit` (`id`, `user_id`, `problem_id`, `code`, `language`, `result`, `time_cost`, `memory_cost`, `create_time`) VALUES (5, 2, 9, 'def bubble_sort(arr):
    """冒泡排序算法：从小到大排序"""
    n = len(arr)
    # 外层循环：控制排序轮数
    for i in range(n):
        # 标记本轮是否发生交换（优化：若未交换则已排序完成）
        swapped = False
        # 内层循环：比较相邻元素，每轮将最大元素“冒泡”到末尾
        for j in range(0, n - i - 1):
            if arr[j] > arr[j + 1]:
                # 交换元素
                arr[j], arr[j + 1] = arr[j + 1], arr[j]
                swapped = True
        # 若本轮未交换，说明数组已有序，提前退出
        if not swapped:
            break
    return arr

# 输入处理：读取n个整数
try:
    # 读取一行输入，按空格分割并转换为整数列表
    input_str = input("请输入n个整数（用空格分隔）：")
    num_list = list(map(int, input_str.split()))
    
    if not num_list:
        print("输入不能为空！")
    else:
        # 执行冒泡排序
        sorted_list = bubble_sort(num_list)
        # 输出结果：用空格分隔
        print("排序后结果：", " ".join(map(str, sorted_list)))
except ValueError:
    print("输入格式错误，请输入整数！")', 'python', 3, 6150, 0, '2026-03-12 21:17:20');
INSERT INTO `submit` (`id`, `user_id`, `problem_id`, `code`, `language`, `result`, `time_cost`, `memory_cost`, `create_time`) VALUES (6, 2, 9, 'def bubble_sort(arr):
    n = len(arr)
    for i in range(n):
        swapped = False
        for j in range(n - i - 1):
            if arr[j] > arr[j + 1]:
                arr[j], arr[j + 1] = arr[j + 1], arr[j]
                swapped = True
        if not swapped:
            break
    return arr', 'python', 1, 6054, 0, '2026-03-12 21:18:44');
INSERT INTO `submit` (`id`, `user_id`, `problem_id`, `code`, `language`, `result`, `time_cost`, `memory_cost`, `create_time`) VALUES (7, 2, 9, 'def bubble_sort(arr):
    n = len(arr)
    for i in range(n):
        swapped = False
        for j in range(n - i - 1):
            if arr[j] > arr[j + 1]:
                arr[j], arr[j + 1] = arr[j + 1], arr[j]
                swapped = True
        if not swapped:
            break
    return arr', 'python', 1, 5996, 0, '2026-03-12 21:18:59');
INSERT INTO `submit` (`id`, `user_id`, `problem_id`, `code`, `language`, `result`, `time_cost`, `memory_cost`, `create_time`) VALUES (8, 1, 8, 'public class Main {
    public static void main(String[] args) {
        // 在此编写你的代码
        System.out.println("Hello World");
    }
}', 'java', 1, 5963, 0, '2026-03-12 22:32:59');
INSERT INTO `submit` (`id`, `user_id`, `problem_id`, `code`, `language`, `result`, `time_cost`, `memory_cost`, `create_time`) VALUES (9, 1, 8, '# 在此编写你的代码
def main():
    print("Hello World")

if __name__ == "__main__":
    main()', 'python', 1, 5893, 0, '2026-03-12 22:33:22');
INSERT INTO `submit` (`id`, `user_id`, `problem_id`, `code`, `language`, `result`, `time_cost`, `memory_cost`, `create_time`) VALUES (10, 1, 9, 'public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}', 'java', 1, 84, 0, '2026-03-13 05:09:17');
INSERT INTO `submit` (`id`, `user_id`, `problem_id`, `code`, `language`, `result`, `time_cost`, `memory_cost`, `create_time`) VALUES (11, 1, 9, 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // 读取数组长度
        int n = scanner.nextInt();
        scanner.nextLine(); // 消耗换行符
        
        // 读取数组元素
        String[] input = scanner.nextLine().split(" ");
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = Integer.parseInt(input[i]);
        }
        
        // 冒泡排序
        bubbleSort(arr);
        
        // 输出排序结果
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
            if (i < arr.length - 1) {
                System.out.print(" ");
            }
        }
        System.out.println();
        
        scanner.close();
    }
    
    /**
     * 冒泡排序算法
     */
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    // 交换元素
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
}', 'java', 1, 5493, 0, '2026-03-13 18:44:38');
INSERT INTO `submit` (`id`, `user_id`, `problem_id`, `code`, `language`, `result`, `time_cost`, `memory_cost`, `create_time`) VALUES (12, 1, 9, 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // 读取数组长度
        int n = scanner.nextInt();
        scanner.nextLine(); // 消耗换行符
        
        // 读取数组元素
        String[] input = scanner.nextLine().split(" ");
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = Integer.parseInt(input[i]);
        }
        
        // 冒泡排序
        bubbleSort(arr);
        
        // 输出排序结果
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
            if (i < arr.length - 1) {
                System.out.print(" ");
            }
        }
        System.out.println();
        
        scanner.close();
    }
    
    /**
     * 冒泡排序算法
     */
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    // 交换元素
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
}', 'java', 3, 3072, 0, '2026-03-13 18:45:07');
INSERT INTO `submit` (`id`, `user_id`, `problem_id`, `code`, `language`, `result`, `time_cost`, `memory_cost`, `create_time`) VALUES (13, 1, 9, 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // 读取数组长度
        int n = scanner.nextInt();
        scanner.nextLine(); // 消耗换行符
        
        // 读取数组元素
        String[] input = scanner.nextLine().split(" ");
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = Integer.parseInt(input[i]);
        }
        
        // 冒泡排序
        bubbleSort(arr);
        
        // 输出排序结果
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
            if (i < arr.length - 1) {
                System.out.print(" ");
            }
        }
        System.out.println();
        
        scanner.close();
    }
    
    /**
     * 冒泡排序算法
     */
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    // 交换元素
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
}', 'java', 1, 2937, 0, '2026-03-13 18:45:43');
INSERT INTO `submit` (`id`, `user_id`, `problem_id`, `code`, `language`, `result`, `time_cost`, `memory_cost`, `create_time`) VALUES (14, 1, 9, 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // 读取数组长度
        int n = scanner.nextInt();
        scanner.nextLine(); // 消耗换行符
        
        // 读取数组元素
        String[] input = scanner.nextLine().split(" ");
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = Integer.parseInt(input[i]);
        }
        
        // 冒泡排序
        bubbleSort(arr);
        
        // 输出排序结果
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
            if (i < arr.length - 1) {
                System.out.print(" ");
            }
        }
        System.out.println();
        
        scanner.close();
    }
    
    /**
     * 冒泡排序算法
     */
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    // 交换元素
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
}', 'java', 3, 4562, 0, '2026-03-13 19:40:18');

-- Table structure for submit_test_case_result
DROP TABLE IF EXISTS `submit_test_case_result`;
CREATE TABLE `submit_test_case_result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `submit_id` bigint NOT NULL COMMENT '鎻愪氦璁板綍ID',
  `test_case_id` bigint NOT NULL COMMENT '娴嬭瘯鐢ㄤ緥ID',
  `result` int DEFAULT NULL COMMENT '鎵ц?缁撴灉锛?-鎴愬姛锛?-澶辫触锛?-瓒呮椂锛?-鍐呭瓨瓒呴檺锛?-缂栬瘧閿欒?锛',
  `time_cost` int DEFAULT NULL COMMENT '鏃堕棿娑堣?锛堟?绉掞級',
  `memory_cost` int DEFAULT NULL COMMENT '鍐呭瓨娑堣?锛圞B锛',
  `actual_output` text COMMENT '瀹為檯杈撳嚭',
  `error_message` text COMMENT '閿欒?淇℃伅',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_submit_id` (`submit_id`),
  KEY `idx_test_case_id` (`test_case_id`),
  KEY `idx_result` (`result`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鎻愪氦娴嬭瘯鐢ㄤ緥缁撴灉琛';

-- Data for submit_test_case_result
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (1, 10, 26, 1, 84, 0, '运行异常：Status 400: {"message":"Minimum memory limit allowed is 6MB"}
', NULL, '2026-03-13 05:09:17');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (2, 10, 27, 1, 84, 0, '运行异常：Status 400: {"message":"Minimum memory limit allowed is 6MB"}
', NULL, '2026-03-13 05:09:17');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (3, 10, 28, 1, 84, 0, '运行异常：Status 400: {"message":"Minimum memory limit allowed is 6MB"}
', NULL, '2026-03-13 05:09:17');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (4, 11, 152, 1, 5492, 0, '', NULL, '2026-03-13 18:44:39');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (5, 11, 153, 1, 5493, 0, '', NULL, '2026-03-13 18:44:39');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (6, 11, 154, 1, 5492, 0, '', NULL, '2026-03-13 18:44:39');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (7, 11, 155, 1, 5486, 0, '', NULL, '2026-03-13 18:44:39');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (8, 12, 152, 3, 3071, 0, '运行错误：Exception in thread "main" java.util.NoSuchElementException
	at java.base/java.util.Scanner.throwFor(Scanner.java:937)
	at java.base/java.util.Scanner.next(Scanner.java:1594)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2258)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2212)
	at Main.main(Main.java:8)', 'Exception in thread "main" java.util.NoSuchElementException
	at java.base/java.util.Scanner.throwFor(Scanner.java:937)
	at java.base/java.util.Scanner.next(Scanner.java:1594)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2258)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2212)
	at Main.main(Main.java:8)', '2026-03-13 18:45:07');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (9, 12, 153, 3, 3070, 0, '运行错误：Exception in thread "main" java.util.NoSuchElementException
	at java.base/java.util.Scanner.throwFor(Scanner.java:937)
	at java.base/java.util.Scanner.next(Scanner.java:1594)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2258)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2212)
	at Main.main(Main.java:8)', 'Exception in thread "main" java.util.NoSuchElementException
	at java.base/java.util.Scanner.throwFor(Scanner.java:937)
	at java.base/java.util.Scanner.next(Scanner.java:1594)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2258)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2212)
	at Main.main(Main.java:8)', '2026-03-13 18:45:07');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (10, 12, 154, 3, 3072, 0, '运行错误：Exception in thread "main" java.util.NoSuchElementException
	at java.base/java.util.Scanner.throwFor(Scanner.java:937)
	at java.base/java.util.Scanner.next(Scanner.java:1594)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2258)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2212)
	at Main.main(Main.java:8)', 'Exception in thread "main" java.util.NoSuchElementException
	at java.base/java.util.Scanner.throwFor(Scanner.java:937)
	at java.base/java.util.Scanner.next(Scanner.java:1594)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2258)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2212)
	at Main.main(Main.java:8)', '2026-03-13 18:45:07');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (11, 12, 155, 3, 3069, 0, '运行错误：Exception in thread "main" java.util.NoSuchElementException
	at java.base/java.util.Scanner.throwFor(Scanner.java:937)
	at java.base/java.util.Scanner.next(Scanner.java:1594)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2258)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2212)
	at Main.main(Main.java:8)', 'Exception in thread "main" java.util.NoSuchElementException
	at java.base/java.util.Scanner.throwFor(Scanner.java:937)
	at java.base/java.util.Scanner.next(Scanner.java:1594)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2258)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2212)
	at Main.main(Main.java:8)', '2026-03-13 18:45:07');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (12, 13, 152, 1, 2937, 0, '', NULL, '2026-03-13 18:45:43');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (13, 13, 153, 1, 2927, 0, '', NULL, '2026-03-13 18:45:43');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (14, 13, 154, 1, 2936, 0, '', NULL, '2026-03-13 18:45:43');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (15, 13, 155, 1, 2930, 0, '', NULL, '2026-03-13 18:45:43');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (16, 14, 152, 3, 4562, 0, '运行错误：Exception in thread "main" java.util.NoSuchElementException
	at java.base/java.util.Scanner.throwFor(Scanner.java:937)
	at java.base/java.util.Scanner.next(Scanner.java:1594)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2258)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2212)
	at Main.main(Main.java:8)', 'Exception in thread "main" java.util.NoSuchElementException
	at java.base/java.util.Scanner.throwFor(Scanner.java:937)
	at java.base/java.util.Scanner.next(Scanner.java:1594)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2258)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2212)
	at Main.main(Main.java:8)', '2026-03-13 19:40:19');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (17, 14, 153, 3, 4562, 0, '运行错误：Exception in thread "main" java.util.NoSuchElementException
	at java.base/java.util.Scanner.throwFor(Scanner.java:937)
	at java.base/java.util.Scanner.next(Scanner.java:1594)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2258)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2212)
	at Main.main(Main.java:8)', 'Exception in thread "main" java.util.NoSuchElementException
	at java.base/java.util.Scanner.throwFor(Scanner.java:937)
	at java.base/java.util.Scanner.next(Scanner.java:1594)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2258)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2212)
	at Main.main(Main.java:8)', '2026-03-13 19:40:19');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (18, 14, 154, 3, 4560, 0, '运行错误：Exception in thread "main" java.util.NoSuchElementException
	at java.base/java.util.Scanner.throwFor(Scanner.java:937)
	at java.base/java.util.Scanner.next(Scanner.java:1594)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2258)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2212)
	at Main.main(Main.java:8)', 'Exception in thread "main" java.util.NoSuchElementException
	at java.base/java.util.Scanner.throwFor(Scanner.java:937)
	at java.base/java.util.Scanner.next(Scanner.java:1594)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2258)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2212)
	at Main.main(Main.java:8)', '2026-03-13 19:40:19');
INSERT INTO `submit_test_case_result` (`id`, `submit_id`, `test_case_id`, `result`, `time_cost`, `memory_cost`, `actual_output`, `error_message`, `create_time`) VALUES (19, 14, 155, 3, 4559, 0, '运行错误：Exception in thread "main" java.util.NoSuchElementException
	at java.base/java.util.Scanner.throwFor(Scanner.java:937)
	at java.base/java.util.Scanner.next(Scanner.java:1594)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2258)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2212)
	at Main.main(Main.java:8)', 'Exception in thread "main" java.util.NoSuchElementException
	at java.base/java.util.Scanner.throwFor(Scanner.java:937)
	at java.base/java.util.Scanner.next(Scanner.java:1594)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2258)
	at java.base/java.util.Scanner.nextInt(Scanner.java:2212)
	at Main.main(Main.java:8)', '2026-03-13 19:40:19');

-- Table structure for system_log
DROP TABLE IF EXISTS `system_log`;
CREATE TABLE `system_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `level` varchar(20) NOT NULL,
  `category` varchar(100) NOT NULL,
  `message` text NOT NULL,
  `details` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for system_log

-- Table structure for system_monitor
DROP TABLE IF EXISTS `system_monitor`;
CREATE TABLE `system_monitor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cpu_usage` double DEFAULT NULL,
  `memory_usage` double DEFAULT NULL,
  `disk_usage` double DEFAULT NULL,
  `network_in` double DEFAULT NULL,
  `network_out` double DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for system_monitor

-- Table structure for test_case
DROP TABLE IF EXISTS `test_case`;
CREATE TABLE `test_case` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `problem_id` bigint NOT NULL COMMENT '棰樼洰ID',
  `input` text COMMENT '杈撳叆鏁版嵁',
  `output` text COMMENT '鏈熸湜杈撳嚭',
  `is_sample` tinyint(1) DEFAULT '1' COMMENT '鏄?惁涓虹ず渚嬫祴璇曠敤渚嬶紙1-鏄?紝0-鍚︼級',
  `sort_order` int DEFAULT '0' COMMENT '鎺掑簭椤哄簭',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_problem_id` (`problem_id`),
  KEY `idx_is_sample` (`is_sample`)
) ENGINE=InnoDB AUTO_INCREMENT=278 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='娴嬭瘯鐢ㄤ緥琛';

-- Data for test_case
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (29, 1, '1 2', '3', 1, 1, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (30, 1, '100 200', '300', 0, 2, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (31, 1, '-5 10', '5', 0, 3, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (32, 1, '0 0', '0', 0, 4, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (33, 1, '9999 1', '10000', 0, 5, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (34, 2, '5 3', '5', 1, 1, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (35, 2, '3 5', '5', 0, 2, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (36, 2, '10 10', '10', 0, 3, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (37, 2, '-1 -5', '-1', 0, 4, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (38, 2, '0 100', '100', 0, 5, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (39, 3, '-5', '5', 1, 1, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (40, 3, '10', '10', 0, 2, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (41, 3, '0', '0', 0, 3, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (42, 3, '-999', '999', 0, 4, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (43, 4, '7', '奇数', 1, 1, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (44, 4, '4', '偶数', 0, 2, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (45, 4, '0', '偶数', 0, 3, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (46, 4, '-5', '奇数', 0, 4, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (47, 5, '100', '5050', 1, 1, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (48, 5, '1', '1', 0, 2, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (49, 5, '5', '15', 0, 3, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (50, 5, '10', '55', 0, 4, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (51, 6, 'hello', '5', 1, 1, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (52, 6, '', '0', 0, 2, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (53, 6, 'programming', '11', 0, 3, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (54, 6, '12345', '5', 0, 4, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (55, 7, 'hello', 'olleh', 1, 1, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (56, 7, 'abc', 'cba', 0, 2, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (57, 7, '12345', '54321', 0, 3, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (58, 8, 'programming
m', '2', 1, 1, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (59, 8, 'hello
l', '2', 0, 2, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (60, 8, 'test
t', '2', 0, 3, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (61, 8, 'abc
d', '0', 0, 4, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (66, 10, '5
3 1 4 2 5', '5', 1, 1, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (67, 10, '3
10 5 20', '20', 0, 2, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (68, 10, '1
-5', '-5', 0, 3, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (69, 10, '4
-1 -2 -3 -4', '-1', 0, 4, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (70, 11, '5
3 1 4 2 5', '1 2 3 4 5', 1, 1, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (71, 11, '5
5 4 3 2 1', '1 2 3 4 5', 0, 2, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (72, 11, '5
1 2 3 4 5', '1 2 3 4 5', 0, 3, '2026-03-13 18:19:39', '2026-03-13 18:19:39');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (77, 3, '1 2 3', '6', 1, 1, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (78, 3, '10 20 30', '60', 0, 2, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (79, 3, '-1 -2 -3', '-6', 0, 3, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (80, 4, '1 2 3 4', '10', 1, 1, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (81, 4, '10 20 30 40', '100', 0, 2, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (82, 4, '-1 -2 -3 -4', '-10', 0, 3, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (83, 5, '1 2 3', '6', 1, 1, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (84, 5, '5 10 15', '30', 0, 2, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (85, 5, '0 0 0', '0', 0, 3, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (86, 6, '2024', '是闰年', 1, 1, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (87, 6, '2000', '是闰年', 0, 2, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (88, 6, '1900', '不是闰年', 0, 3, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (89, 6, '2023', '不是闰年', 0, 4, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (90, 7, '5', '0 1 1 2 3', 1, 1, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (91, 7, '1', '0', 0, 2, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (92, 7, '10', '0 1 1 2 3 5 8 13 21 34', 0, 3, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (93, 8, 'hello', 'olleh', 1, 1, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (94, 8, 'abc', 'cba', 0, 2, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (95, 8, '12345', '54321', 0, 3, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (99, 11, '5 3', '8', 1, 1, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (100, 11, '10 20', '30', 0, 2, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (101, 11, '-5 -3', '-8', 0, 3, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (102, 12, '5 3', '5', 1, 1, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (103, 12, '3 5', '5', 0, 2, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (104, 12, '10 10', '10', 0, 3, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (105, 12, '-1 -5', '-1', 0, 4, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (106, 13, '-5', '5', 1, 1, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (107, 13, '10', '10', 0, 2, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (108, 13, '0', '0', 0, 3, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (109, 14, '7', '奇数', 1, 1, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (110, 14, '4', '偶数', 0, 2, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (111, 14, '0', '偶数', 0, 3, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (112, 15, '100', '5050', 1, 1, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (113, 15, '1', '1', 0, 2, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (114, 15, '5', '15', 0, 3, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (115, 16, 'hello', '5', 1, 1, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (116, 16, '', '0', 0, 2, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (117, 16, 'programming', '11', 0, 3, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (118, 17, 'programming
m', '2', 1, 1, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (119, 17, 'hello
l', '2', 0, 2, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (120, 17, 'test
t', '2', 0, 3, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (121, 18, '5
1 2 3 4 5', '15', 1, 1, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (122, 18, '3
10 20 30', '60', 0, 2, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (123, 18, '1
5', '5', 0, 3, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (124, 19, '5
3 1 4 2 5', '5', 1, 1, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (125, 19, '3
10 5 20', '20', 0, 2, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (126, 19, '1
-5', '-5', 0, 3, '2026-03-13 18:28:19', '2026-03-13 18:28:19');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (152, 9, '5
3 1 4 2 5', '1 2 3 4 5', 1, 1, '2026-03-13 18:37:05', '2026-03-13 18:37:05');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (153, 9, '3
5 3 1', '1 3 5', 0, 2, '2026-03-13 18:37:05', '2026-03-13 18:37:05');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (154, 9, '1
5', '5', 0, 3, '2026-03-13 18:37:05', '2026-03-13 18:37:05');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (155, 9, '5
5 4 3 2 1', '1 2 3 4 5', 0, 4, '2026-03-13 18:37:05', '2026-03-13 18:37:05');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (156, 20, '5
1 2 3 4 5', '3.0', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (157, 20, '3
10 20 30', '20.0', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (158, 21, '5
5 3 4 1 2', '1 2 3 4 5', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (159, 21, '3
3 2 1', '1 2 3', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (160, 22, '5
5 3 4 1 2', '1 2 3 4 5', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (161, 22, '4
4 3 2 1', '1 2 3 4', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (162, 23, '-5', '5', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (163, 23, '10', '10', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (164, 23, '0', '0', 0, 2, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (165, 24, '2020', '是', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (166, 24, '2021', '否', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (167, 24, '1900', '否', 0, 2, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (168, 24, '2000', '是', 0, 3, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (169, 25, '7', '奇数', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (170, 25, '10', '偶数', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (171, 25, '0', '偶数', 0, 2, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (172, 26, '5', '5', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (173, 26, '10', '55', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (174, 26, '1', '1', 0, 2, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (175, 27, '17', '是', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (176, 27, '15', '否', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (177, 27, '2', '是', 0, 2, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (178, 28, '5
1 5 3 7 2', '7', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (179, 28, '3
-5 -1 -10', '-1', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (180, 29, '5
1 5 3 7 2', '1', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (181, 29, '3
-5 -1 -10', '-10', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (182, 30, '5
5 3 4 1 2', '1 2 3 4 5', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (183, 30, '4
10 5 8 3', '3 5 8 10', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (184, 31, '5', '120', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (185, 31, '10', '3628800', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (186, 31, '0', '1', 0, 2, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (187, 32, '5
5 3 4 1 2', '1 2 3 4 5', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (188, 32, '3
3 1 2', '1 2 3', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (189, 33, '10 20', '10 20', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (190, 34, '3 5', '5 3', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (191, 35, '123 3.14 hello', '123 3.14 hello', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (192, 36, 'Hello World', 'Hello World', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (193, 37, '3.1415926', '3.14', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (194, 38, '-5', '负数', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (195, 38, '10', '正数', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (196, 38, '0', '零', 0, 2, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (197, 39, '85', '良好', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (198, 39, '95', '优秀', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (199, 39, '55', '不及格', 0, 2, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (200, 40, '2020', '是', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (201, 40, '2021', '否', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (202, 41, '10', '55', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (203, 41, '100', '5050', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (204, 42, '3', '1*1=1
1*2=2 2*2=4
1*3=3 2*3=6 3*3=9', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (205, 43, '5', '120', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (206, 43, '10', '3628800', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (207, 44, '12345', '54321', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (208, 44, '100', '1', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (209, 45, '5
1 2 3 4 5', '15', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (210, 45, '3
10 -5 8', '13', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (211, 46, '5
1 5 3 7 2', '7', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (212, 46, '3
-5 -1 -10', '-1', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (213, 47, '5
1 2 3 4 5', '5 4 3 2 1', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (214, 47, '3
a b c', 'c b a', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (215, 48, 'hello', '5', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (216, 48, 'programming', '11', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (217, 49, 'Hello
World', 'HelloWorld', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (218, 49, 'a
b', 'ab', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (219, 50, 'HelloWorld', 'helloworld
HELLOWORLD', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (220, 51, 'HelloWorld
World', '是', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (221, 51, 'HelloWorld
Python', '否', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (222, 52, 'hello world
l', '3', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (223, 52, 'aaaaa
a', '5', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (224, 53, '3 5', '8', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (225, 53, '10 20', '30', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (226, 54, '17', '是', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (227, 54, '15', '否', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (228, 55, '5
1 2 3 4 5', '15', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (229, 56, '5
1 5 3 7 2', '7', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (230, 57, '3 5', '8', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (231, 57, '3 5 7', '15', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (232, 58, '5', '120', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (233, 58, '10', '3628800', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (234, 59, '5', '5', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (235, 59, '10', '55', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (236, 59, '1', '1', 0, 2, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (237, 60, '张三
18
计算机', '姓名: 张三, 年龄: 18, 专业: 计算机', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (238, 61, '张三
18', '姓名: 张三, 年龄: 18', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (239, 62, '张三
20', '姓名: 张三, 年龄: 20', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (240, 63, 'test', 'test', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (241, 64, '张三
18', '姓名: 张三, 年龄: 18', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (242, 65, 'Dog', '汪汪汪', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (243, 65, 'Cat', '喵喵喵', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (244, 66, 'hello', 'hello', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (245, 67, '123
3.14
hello', '123 3.14 hello', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (246, 68, 'Hello Python', 'Hello Python', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (247, 69, '5 10', '10', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (248, 69, '15 3', '15', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (249, 70, '3', '春季', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (250, 70, '7', '夏季', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (251, 71, '10', '55', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (252, 71, '100', '5050', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (253, 72, '3
1 2 3', '1
2
3', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (254, 73, '50
50', '恭喜，猜对了！', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (255, 74, '5
1 2 3 4 5
6', '1 2 3 4 5 6', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (256, 75, '5
1 2 3 4 5', '2 3 4', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (257, 76, '5
1 2 3 4 5', '2 3 4', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (258, 77, '张三
18', '张三: 18', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (259, 78, '3
1 2 3', '1 2 3', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (260, 79, 'hello', 'olleh', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (261, 79, 'programming', 'gnimmargorp', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (262, 80, 'hello world hello', 'hello: 2
world: 1', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (263, 81, '3 5', '8', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (264, 82, '5', '10', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (265, 82, '5 3', '8', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (266, 83, '3
1 2 3', '6', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (267, 84, '5 3', '8 2', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (268, 85, '5', '10', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (269, 86, '5', '120', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (270, 86, '10', '3628800', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (271, 87, '张三
18', '姓名: 张三, 年龄: 18', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (272, 88, '张三
18', '姓名: 张三, 年龄: 18', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (273, 89, '张三
20', '姓名: 张三, 年龄: 20', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (274, 90, 'Dog', '汪汪汪', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (275, 91, 'Dog', '汪汪汪', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (276, 91, 'Cat', '喵喵喵', 0, 1, '2026-03-20 07:13:24', '2026-03-20 07:13:24');
INSERT INTO `test_case` (`id`, `problem_id`, `input`, `output`, `is_sample`, `sort_order`, `create_time`, `update_time`) VALUES (277, 92, '张三
18', 'Person: 姓名=张三, 年龄=18', 1, 0, '2026-03-20 07:13:24', '2026-03-20 07:13:24');

-- Table structure for user
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码（加密）',
  `role` tinyint NOT NULL DEFAULT '0' COMMENT '角色：0-普通用户，1-管理员',
  `language` varchar(20) DEFAULT 'java' COMMENT '常用编程语言',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `ability_profile` text,
  `status` int DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- Data for user
INSERT INTO `user` (`id`, `username`, `password`, `role`, `language`, `create_time`, `update_time`, `ability_profile`, `status`) VALUES (1, 'admin', 'admin123', 1, 'java', '2026-03-11 11:43:53', '2026-03-13 05:28:34', NULL, 1);
INSERT INTO `user` (`id`, `username`, `password`, `role`, `language`, `create_time`, `update_time`, `ability_profile`, `status`) VALUES (2, 'testuser', '123456', 0, 'java', '2026-03-12 14:44:00', '2026-03-13 16:47:12', NULL, 1);
INSERT INTO `user` (`id`, `username`, `password`, `role`, `language`, `create_time`, `update_time`, `ability_profile`, `status`) VALUES (3, 'fhrq', 'zz12321321', 0, 'java', '2026-03-13 05:29:06', '2026-03-13 05:29:06', NULL, 1);
INSERT INTO `user` (`id`, `username`, `password`, `role`, `language`, `create_time`, `update_time`, `ability_profile`, `status`) VALUES (5, 'test_user_1773940657', '123456', 0, 'java', '2026-03-20 01:17:49', '2026-03-20 01:17:49', NULL, NULL);
INSERT INTO `user` (`id`, `username`, `password`, `role`, `language`, `create_time`, `update_time`, `ability_profile`, `status`) VALUES (6, 'test_user_1773940707', '123456', 0, 'java', '2026-03-20 01:18:39', '2026-03-20 01:18:39', NULL, NULL);
INSERT INTO `user` (`id`, `username`, `password`, `role`, `language`, `create_time`, `update_time`, `ability_profile`, `status`) VALUES (7, 'test_user_1773940727', '123456', 0, 'java', '2026-03-20 01:18:59', '2026-03-20 01:18:59', NULL, NULL);
INSERT INTO `user` (`id`, `username`, `password`, `role`, `language`, `create_time`, `update_time`, `ability_profile`, `status`) VALUES (8, 'test_user_1773940911', '123456', 0, 'java', '2026-03-20 01:22:03', '2026-03-20 01:22:03', NULL, NULL);
INSERT INTO `user` (`id`, `username`, `password`, `role`, `language`, `create_time`, `update_time`, `ability_profile`, `status`) VALUES (9, 'test_user_1773940949', '123456', 0, 'java', '2026-03-20 01:22:41', '2026-03-20 01:22:41', NULL, NULL);
INSERT INTO `user` (`id`, `username`, `password`, `role`, `language`, `create_time`, `update_time`, `ability_profile`, `status`) VALUES (10, 'test_user_1773941025', '123456', 0, 'python', '2026-03-20 01:23:57', '2026-03-20 01:23:57', NULL, NULL);

-- Table structure for user_assessment
DROP TABLE IF EXISTS `user_assessment`;
CREATE TABLE `user_assessment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `language` varchar(50) NOT NULL,
  `direction` varchar(100) NOT NULL,
  `score` int NOT NULL,
  `result` text,
  `ability_level` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `user_assessment_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for user_assessment
INSERT INTO `user_assessment` (`id`, `user_id`, `language`, `direction`, `score`, `result`, `ability_level`, `create_time`) VALUES (1, 1, 'java', 'algorithm', 40, '1:错误;2:错误;3:正确;4:正确;5:错误;6:正确;7:正确;8:错误;9:错误;10:错误;', '入门', '2026-03-20 03:23:45');
INSERT INTO `user_assessment` (`id`, `user_id`, `language`, `direction`, `score`, `result`, `ability_level`, `create_time`) VALUES (2, 1, 'java', 'backend', 60, '48:错误;49:正确;50:正确;41:正确;42:错误;43:错误;44:正确;45:正确;46:错误;47:正确;', '入门', '2026-03-20 03:33:58');
INSERT INTO `user_assessment` (`id`, `user_id`, `language`, `direction`, `score`, `result`, `ability_level`, `create_time`) VALUES (3, 1, 'java', 'algorithm', 40, '1:错误;17:错误;2:错误;3:正确;4:正确;20:错误;9:正确;12:错误;13:正确;14:错误;', '入门', '2026-03-20 03:37:53');
INSERT INTO `user_assessment` (`id`, `user_id`, `language`, `direction`, `score`, `result`, `ability_level`, `create_time`) VALUES (4, 1, 'java', 'algorithm', 30, '16:错误;3:错误;20:错误;5:正确;6:错误;8:正确;9:错误;10:正确;11:错误;14:错误;', '零基础', '2026-03-20 04:29:23');

-- Table structure for user_class
DROP TABLE IF EXISTS `user_class`;
CREATE TABLE `user_class` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `class_id` bigint NOT NULL,
  `role` varchar(50) NOT NULL,
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_class` (`user_id`,`class_id`),
  KEY `class_id` (`class_id`),
  CONSTRAINT `user_class_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_class_ibfk_2` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for user_class

-- Table structure for user_knowledge_mastery
DROP TABLE IF EXISTS `user_knowledge_mastery`;
CREATE TABLE `user_knowledge_mastery` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `knowledge_id` bigint NOT NULL,
  `mastery_level` int NOT NULL,
  `score` int NOT NULL,
  `last_practice_time` datetime DEFAULT NULL,
  `practice_count` int DEFAULT '0',
  `correct_count` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_knowledge` (`user_id`,`knowledge_id`),
  KEY `knowledge_id` (`knowledge_id`),
  CONSTRAINT `user_knowledge_mastery_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_knowledge_mastery_ibfk_2` FOREIGN KEY (`knowledge_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for user_knowledge_mastery

-- Table structure for user_path_progress
DROP TABLE IF EXISTS `user_path_progress`;
CREATE TABLE `user_path_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `path_id` bigint NOT NULL,
  `current_chapter_id` bigint DEFAULT NULL,
  `current_level_id` bigint DEFAULT NULL,
  `completed_chapters` text,
  `completed_levels` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `path_id` (`path_id`),
  CONSTRAINT `user_path_progress_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_path_progress_ibfk_2` FOREIGN KEY (`path_id`) REFERENCES `learning_path` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for user_path_progress
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (4, 1, 11, NULL, NULL, '', '', '2026-03-20 03:55:00', '2026-03-20 03:55:00');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (5, 1, 20, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (6, 1, 19, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (7, 1, 18, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (8, 1, 17, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (9, 1, 16, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (10, 1, 15, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (11, 1, 14, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (12, 1, 13, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (13, 1, 12, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (14, 1, 1, 1, 1, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (15, 1, 10, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (16, 1, 9, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (17, 1, 8, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (18, 1, 7, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (19, 1, 6, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (20, 1, 5, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (21, 1, 4, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (22, 1, 3, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');
INSERT INTO `user_path_progress` (`id`, `user_id`, `path_id`, `current_chapter_id`, `current_level_id`, `completed_chapters`, `completed_levels`, `create_time`, `update_time`) VALUES (23, 1, 2, NULL, NULL, '', '', '2026-03-20 03:55:02', '2026-03-20 03:55:02');

-- Table structure for user_role
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for user_role

-- Table structure for user_solution
DROP TABLE IF EXISTS `user_solution`;
CREATE TABLE `user_solution` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `problem_id` bigint NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `code` text,
  `language` varchar(50) DEFAULT NULL,
  `status` int DEFAULT '0',
  `view_count` int DEFAULT '0',
  `like_count` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `problem_id` (`problem_id`),
  CONSTRAINT `user_solution_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_solution_ibfk_2` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for user_solution

-- Table structure for wrong_book
DROP TABLE IF EXISTS `wrong_book`;
CREATE TABLE `wrong_book` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `wrong_book_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for wrong_book
INSERT INTO `wrong_book` (`id`, `user_id`, `create_time`, `update_time`) VALUES (1, 9, '2026-03-20 01:22:48', '2026-03-20 01:22:48');
INSERT INTO `wrong_book` (`id`, `user_id`, `create_time`, `update_time`) VALUES (2, 10, '2026-03-20 01:24:01', '2026-03-20 01:24:01');
INSERT INTO `wrong_book` (`id`, `user_id`, `create_time`, `update_time`) VALUES (3, 1, '2026-03-20 02:31:03', '2026-03-20 02:31:03');

-- Table structure for wrong_book_item
DROP TABLE IF EXISTS `wrong_book_item`;
CREATE TABLE `wrong_book_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `wrong_book_id` bigint NOT NULL,
  `problem_id` bigint NOT NULL,
  `submit_id` bigint NOT NULL,
  `code` text NOT NULL,
  `language` varchar(50) NOT NULL,
  `error_message` text,
  `knowledge_points` text,
  `review_status` int DEFAULT '0',
  `last_review_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `wrong_book_id` (`wrong_book_id`),
  KEY `problem_id` (`problem_id`),
  CONSTRAINT `wrong_book_item_ibfk_1` FOREIGN KEY (`wrong_book_id`) REFERENCES `wrong_book` (`id`) ON DELETE CASCADE,
  CONSTRAINT `wrong_book_item_ibfk_2` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data for wrong_book_item

SET FOREIGN_KEY_CHECKS = 1;
