#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试用例生成工具
为题目生成测试用例并输出SQL语句
"""

class TestCaseGenerator:
    def __init__(self):
        self.test_cases = []
    
    def add_test_case(self, problem_id, input_data, expected_output, is_sample=False, sort_order=1):
        """
        添加一个测试用例
        """
        self.test_cases.append({
            "problem_id": problem_id,
            "input": input_data,
            "output": expected_output,
            "is_sample": 1 if is_sample else 0,
            "sort_order": sort_order
        })
    
    def generate_for_sum_two_numbers(self, problem_id=1):
        """
        为两数之和生成测试用例
        """
        cases = [
            ("1 2", "3", True, 1),
            ("100 200", "300", False, 2),
            ("-5 10", "5", False, 3),
            ("0 0", "0", False, 4),
            ("9999 1", "10000", False, 5),
        ]
        for i, (input_data, output, is_sample, order) in enumerate(cases):
            self.add_test_case(problem_id, input_data, output, is_sample, order)
    
    def generate_for_find_max(self, problem_id=2):
        """
        为求最大值生成测试用例
        """
        cases = [
            ("5 3", "5", True, 1),
            ("3 5", "5", False, 2),
            ("10 10", "10", False, 3),
            ("-1 -5", "-1", False, 4),
            ("0 100", "100", False, 5),
        ]
        for i, (input_data, output, is_sample, order) in enumerate(cases):
            self.add_test_case(problem_id, input_data, output, is_sample, order)
    
    def generate_for_leap_year(self, problem_id=6):
        """
        为判断闰年生成测试用例
        """
        cases = [
            ("2024", "是闰年", True, 1),
            ("2000", "是闰年", False, 2),
            ("1900", "不是闰年", False, 3),
            ("2023", "不是闰年", False, 4),
            ("2024", "是闰年", False, 5),
        ]
        for i, (input_data, output, is_sample, order) in enumerate(cases):
            self.add_test_case(problem_id, input_data, output, is_sample, order)
    
    def generate_for_fibonacci(self, problem_id=7):
        """
        为斐波那契数列生成测试用例
        """
        cases = [
            ("5", "0 1 1 2 3", True, 1),
            ("1", "0", False, 2),
            ("2", "0 1", False, 3),
            ("10", "0 1 1 2 3 5 8 13 21 34", False, 4),
        ]
        for i, (input_data, output, is_sample, order) in enumerate(cases):
            self.add_test_case(problem_id, input_data, output, is_sample, order)
    
    def generate_for_string_reverse(self, problem_id=8):
        """
        为字符串反转生成测试用例
        """
        cases = [
            ("hello", "olleh", True, 1),
            ("abc", "cba", False, 2),
            ("12345", "54321", False, 3),
        ]
        for i, (input_data, output, is_sample, order) in enumerate(cases):
            self.add_test_case(problem_id, input_data, output, is_sample, order)
    
    def generate_for_bubble_sort(self, problem_id=11):
        """
        为冒泡排序生成测试用例
        """
        cases = [
            ("5\n3 1 4 2 5", "1 2 3 4 5", True, 1),
            ("5\n5 4 3 2 1", "1 2 3 4 5", False, 2),
            ("5\n1 2 3 4 5", "1 2 3 4 5", False, 3),
        ]
        for i, (input_data, output, is_sample, order) in enumerate(cases):
            self.add_test_case(problem_id, input_data, output, is_sample, order)
    
    def generate_for_absolute_value(self, problem_id=3):
        """
        为求绝对值生成测试用例
        """
        cases = [
            ("-5", "5", True, 1),
            ("10", "10", False, 2),
            ("0", "0", False, 3),
            ("-999", "999", False, 4),
        ]
        for i, (input_data, output, is_sample, order) in enumerate(cases):
            self.add_test_case(problem_id, input_data, output, is_sample, order)
    
    def generate_for_odd_even(self, problem_id=4):
        """
        为判断奇偶生成测试用例
        """
        cases = [
            ("7", "奇数", True, 1),
            ("4", "偶数", False, 2),
            ("0", "偶数", False, 3),
            ("-5", "奇数", False, 4),
        ]
        for i, (input_data, output, is_sample, order) in enumerate(cases):
            self.add_test_case(problem_id, input_data, output, is_sample, order)
    
    def generate_for_sum_1_to_n(self, problem_id=5):
        """
        为求和（1到n）生成测试用例
        """
        cases = [
            ("100", "5050", True, 1),
            ("1", "1", False, 2),
            ("5", "15", False, 3),
            ("10", "55", False, 4),
        ]
        for i, (input_data, output, is_sample, order) in enumerate(cases):
            self.add_test_case(problem_id, input_data, output, is_sample, order)
    
    def generate_for_string_length(self, problem_id=6):
        """
        为字符串长度生成测试用例
        """
        cases = [
            ("hello", "5", True, 1),
            ("", "0", False, 2),
            ("programming", "11", False, 3),
            ("12345", "5", False, 4),
        ]
        for i, (input_data, output, is_sample, order) in enumerate(cases):
            self.add_test_case(problem_id, input_data, output, is_sample, order)
    
    def generate_for_count_char(self, problem_id=8):
        """
        为统计字符个数生成测试用例
        """
        cases = [
            ("programming\nm", "2", True, 1),
            ("hello\nl", "2", False, 2),
            ("test\nt", "2", False, 3),
            ("abc\nd", "0", False, 4),
        ]
        for i, (input_data, output, is_sample, order) in enumerate(cases):
            self.add_test_case(problem_id, input_data, output, is_sample, order)
    
    def generate_for_array_sum(self, problem_id=9):
        """
        为数组求和生成测试用例
        """
        cases = [
            ("5\n1 2 3 4 5", "15", True, 1),
            ("3\n10 20 30", "60", False, 2),
            ("1\n5", "5", False, 3),
            ("4\n-1 -2 -3 -4", "-10", False, 4),
        ]
        for i, (input_data, output, is_sample, order) in enumerate(cases):
            self.add_test_case(problem_id, input_data, output, is_sample, order)
    
    def generate_for_array_max(self, problem_id=10):
        """
        为找数组最大值生成测试用例
        """
        cases = [
            ("5\n3 1 4 2 5", "5", True, 1),
            ("3\n10 5 20", "20", False, 2),
            ("1\n-5", "-5", False, 3),
            ("4\n-1 -2 -3 -4", "-1", False, 4),
        ]
        for i, (input_data, output, is_sample, order) in enumerate(cases):
            self.add_test_case(problem_id, input_data, output, is_sample, order)
    
    def to_sql(self, filename="test_cases.sql"):
        """
        输出为SQL文件
        """
        sql_lines = []
        sql_lines.append("-- 测试用例数据")
        sql_lines.append("-- 生成时间: " + __import__("datetime").datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
        sql_lines.append("")
        
        for tc in self.test_cases:
            sql = f"INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES ({tc['problem_id']}, '{tc['input']}', '{tc['output']}', {tc['is_sample']}, {tc['sort_order']});"
            sql_lines.append(sql)
        
        sql_content = "\n".join(sql_lines)
        
        with open(filename, "w", encoding="utf-8") as f:
            f.write(sql_content)
        
        print(f"✅ SQL文件已生成: {filename}")
        print(f"   共 {len(self.test_cases)} 个测试用例")
    
    def print_sql(self):
        """
        打印SQL语句到控制台
        """
        print("=" * 60)
        print("测试用例 SQL 语句")
        print("=" * 60)
        for tc in self.test_cases:
            sql = f"INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`) VALUES ({tc['problem_id']}, '{tc['input']}', '{tc['output']}', {tc['is_sample']}, {tc['sort_order']});"
            print(sql)
        print("=" * 60)
        print(f"共 {len(self.test_cases)} 个测试用例")


def main():
    print("=" * 60)
    print("测试用例生成工具")
    print("=" * 60)
    
    generator = TestCaseGenerator()
    
    print("\n📝 正在生成两数之和测试用例...")
    generator.generate_for_sum_two_numbers(1)
    
    print("📝 正在生成求最大值测试用例...")
    generator.generate_for_find_max(2)
    
    print("📝 正在生成求绝对值测试用例...")
    generator.generate_for_absolute_value(3)
    
    print("📝 正在生成判断奇偶测试用例...")
    generator.generate_for_odd_even(4)
    
    print("📝 正在生成求和（1到n）测试用例...")
    generator.generate_for_sum_1_to_n(5)
    
    print("📝 正在生成字符串长度测试用例...")
    generator.generate_for_string_length(6)
    
    print("📝 正在生成字符串反转测试用例...")
    generator.generate_for_string_reverse(7)
    
    print("📝 正在生成统计字符个数测试用例...")
    generator.generate_for_count_char(8)
    
    print("📝 正在生成数组求和测试用例...")
    generator.generate_for_array_sum(9)
    
    print("📝 正在生成找数组最大值测试用例...")
    generator.generate_for_array_max(10)
    
    print("📝 正在生成冒泡排序测试用例...")
    generator.generate_for_bubble_sort(11)
    
    timestamp = __import__("datetime").datetime.now().strftime("%Y%m%d_%H%M%S")
    
    print("\n💾 正在保存文件...")
    generator.to_sql(f"test_cases_{timestamp}.sql")
    
    print("\n📋 SQL预览:")
    generator.print_sql()
    
    print("\n🎉 完成！")
    print("\n💡 使用方法:")
    print("   1. 在MySQL中执行生成的SQL文件")
    print("   2. 或直接复制上面的SQL语句执行")


if __name__ == "__main__":
    main()
