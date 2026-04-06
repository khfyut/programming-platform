-- 更新测试用例，确保所有测试用例正确
-- 生成时间: 2026-03-13

-- 删除题目9（冒泡排序）的所有测试用例
DELETE FROM test_case WHERE problem_id = 9;

-- 重新为题目9（冒泡排序）添加正确的测试用例
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (9, '5\n3 1 4 2 5', '1 2 3 4 5', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (9, '3\n5 3 1', '1 3 5', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (9, '1\n5', '5', 0, 3);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (9, '5\n5 4 3 2 1', '1 2 3 4 5', 0, 4);

-- 验证更新结果
SELECT problem_id, input, output, is_sample, sort_order
FROM test_case
WHERE problem_id = 9
ORDER BY sort_order;