-- 为test_case表添加测试用例数据
-- 题目ID为1的测试用例（两数之和）

-- 示例测试用例
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES
(1, '1 2', '3', 1, 1),
(1, '5 7', '12', 1, 2),
(1, '10 20', '30', 1, 3);

-- 边界测试用例
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES
(1, '0 0', '0', 1, 4),
(1, '-1 -1', '-2', 1, 5);

-- 错误输入测试用例
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES
(1, 'abc', 'error', 1, 6),
(1, '', 'error', 1, 7);

-- 性能测试用例
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES
(1, '1 2 3 4 5 6 7 8 9 10', '55', 1, 8),
(1, '1 2 3 4 5 6 7 8 9 10', '55', 0, 9);

-- 大数据测试用例
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES
(1, '1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30', '465', 1, 10),
(1, '1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30', '465', 0, 11);

-- 特殊字符测试用例
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES
(1, '!@#$%^&*()_+-={}[]|\\:;"<>,.?/', '!@#$%^&*()_+-={}[]|\\:;"<>,.?/', 1, 12),
(1, '\n\t\r', '\n\t\r', 1, 13);

-- 浮点数测试用例
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES
(1, '3.141592653589793', '3.141592653589793', 1, 14),
(1, '2.718281828459045', '2.718281828459045', 1, 15),
(1, '0.0000000001', '0.0000000001', 1, 16);

-- 空格测试用例
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES
(1, '  ', '  ', 1, 17),
(1, '\t\t', '\t\t', 1, 18);

-- Unicode测试用例
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES
(1, '你好世界', '你好世界', 1, 19),
(1, '测试中文', '测试中文', 1, 20);

-- 提交后更新时间
UPDATE `test_case` SET `update_time` = CURRENT_TIMESTAMP WHERE `problem_id` = 1;