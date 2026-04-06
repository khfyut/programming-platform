-- 测试用例数据
-- 生成时间: 2026-03-13 17:32:34

INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (1, '1 2', '3', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (1, '100 200', '300', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (1, '-5 10', '5', 0, 3);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (1, '0 0', '0', 0, 4);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (1, '9999 1', '10000', 0, 5);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (2, '5 3', '5', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (2, '3 5', '5', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (2, '10 10', '10', 0, 3);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (2, '-1 -5', '-1', 0, 4);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (2, '0 100', '100', 0, 5);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (3, '-5', '5', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (3, '10', '10', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (3, '0', '0', 0, 3);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (3, '-999', '999', 0, 4);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (4, '7', '奇数', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (4, '4', '偶数', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (4, '0', '偶数', 0, 3);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (4, '-5', '奇数', 0, 4);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (5, '100', '5050', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (5, '1', '1', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (5, '5', '15', 0, 3);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (5, '10', '55', 0, 4);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (6, 'hello', '5', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (6, '', '0', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (6, 'programming', '11', 0, 3);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (6, '12345', '5', 0, 4);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (7, 'hello', 'olleh', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (7, 'abc', 'cba', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (7, '12345', '54321', 0, 3);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (8, 'programming
m', '2', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (8, 'hello
l', '2', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (8, 'test
t', '2', 0, 3);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (8, 'abc
d', '0', 0, 4);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (9, '5
1 2 3 4 5', '15', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (9, '3
10 20 30', '60', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (9, '1
5', '5', 0, 3);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (9, '4
-1 -2 -3 -4', '-10', 0, 4);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (10, '5
3 1 4 2 5', '5', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (10, '3
10 5 20', '20', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (10, '1
-5', '-5', 0, 3);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (10, '4
-1 -2 -3 -4', '-1', 0, 4);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (11, '5
3 1 4 2 5', '1 2 3 4 5', 1, 1);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (11, '5
5 4 3 2 1', '1 2 3 4 5', 0, 2);
INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES (11, '5
1 2 3 4 5', '1 2 3 4 5', 0, 3);