-- 2026-03-27 learning experience patch
-- Goal:
-- 1. Fix schema drift that blocks learning resources / level-problem binding.
-- 2. Normalize learning path language values for frontend/backend filtering.
-- 3. Supplement the problem bank with enough practice problems to support all levels.
-- 4. Generate learning resources for every level and bind problems to all levels.

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1) Compatibility fixes
-- ------------------------------------------------------------

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'learning_resource'
        AND COLUMN_NAME = 'level_id'
    ),
    'SELECT ''learning_resource.level_id exists''',
    'ALTER TABLE learning_resource ADD COLUMN level_id BIGINT NULL AFTER id'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'learning_resource'
        AND COLUMN_NAME = 'order_num'
    ),
    'SELECT ''learning_resource.order_num exists''',
    'ALTER TABLE learning_resource ADD COLUMN order_num INT NULL DEFAULT 0 AFTER knowledge_points'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'path_level_problem'
        AND COLUMN_NAME = 'update_time'
    ),
    'SELECT ''path_level_problem.update_time exists''',
    'ALTER TABLE path_level_problem ADD COLUMN update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER create_time'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 2) Normalize learning path language values
-- ------------------------------------------------------------

UPDATE learning_path SET language = 'java' WHERE LOWER(language) = 'java';
UPDATE learning_path SET language = 'python' WHERE LOWER(language) = 'python';
UPDATE learning_path SET language = '通用' WHERE language IN ('all', 'ALL', 'All', '通用');

-- ------------------------------------------------------------
-- 3) Supplement problem bank
-- ------------------------------------------------------------

INSERT INTO problem (
  title, content, input, output, difficulty, language, time_limit, memory_limit,
  tags, knowledge_points, hints, sample_explanation, status, supported_languages,
  create_time, update_time
)
SELECT
  seed.title,
  seed.content,
  seed.example_input,
  seed.example_output,
  seed.difficulty,
  seed.problem_language,
  1000,
  256,
  seed.tags,
  seed.knowledge_points,
  seed.hints,
  seed.sample_explanation,
  'PUBLISHED',
  'Java,Python',
  NOW(),
  NOW()
