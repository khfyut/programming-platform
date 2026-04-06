-- 分析数据库状态

-- 1. 查询重复题目
SELECT title, COUNT(*) as count
FROM problem
GROUP BY title
HAVING COUNT(*) > 1;

-- 2. 查看重复题目的详细信息
SELECT id, title, content, input, output, difficulty, language
FROM problem
WHERE title IN (
    SELECT title
    FROM problem
    GROUP BY title
    HAVING COUNT(*) > 1
)
ORDER BY title, id;

-- 3. 统计现有测试用例数量
SELECT COUNT(*) as total_test_cases
FROM test_case;

-- 4. 统计每个题目的测试用例数量
SELECT problem_id, COUNT(*) as test_case_count
FROM test_case
GROUP BY problem_id
ORDER BY problem_id;