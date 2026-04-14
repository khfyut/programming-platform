-- Follow-up quality fix for Java-first Agent path bindings after V6 was applied.
-- V6 intentionally did not delete historical algorithm/backend levels, so this patch
-- fills their missing level knowledge points and fixes the database-application level.

UPDATE `problem`
SET `knowledge_points` = CASE `title`
  WHEN '判断闰年' THEN '控制流,条件判断,逻辑运算,取模运算'
  WHEN '判断奇偶' THEN '控制流,取模运算,条件判断'
  WHEN '词频统计' THEN 'Map,哈希表,词频统计,字符串'
  ELSE `knowledge_points`
END,
`update_time` = NOW()
WHERE `title` IN ('判断闰年', '判断奇偶', '词频统计');

UPDATE `path_level`
SET `knowledge_points` = CASE `id`
  WHEN 23 THEN '排序算法,搜索算法'
  WHEN 24 THEN '排序算法,贪心'
  WHEN 25 THEN '搜索算法,图'
  WHEN 26 THEN '数组,链表'
  WHEN 27 THEN '栈,队列'
  WHEN 28 THEN '哈希表,数组'
  WHEN 29 THEN '数组,栈'
  WHEN 30 THEN '链表,哈希表'
  WHEN 32 THEN '图'
  WHEN 33 THEN '堆'
  WHEN 34 THEN '动态规划'
  WHEN 35 THEN '贪心'
  WHEN 36 THEN '回溯'
  WHEN 37 THEN '动态规划,贪心'
  WHEN 38 THEN '二叉树,图'
  WHEN 39 THEN 'HTTP协议'
  WHEN 40 THEN 'TCP/IP'
  WHEN 41 THEN 'RESTful'
  WHEN 44 THEN 'ORM'
  WHEN 45 THEN 'RESTful'
  WHEN 46 THEN 'SQL,数据库设计'
  WHEN 48 THEN 'Spring MVC,RESTful'
  WHEN 49 THEN '数据库设计,ORM'
  ELSE `knowledge_points`
END,
`update_time` = NOW()
WHERE `id` IN (23,24,25,26,27,28,29,30,32,33,34,35,36,37,38,39,40,41,44,45,46,48,49);

DELETE FROM `path_level_problem`
WHERE `level_id` = 46;

INSERT INTO `path_level_problem` (`path_id`, `chapter_id`, `level_id`, `problem_id`, `order_num`, `create_time`, `update_time`)
SELECT pc.`path_id`, pl.`chapter_id`, pl.`id`, p.`id`, v.`order_num`, NOW(), NOW()
FROM (
  SELECT 'SQL条件语句生成' AS `title`, 1 AS `order_num`
  UNION ALL
  SELECT '表字段规范检查', 2
) v
INNER JOIN `path_level` pl ON pl.`id` = 46
INNER JOIN `path_chapter` pc ON pc.`id` = pl.`chapter_id`
INNER JOIN `problem` p ON p.`title` = v.`title`
WHERE COALESCE(p.`status`, 'PUBLISHED') = 'PUBLISHED'
  AND NOT EXISTS (
    SELECT 1 FROM `path_level_problem` existing
    WHERE existing.`level_id` = pl.`id` AND existing.`problem_id` = p.`id`
  );

UPDATE `path_level` pl
LEFT JOIN (
  SELECT `level_id`, GROUP_CONCAT(`problem_id` ORDER BY `order_num`) AS `problem_ids`
  FROM `path_level_problem`
  WHERE `level_id` = 46
  GROUP BY `level_id`
) bound ON bound.`level_id` = pl.`id`
SET pl.`problem_ids` = bound.`problem_ids`,
    pl.`update_time` = NOW()
WHERE pl.`id` = 46;