FROM (
  SELECT '数组最大值' AS title, '给定一个长度为 n 的整数数组，输出其中的最大值。' AS content, '5\n1 3 2 9 7' AS example_input, '9' AS example_output, 0 AS difficulty, 'algorithm' AS problem_language, '数组,遍历' AS tags, '数组,遍历,比较' AS knowledge_points, '先用一个变量记录当前最大值，再逐个比较更新。' AS hints, '样例中最大的数是 9。' AS sample_explanation
  UNION ALL SELECT '有效括号', '给定只包含 ()[]{} 的字符串，判断括号是否完全匹配。匹配输出 valid，否则输出 invalid。', '([]{})', 'valid', 1, 'algorithm', '栈,字符串', '栈,括号匹配,字符串', '遇到左括号入栈，遇到右括号时和栈顶匹配。', '样例中的括号可以完整两两配对。'
  UNION ALL SELECT '队列模拟', '给定 n 条队列操作。操作只有 push x、pop、front。每次执行 pop 或 front 时输出结果，空队列输出 EMPTY。', '6\npush 1\npush 2\nfront\npop\npop\npop', '1\n1\n2\nEMPTY', 1, 'algorithm', '队列,模拟', '队列,数据结构,模拟', '用先进先出的规则维护队列状态。', '先查看队首得到 1，依次弹出 1、2，最后队列为空。'
  UNION ALL SELECT '词频统计', '给定 n 个单词，输出出现次数最多的单词和次数；若次数相同，输出字典序最小的单词。', '6\napple banana apple orange banana apple', 'apple 3', 1, 'algorithm', '哈希表,字符串', '哈希表,词频统计,字符串', '统计每个单词次数后再处理并列情况。', 'apple 出现 3 次，是最高频单词。'
  UNION ALL SELECT '二叉树节点总和', '给定用层序数组表示的二叉树，空节点用 null 表示，输出所有非空节点数值之和。', '1,2,3,null,4', '10', 1, 'algorithm', '二叉树,遍历', '二叉树,层序遍历,求和', '只统计非 null 节点。', '1 + 2 + 3 + 4 = 10。'
  UNION ALL SELECT '图的最短路径步数', '给定无向图的点和边，以及起点和终点，输出最短路径经过的边数；不可达输出 -1。', '5 5\n1 2\n2 3\n3 4\n4 5\n1 5\n1 4', '2', 2, 'algorithm', '图,BFS', '图,广度优先搜索,最短路径', '无权图最短路径优先考虑 BFS。', '1 到 4 可以走 1-5-4，共 2 步。'
  UNION ALL SELECT '最长递增子序列', '给定一个整数数组，输出其最长严格递增子序列的长度。', '8\n10 9 2 5 3 7 101 18', '4', 2, 'algorithm', '动态规划,数组', '动态规划,最长递增子序列,数组', '可以用 O(n^2) DP 或更优的贪心加二分。', '最长递增子序列之一是 2 3 7 101。'
  UNION ALL SELECT '区间调度', '给定 n 个闭区间 [l, r]，选择尽可能多的互不重叠区间，输出最多可选数量。', '4\n1 3\n2 4\n3 5\n6 7', '3', 1, 'algorithm', '贪心,区间', '贪心,区间调度,排序', '按结束时间排序并尽量早地选择区间。', '可选区间为 [1,3]、[3,5]、[6,7]。'
  UNION ALL SELECT '全排列数量', '给定 n，输出 1 到 n 的全排列数量。', '4', '24', 1, 'algorithm', '回溯,数学', '回溯,排列,递归', '全排列数量等于 n 的阶乘。', '4 的全排列数量为 4! = 24。'
  UNION ALL SELECT '第K大元素', '给定数组和整数 k，输出数组中的第 k 大元素。', '6 2\n3 2 1 5 6 4', '5', 1, 'algorithm', '堆,数组', '堆,快速选择,数组', '维护大小为 k 的最小堆即可。', '第 2 大元素是 5。'
  UNION ALL SELECT 'HTTP状态码分类', '输入一个 HTTP 状态码，输出其所属类别：INFORMATIONAL、SUCCESS、REDIRECT、CLIENT_ERROR、SERVER_ERROR 或 UNKNOWN。', '404', 'CLIENT_ERROR', 0, 'algorithm', '网络,HTTP', 'HTTP协议,状态码,网络基础', '根据状态码的百位数进行分类。', '404 属于 4xx 客户端错误。'
  UNION ALL SELECT 'IP地址分类', '输入一个 IPv4 地址，按首段范围输出 A、B、C、D、E，非法地址输出 INVALID。', '192.168.1.10', 'C', 1, 'algorithm', '网络,字符串', 'TCP/IP,IP地址,字符串处理', '先校验格式和每段范围，再按首段分类。', '192 开头属于 C 类地址。'
  UNION ALL SELECT 'RESTful路径拆解', '输入一个 RESTful 路径，将非空路径段按空格输出。', '/users/42/orders/7', 'users 42 orders 7', 0, 'algorithm', '字符串,接口设计', 'RESTful,路径解析,字符串处理', '按 / 分割后过滤空字符串。', '路径段依次为 users、42、orders、7。'
  UNION ALL SELECT '查询参数解析', '输入形如 key=value&key2=value2 的查询串，按键名字典序输出 key=value，项之间用逗号连接。', 'page=2&size=10&sort=desc', 'page=2,size=10,sort=desc', 1, 'algorithm', '字符串,排序', 'RESTful,查询参数,排序', '先拆分成键值对，再按 key 排序。', 'page、size、sort 已经按字典序输出。'
  UNION ALL SELECT '学生成绩分组统计', '输入 n 个学生成绩，输出平均分和最高分，结果格式为 avg max，平均分保留两位小数。', '4\n80 90 70 100', '85.00 100', 0, 'database', '统计,基础', '数据库,聚合统计,平均值', '分别累计总分和最大值。', '总分 340，平均分 85.00，最高分 100。'
  UNION ALL SELECT '事务日志汇总', '输入若干条事务日志，每条为 IN 金额 或 OUT 金额，输出最终余额变化值。', '5\nIN 100\nOUT 40\nIN 30\nOUT 10\nIN 5', '85', 1, 'database', '日志,统计', '事务,日志处理,累加', '收入记为加，支出记为减。', '100 - 40 + 30 - 10 + 5 = 85。'
  UNION ALL SELECT 'ORM字段映射检查', '输入一个下划线风格字段名，输出对应的驼峰命名结果。', 'user_name', 'userName', 0, 'database', 'ORM,字符串', 'ORM,字段映射,命名规范', '下划线后的单词首字母要大写。', 'user_name 映射为 userName。'
  UNION ALL SELECT 'Flask路由匹配', '输入一条 Flask 风格路由模板和一条访问路径。模板中的 <id> 视为占位符。若匹配则输出 MATCH，否则输出 404。', '/users/<id>\n/users/42', 'MATCH', 1, 'algorithm', '后端,路由', 'Flask,路由匹配,字符串处理', '逐段比较，模板中的占位符可以匹配任意非空段。', '模板 /users/<id> 可以匹配 /users/42。'
  UNION ALL SELECT 'FastAPI参数校验', '输入用户名和年龄。用户名非空且年龄在 1 到 120 之间输出 valid，否则输出 invalid。', 'alice 23', 'valid', 0, 'algorithm', '后端,校验', 'FastAPI,参数校验,条件判断', '按题目要求检查每个参数是否合法。', 'alice 和 23 都合法。'
) AS seed
WHERE NOT EXISTS (
  SELECT 1
  FROM problem p
  WHERE p.title = seed.title
);

