-- Normalize historical category-style problem languages into judge runtime languages.
-- `algorithm` and `database` were old content categories, not executable runtimes.

DELETE FROM `problem_supported_language`
WHERE LOWER(`language_code`) NOT IN (
  'java',
  'python',
  'cpp',
  'javascript',
  'typescript',
  'go'
);

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
  lang.`language_code`,
  CASE WHEN lang.`sort_order` = 0 THEN 1 ELSE 0 END AS `is_default`,
  lang.`starter_code`,
  lang.`starter_filename`,
  'ACTIVE' AS `status`,
  lang.`sort_order`,
  NOW() AS `create_time`,
  NOW() AS `update_time`
FROM `problem` p
JOIN (
  SELECT
    'java' AS `language_code`,
    'public class Main {\n    public static void main(String[] args) throws Exception {\n        \n    }\n}\n' AS `starter_code`,
    'Main.java' AS `starter_filename`,
    0 AS `sort_order`
  UNION ALL
  SELECT
    'python',
    'import sys\n\n\ndef main():\n    pass\n\n\nif __name__ == "__main__":\n    main()\n',
    'main.py',
    1
  UNION ALL
  SELECT
    'cpp',
    '#include <iostream>\nusing namespace std;\n\nint main() {\n    return 0;\n}\n',
    'main.cpp',
    2
  UNION ALL
  SELECT
    'javascript',
    'function main() {\n}\n\nmain();\n',
    'main.js',
    3
  UNION ALL
  SELECT
    'typescript',
    'function main(): void {\n}\n\nmain();\n',
    'main.ts',
    4
  UNION ALL
  SELECT
    'go',
    'package main\n\nfunc main() {\n}\n',
    'main.go',
    5
) lang
WHERE LOWER(COALESCE(p.`language`, '')) NOT IN (
    'java',
    'python',
    'cpp',
    'javascript',
    'typescript',
    'go'
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `problem_supported_language` existing
    WHERE existing.`problem_id` = p.`id`
      AND LOWER(existing.`language_code`) = lang.`language_code`
  );

UPDATE `problem`
SET
  `language` = 'java',
  `update_time` = NOW()
WHERE LOWER(COALESCE(`language`, '')) NOT IN (
  'java',
  'python',
  'cpp',
  'javascript',
  'typescript',
  'go'
);
