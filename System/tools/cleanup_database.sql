-- 清空测试用例表并删除重复题目

-- 1. 清空测试用例表
DELETE FROM test_case;

-- 2. 删除重复题目（保留最小id的记录）
DELETE FROM problem
WHERE id NOT IN (
    SELECT MIN(id) as min_id
    FROM problem
    GROUP BY title
);