INSERT INTO test_case (
  problem_id, input, output, is_sample, sort_order, create_time, update_time
)
SELECT
  p.id,
  seed.case_input,
  seed.case_output,
  seed.is_sample,
  seed.sort_order,
  NOW(),
  NOW()
FROM (
  SELECT '数组最大值' AS problem_title, '5\n1 3 2 9 7' AS case_input, '9' AS case_output, 1 AS is_sample, 1 AS sort_order
  UNION ALL SELECT '数组最大值', '4\n-5 -2 -9 -1', '-1', 0, 2
  UNION ALL SELECT '有效括号', '([]{})', 'valid', 1, 1
  UNION ALL SELECT '有效括号', '([)]', 'invalid', 0, 2
  UNION ALL SELECT '队列模拟', '6\npush 1\npush 2\nfront\npop\npop\npop', '1\n1\n2\nEMPTY', 1, 1
  UNION ALL SELECT '队列模拟', '5\npop\npush 3\nfront\npush 4\npop', 'EMPTY\n3\n3', 0, 2
  UNION ALL SELECT '词频统计', '6\napple banana apple orange banana apple', 'apple 3', 1, 1
  UNION ALL SELECT '词频统计', '5\ncat dog dog cat ant', 'cat 2', 0, 2
  UNION ALL SELECT '二叉树节点总和', '1,2,3,null,4', '10', 1, 1
  UNION ALL SELECT '二叉树节点总和', '5,null,7,8', '20', 0, 2
  UNION ALL SELECT '图的最短路径步数', '5 5\n1 2\n2 3\n3 4\n4 5\n1 5\n1 4', '2', 1, 1
  UNION ALL SELECT '图的最短路径步数', '4 2\n1 2\n3 4\n1 4', '-1', 0, 2
  UNION ALL SELECT '最长递增子序列', '8\n10 9 2 5 3 7 101 18', '4', 1, 1
  UNION ALL SELECT '最长递增子序列', '6\n0 1 0 3 2 3', '4', 0, 2
  UNION ALL SELECT '区间调度', '4\n1 3\n2 4\n3 5\n6 7', '3', 1, 1
  UNION ALL SELECT '区间调度', '3\n1 2\n2 3\n3 4', '3', 0, 2
  UNION ALL SELECT '全排列数量', '4', '24', 1, 1
  UNION ALL SELECT '全排列数量', '1', '1', 0, 2
  UNION ALL SELECT '第K大元素', '6 2\n3 2 1 5 6 4', '5', 1, 1
  UNION ALL SELECT '第K大元素', '5 1\n7 4 9 2 6', '9', 0, 2
  UNION ALL SELECT 'HTTP状态码分类', '404', 'CLIENT_ERROR', 1, 1
  UNION ALL SELECT 'HTTP状态码分类', '200', 'SUCCESS', 0, 2
  UNION ALL SELECT 'IP地址分类', '192.168.1.10', 'C', 1, 1
  UNION ALL SELECT 'IP地址分类', '256.1.1.1', 'INVALID', 0, 2
  UNION ALL SELECT 'RESTful路径拆解', '/users/42/orders/7', 'users 42 orders 7', 1, 1
  UNION ALL SELECT 'RESTful路径拆解', '/api/v1/status', 'api v1 status', 0, 2
  UNION ALL SELECT '查询参数解析', 'page=2&size=10&sort=desc', 'page=2,size=10,sort=desc', 1, 1
  UNION ALL SELECT '查询参数解析', 'b=2&a=1', 'a=1,b=2', 0, 2
  UNION ALL SELECT '学生成绩分组统计', '4\n80 90 70 100', '85.00 100', 1, 1
  UNION ALL SELECT '学生成绩分组统计', '3\n60 60 90', '70.00 90', 0, 2
  UNION ALL SELECT '事务日志汇总', '5\nIN 100\nOUT 40\nIN 30\nOUT 10\nIN 5', '85', 1, 1
  UNION ALL SELECT '事务日志汇总', '3\nOUT 50\nIN 20\nOUT 10', '-40', 0, 2
  UNION ALL SELECT 'ORM字段映射检查', 'user_name', 'userName', 1, 1
  UNION ALL SELECT 'ORM字段映射检查', 'created_at_time', 'createdAtTime', 0, 2
  UNION ALL SELECT 'Flask路由匹配', '/users/<id>\n/users/42', 'MATCH', 1, 1
  UNION ALL SELECT 'Flask路由匹配', '/users/<id>\n/orders/42', '404', 0, 2
  UNION ALL SELECT 'FastAPI参数校验', 'alice 23', 'valid', 1, 1
  UNION ALL SELECT 'FastAPI参数校验', 'bob 0', 'invalid', 0, 2
) AS seed
JOIN problem p ON p.title = seed.problem_title
LEFT JOIN test_case t
  ON t.problem_id = p.id
 AND t.input = seed.case_input
 AND t.output = seed.case_output
