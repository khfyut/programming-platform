-- 重置所有测试用例
-- 生成时间: 2026-03-13

-- 清空整个test_case表
TRUNCATE TABLE test_case;

-- 为所有题目重新添加正确的测试用例

-- 题目1: 两数之和
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (1, '1 2', '3', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (1, '100 200', '300', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (1, '-5 10', '5', 0, 3);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (1, '0 0', '0', 0, 4);

-- 题目3: 三数之和
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (3, '1 2 3', '6', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (3, '10 20 30', '60', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (3, '-1 -2 -3', '-6', 0, 3);

-- 题目4: 四数之和
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (4, '1 2 3 4', '10', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (4, '10 20 30 40', '100', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (4, '-1 -2 -3 -4', '-10', 0, 3);

-- 题目5: 三数之和
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (5, '1 2 3', '6', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (5, '5 10 15', '30', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (5, '0 0 0', '0', 0, 3);

-- 题目6: 判断闰年
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (6, '2024', '是闰年', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (6, '2000', '是闰年', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (6, '1900', '不是闰年', 0, 3);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (6, '2023', '不是闰年', 0, 4);

-- 题目7: 斐波那契数列
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (7, '5', '0 1 1 2 3', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (7, '1', '0', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (7, '10', '0 1 1 2 3 5 8 13 21 34', 0, 3);

-- 题目8: 字符串反转
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (8, 'hello', 'olleh', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (8, 'abc', 'cba', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (8, '12345', '54321', 0, 3);

-- 题目9: 冒泡排序
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (9, '5\n3 1 4 2 5', '1 2 3 4 5', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (9, '3\n5 3 1', '1 3 5', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (9, '1\n5', '5', 0, 3);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (9, '5\n5 4 3 2 1', '1 2 3 4 5', 0, 4);

-- 题目11: 两数之和
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (11, '5 3', '8', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (11, '10 20', '30', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (11, '-5 -3', '-8', 0, 3);

-- 题目12: 求最大值
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (12, '5 3', '5', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (12, '3 5', '5', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (12, '10 10', '10', 0, 3);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (12, '-1 -5', '-1', 0, 4);

-- 题目13: 求绝对值
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (13, '-5', '5', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (13, '10', '10', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (13, '0', '0', 0, 3);

-- 题目14: 判断奇偶
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (14, '7', '奇数', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (14, '4', '偶数', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (14, '0', '偶数', 0, 3);

-- 题目15: 求和（1到n）
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (15, '100', '5050', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (15, '1', '1', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (15, '5', '15', 0, 3);

-- 题目16: 字符串长度
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (16, 'hello', '5', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (16, '', '0', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (16, 'programming', '11', 0, 3);

-- 题目17: 统计字符个数
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (17, 'programming\nm', '2', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (17, 'hello\nl', '2', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (17, 'test\nt', '2', 0, 3);

-- 题目18: 数组求和
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (18, '5\n1 2 3 4 5', '15', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (18, '3\n10 20 30', '60', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (18, '1\n5', '5', 0, 3);

-- 题目19: 找数组最大值
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (19, '5\n3 1 4 2 5', '5', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (19, '3\n10 5 20', '20', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (19, '1\n-5', '-5', 0, 3);

-- 验证结果
SELECT problem_id, COUNT(*) as test_case_count
FROM test_case
GROUP BY problem_id
ORDER BY problem_id;