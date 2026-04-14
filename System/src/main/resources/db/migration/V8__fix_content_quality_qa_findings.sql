-- Fix QA findings from the Java-first Agent content quality flow.
-- Do not edit V6/V7 because they have already been applied by Flyway.

INSERT INTO `knowledge_point` (`name`, `parent_id`, `level`, `category`, `difficulty`, `description`, `create_time`, `update_time`)
SELECT v.`name`, NULL, 1, 'qa-standardized', 0, v.`description`, NOW(), NOW()
FROM (
  SELECT 'Boot' AS `name`, 'Legacy Spring Boot token used by existing problems' AS `description` UNION ALL
  SELECT 'Cloud', 'Legacy Spring Cloud token used by existing problems' UNION ALL
  SELECT 'FastAPI', 'Python backend framework token kept for legacy content quality compatibility' UNION ALL
  SELECT 'Flask', 'Python backend framework token kept for legacy content quality compatibility' UNION ALL
  SELECT 'IO', 'Input and output processing' UNION ALL
  SELECT 'IP地址', 'IP address parsing and classification' UNION ALL
  SELECT 'MVC', 'MVC framework pattern' UNION ALL
  SELECT 'ORM', 'Object relational mapping basics' UNION ALL
  SELECT 'RESTful', 'RESTful API path and query design' UNION ALL
  SELECT 'Spring', 'Spring framework family token' UNION ALL
  SELECT 'TCP/IP', 'TCP/IP networking basics' UNION ALL
  SELECT '事务', 'Database transaction basics' UNION ALL
  SELECT '二分查找', 'Binary search' UNION ALL
  SELECT '区间调度', 'Interval scheduling' UNION ALL
  SELECT '参数校验', 'Request and parameter validation' UNION ALL
  SELECT '取模运算', 'Modulo operation' UNION ALL
  SELECT '命名规范', 'Naming convention checks' UNION ALL
  SELECT '回溯', 'Backtracking token used by existing problems' UNION ALL
  SELECT '字段映射', 'Field mapping checks' UNION ALL
  SELECT '字符串处理', 'String parsing and formatting' UNION ALL
  SELECT '层序遍历', 'Binary tree level order traversal' UNION ALL
  SELECT '平均值', 'Average calculation' UNION ALL
  SELECT '循环', 'Loop control' UNION ALL
  SELECT '快速选择', 'Quickselect algorithm' UNION ALL
  SELECT '括号匹配', 'Bracket matching' UNION ALL
  SELECT '指针操作', 'Pointer or link manipulation' UNION ALL
  SELECT '排列', 'Permutation counting or generation' UNION ALL
  SELECT '排序', 'Sorting token used by existing problems' UNION ALL
  SELECT '数学', 'Math basics' UNION ALL
  SELECT '数学运算', 'Arithmetic operations' UNION ALL
  SELECT '数据结构', 'Data structure basics' UNION ALL
  SELECT '日志处理', 'Log processing' UNION ALL
  SELECT '最短路径', 'Shortest path' UNION ALL
  SELECT '最长递增子序列', 'Longest increasing subsequence' UNION ALL
  SELECT '条件判断', 'Conditional branching' UNION ALL
  SELECT '查询', 'Query basics' UNION ALL
  SELECT '查询参数', 'Query parameter parsing' UNION ALL
  SELECT '模拟', 'Simulation problems' UNION ALL
  SELECT '比较', 'Comparison operations' UNION ALL
  SELECT '求和', 'Summation' UNION ALL
  SELECT '状态码', 'HTTP status code classification' UNION ALL
  SELECT '累加', 'Accumulation pattern' UNION ALL
  SELECT '聚合统计', 'Aggregation statistics' UNION ALL
  SELECT '词频统计', 'Word frequency counting' UNION ALL
  SELECT '请求', 'HTTP request basics' UNION ALL
  SELECT '贪心', 'Greedy algorithm token used by existing problems' UNION ALL
  SELECT '路径解析', 'Path parsing' UNION ALL
  SELECT '路由匹配', 'Route matching' UNION ALL
  SELECT '递归', 'Recursive implementation' UNION ALL
  SELECT '逻辑运算', 'Logical operations' UNION ALL
  SELECT '遍历', 'Traversal pattern'
) v
WHERE NOT EXISTS (
  SELECT 1 FROM `knowledge_point` kp WHERE kp.`name` = v.`name`
);

DELETE plp
FROM `path_level_problem` plp
INNER JOIN `problem` p ON p.`id` = plp.`problem_id`
WHERE plp.`level_id` = 9
  AND p.`title` = '单词频次查询';

UPDATE `path_level_problem`
SET `order_num` = 1,
    `update_time` = NOW()
WHERE `level_id` = 9;

UPDATE `path_level` pl
LEFT JOIN (
  SELECT `level_id`, GROUP_CONCAT(`problem_id` ORDER BY `order_num`) AS `problem_ids`
  FROM `path_level_problem`
  WHERE `level_id` = 9
  GROUP BY `level_id`
) bound ON bound.`level_id` = pl.`id`
SET pl.`problem_ids` = bound.`problem_ids`,
    pl.`update_time` = NOW()
WHERE pl.`id` = 9;

UPDATE `user`
SET `password` = '$2a$10$gTdYLCHUw/QLgFoqXRZFuOUt63DKRcxhKTDOnyaWXsattcfJsh1n6',
    `status` = 1,
    `update_time` = NOW()
WHERE `username` = 'admin'
  AND `role` = 1;