WHERE t.id IS NULL;

-- ------------------------------------------------------------
-- 4) Auto-generate level learning resources
-- ------------------------------------------------------------

INSERT INTO learning_resource (
  level_id, name, type, url, description, knowledge_points, order_num, visibility, create_time, update_time
)
SELECT
  l.id,
  CONCAT(l.name, ' 学习指引'),
  'tutorial',
  CONCAT(
    '# ', l.name, CHAR(10), CHAR(10),
    '## 本关目标', CHAR(10),
    '- 理解本关核心概念', CHAR(10),
    '- 能结合示例完成至少一道练习题', CHAR(10),
    '- 为下一关打下可复用的基础', CHAR(10), CHAR(10),
    '## 核心知识点', CHAR(10),
    IF(
      l.knowledge_points IS NULL OR l.knowledge_points = '',
      '- 结合章节主题完成基础理解',
      CONCAT('- ', REPLACE(l.knowledge_points, ',', CONCAT(CHAR(10), '- ')))
    ),
    CHAR(10), CHAR(10),
    '## 建议学习顺序', CHAR(10),
    '1. 先阅读概念和关键词', CHAR(10),
    '2. 再完成页面中的练习题', CHAR(10),
    '3. 最后复盘易错点并整理笔记'
  ),
  CONCAT('用于快速掌握“', l.name, '”的学习指引'),
  l.knowledge_points,
  1,
  0,
  NOW(),
  NOW()
FROM path_level l
WHERE NOT EXISTS (
  SELECT 1
  FROM learning_resource r
  WHERE r.level_id = l.id
    AND r.order_num = 1
);

