INSERT INTO `problem_supported_language` (
  `problem_id`,
  `language_code`,
  `is_default`,
  `starter_code`,
  `starter_filename`,
  `status`,
  `sort_order`,
  `create_time`,
  `update_time`
)
SELECT
  p.`id`,
  LOWER(TRIM(p.`language`)) AS `language_code`,
  1 AS `is_default`,
  NULL AS `starter_code`,
  NULL AS `starter_filename`,
  'ACTIVE' AS `status`,
  0 AS `sort_order`,
  NOW() AS `create_time`,
  NOW() AS `update_time`
FROM `problem` p
WHERE p.`language` IS NOT NULL
  AND TRIM(p.`language`) <> ''
  AND NOT EXISTS (
    SELECT 1
    FROM `problem_supported_language` psl
    WHERE psl.`problem_id` = p.`id`
  );