INSERT INTO learning_resource (
  level_id, name, type, url, description, knowledge_points, order_num, visibility, create_time, update_time
)
SELECT
  l.id,
  CONCAT(l.name, ' 练习建议'),
  'example',
  CONCAT(
    '# ', l.name, ' 练习建议', CHAR(10), CHAR(10),
    '## 过关方式', CHAR(10),
    '1. 先完成本关至少 1 道题目', CHAR(10),
    '2. 如果提交失败，优先查看输入输出边界', CHAR(10),
    '3. 完成后回顾知识点与题目映射', CHAR(10), CHAR(10),
    '## 推荐关注', CHAR(10),
    IF(
      l.knowledge_points IS NULL OR l.knowledge_points = '',
      '- 本关更偏重对章节主题的整体理解',
      CONCAT('- ', REPLACE(l.knowledge_points, ',', CONCAT(CHAR(10), '- ')))
    ),
    CHAR(10), CHAR(10),
    '## 完成标准', CHAR(10),
    '- 能解释关键概念', CHAR(10),
    '- 能独立完成练习', CHAR(10),
    '- 能说明本关与下一关的衔接关系'
  ),
  CONCAT('围绕“', l.name, '”的练习与复盘建议'),
  l.knowledge_points,
  2,
  0,
  NOW(),
  NOW()
FROM path_level l
WHERE NOT EXISTS (
  SELECT 1
  FROM learning_resource r
  WHERE r.level_id = l.id
    AND r.order_num = 2
);

-- ------------------------------------------------------------
-- 5) Bind problems to every level
-- ------------------------------------------------------------

INSERT IGNORE INTO path_level_problem (
  level_id, problem_id, order_num, create_time, update_time
)
SELECT
  seed.level_id,
  p.id,
  seed.order_num,
  NOW(),
  NOW()
FROM (
  SELECT 9 AS level_id, '词频统计' AS problem_title, 3 AS order_num
  UNION ALL SELECT 10, '查询参数解析', 2
  UNION ALL SELECT 11, '学生成绩分组统计', 2
  UNION ALL SELECT 18, '词频统计', 3
  UNION ALL SELECT 19, '词频统计', 2
  UNION ALL SELECT 20, '词频统计', 2
  UNION ALL SELECT 21, '查询参数解析', 2
  UNION ALL SELECT 22, '学生成绩分组统计', 2
  UNION ALL SELECT 23, '冒泡排序', 1
  UNION ALL SELECT 23, '二分查找', 2
  UNION ALL SELECT 24, '冒泡排序', 1
  UNION ALL SELECT 24, '区间调度', 2
  UNION ALL SELECT 25, '二分查找', 1
  UNION ALL SELECT 25, '图的最短路径步数', 2
  UNION ALL SELECT 26, '数组最大值', 1
  UNION ALL SELECT 26, '链表反转', 2
  UNION ALL SELECT 27, '有效括号', 1
  UNION ALL SELECT 27, '队列模拟', 2
  UNION ALL SELECT 28, '两数之和', 1
  UNION ALL SELECT 28, '词频统计', 2
  UNION ALL SELECT 29, '数组最大值', 1
  UNION ALL SELECT 29, '有效括号', 2
  UNION ALL SELECT 30, '链表反转', 1
  UNION ALL SELECT 30, '词频统计', 2
  UNION ALL SELECT 31, '二叉树节点总和', 1
  UNION ALL SELECT 31, '链表反转', 2
  UNION ALL SELECT 32, '图的最短路径步数', 1
  UNION ALL SELECT 33, '第K大元素', 1
  UNION ALL SELECT 34, '最长递增子序列', 1
  UNION ALL SELECT 34, '斐波那契数列', 2
  UNION ALL SELECT 35, '区间调度', 1
  UNION ALL SELECT 36, '全排列数量', 1
  UNION ALL SELECT 37, '最长递增子序列', 1
  UNION ALL SELECT 37, '区间调度', 2
  UNION ALL SELECT 38, '二叉树节点总和', 1
  UNION ALL SELECT 38, '图的最短路径步数', 2
  UNION ALL SELECT 39, 'HTTP请求', 1
  UNION ALL SELECT 39, 'HTTP状态码分类', 2
  UNION ALL SELECT 40, 'IP地址分类', 1
  UNION ALL SELECT 41, 'RESTful路径拆解', 1
  UNION ALL SELECT 41, '查询参数解析', 2
  UNION ALL SELECT 42, 'SQL查询', 1
  UNION ALL SELECT 42, '学生成绩分组统计', 2
  UNION ALL SELECT 43, '事务日志汇总', 1
  UNION ALL SELECT 44, 'ORM字段映射检查', 1
  UNION ALL SELECT 45, 'RESTful路径拆解', 1
  UNION ALL SELECT 45, '查询参数解析', 2
  UNION ALL SELECT 46, 'SQL查询', 1
  UNION ALL SELECT 46, '事务日志汇总', 2
  UNION ALL SELECT 47, '查询参数解析', 1
  UNION ALL SELECT 47, 'HTTP状态码分类', 2
  UNION ALL SELECT 48, 'RESTful路径拆解', 1
  UNION ALL SELECT 48, '查询参数解析', 2
  UNION ALL SELECT 49, 'ORM字段映射检查', 1
  UNION ALL SELECT 50, 'HTTP状态码分类', 1
  UNION ALL SELECT 50, 'RESTful路径拆解', 2
  UNION ALL SELECT 51, 'HTTP状态码分类', 1
  UNION ALL SELECT 51, 'RESTful路径拆解', 2
  UNION ALL SELECT 52, '查询参数解析', 1
  UNION ALL SELECT 52, '事务日志汇总', 2
  UNION ALL SELECT 53, '事务日志汇总', 1
  UNION ALL SELECT 53, '查询参数解析', 2
  UNION ALL SELECT 54, 'RESTful路径拆解', 1
  UNION ALL SELECT 54, 'HTTP状态码分类', 2
  UNION ALL SELECT 55, 'Flask路由匹配', 1
  UNION ALL SELECT 56, 'ORM字段映射检查', 1
  UNION ALL SELECT 56, '查询参数解析', 2
  UNION ALL SELECT 57, 'ORM字段映射检查', 1
  UNION ALL SELECT 58, 'FastAPI参数校验', 1
  UNION ALL SELECT 58, '查询参数解析', 2
  UNION ALL SELECT 59, 'HTTP状态码分类', 1
  UNION ALL SELECT 59, 'RESTful路径拆解', 2
  UNION ALL SELECT 60, '查询参数解析', 1
  UNION ALL SELECT 60, 'FastAPI参数校验', 2
  UNION ALL SELECT 61, 'Flask路由匹配', 1
  UNION ALL SELECT 61, '事务日志汇总', 2
  UNION ALL SELECT 62, 'FastAPI参数校验', 1
  UNION ALL SELECT 62, 'RESTful路径拆解', 2
) AS seed
JOIN problem p ON p.title = seed.problem_title;

-- Fill missing path/chapter context when the current schema carries these columns.
UPDATE path_level_problem plp
JOIN path_level l ON l.id = plp.level_id
JOIN path_chapter c ON c.id = l.chapter_id
SET
  plp.chapter_id = c.id,
  plp.path_id = c.path_id
WHERE (plp.chapter_id IS NULL OR plp.path_id IS NULL);

-- Sync denormalized problem_ids back into path_level so the existing frontend counts remain correct.
UPDATE path_level l
LEFT JOIN (
  SELECT level_id, GROUP_CONCAT(problem_id ORDER BY order_num ASC SEPARATOR ',') AS ids
  FROM path_level_problem
  GROUP BY level_id
) x ON x.level_id = l.id
SET l.problem_ids = x.ids;

-- ------------------------------------------------------------
-- 6) Verification
-- ------------------------------------------------------------

SELECT COUNT(*) AS total_problems FROM problem;
SELECT COUNT(*) AS total_test_cases FROM test_case;
SELECT COUNT(*) AS total_learning_resources FROM learning_resource;
SELECT COUNT(*) AS total_level_problem_bindings FROM path_level_problem;
SELECT COUNT(*) AS levels_without_resources
FROM path_level l
LEFT JOIN learning_resource r ON r.level_id = l.id
WHERE r.id IS NULL;
SELECT COUNT(*) AS levels_without_problems
FROM path_level l
LEFT JOIN path_level_problem plp ON plp.level_id = l.id
WHERE plp.id IS NULL;
