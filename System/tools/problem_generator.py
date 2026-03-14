#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
题目生成工具
自动生成编程题目并保存为Excel/CSV格式
"""

import pandas as pd
import json
from datetime import datetime

class ProblemGenerator:
    def __init__(self):
        self.problems = []
    
    def add_problem(self, title, content, input_example, output_example, 
                   difficulty, language, time_limit=1000, memory_limit=256,
                   tags="", knowledge_points="", hints=None, sample_explanation=""):
        """
        添加一道题目
        """
        if hints is None:
            hints = []
        
        problem = {
            "标题": title,
            "内容": content,
            "输入示例": input_example,
            "输出示例": output_example,
            "难度": difficulty,
            "语言": language,
            "时间限制(ms)": time_limit,
            "内存限制(MB)": memory_limit,
            "标签": tags,
            "知识点": knowledge_points,
            "提示": json.dumps(hints, ensure_ascii=False),
            "示例解释": sample_explanation
        }
        self.problems.append(problem)
    
    def generate_basic_math_problems(self):
        """
        生成基础数学题目
        """
        problems_data = [
            {
                "title": "两数之和",
                "content": "给定两个整数a和b，计算它们的和并输出。",
                "input_example": "1 2",
                "output_example": "3",
                "difficulty": 0,
                "tags": "数组,基础算法",
                "knowledge_points": "基础运算,输入输出",
                "hints": [{"step": 1, "content": "使用Scanner读取输入"}],
                "sample_explanation": "输入两个整数，用空格分隔，输出它们的和"
            },
            {
                "title": "求最大值",
                "content": "给定两个整数a和b，输出较大的那个数。",
                "input_example": "5 3",
                "output_example": "5",
                "difficulty": 0,
                "tags": "条件判断,基础算法",
                "knowledge_points": "条件判断,比较运算",
                "hints": [{"step": 1, "content": "使用if-else语句比较大小"}],
                "sample_explanation": "比较a和b的大小，输出较大的那个数"
            },
            {
                "title": "求绝对值",
                "content": "给定一个整数，输出它的绝对值。",
                "input_example": "-5",
                "output_example": "5",
                "difficulty": 0,
                "tags": "基础算法,数学",
                "knowledge_points": "条件判断,数学运算",
                "hints": [{"step": 1, "content": "如果是负数，乘以-1"}],
                "sample_explanation": "-5的绝对值是5"
            },
            {
                "title": "判断奇偶",
                "content": "给定一个整数，判断它是奇数还是偶数。",
                "input_example": "7",
                "output_example": "奇数",
                "difficulty": 0,
                "tags": "条件判断,数学",
                "knowledge_points": "取模运算,条件判断",
                "hints": [{"step": 1, "content": "使用%2判断是否能被2整除"}],
                "sample_explanation": "7除以2余1，所以是奇数"
            },
            {
                "title": "求和（1到n）",
                "content": "给定正整数n，计算1+2+...+n的和。",
                "input_example": "100",
                "output_example": "5050",
                "difficulty": 0,
                "tags": "循环,数学",
                "knowledge_points": "循环,累加",
                "hints": [{"step": 1, "content": "使用for循环从1累加到n"}, {"step": 2, "content": "或者使用公式n*(n+1)/2"}],
                "sample_explanation": "1+2+...+100=5050"
            }
        ]
        
        for p in problems_data:
            self.add_problem(
                title=p["title"],
                content=p["content"],
                input_example=p["input_example"],
                output_example=p["output_example"],
                difficulty=p["difficulty"],
                language="java",
                tags=p["tags"],
                knowledge_points=p["knowledge_points"],
                hints=p["hints"],
                sample_explanation=p["sample_explanation"]
            )
    
    def generate_string_problems(self):
        """
        生成字符串处理题目
        """
        problems_data = [
            {
                "title": "字符串长度",
                "content": "给定一个字符串，输出它的长度。",
                "input_example": "hello",
                "output_example": "5",
                "difficulty": 0,
                "tags": "字符串,基础算法",
                "knowledge_points": "字符串操作",
                "hints": [{"step": 1, "content": "使用String的length()方法"}],
                "sample_explanation": "\"hello\"的长度是5"
            },
            {
                "title": "字符串反转",
                "content": "给定一个字符串，输出其反转后的结果。",
                "input_example": "hello",
                "output_example": "olleh",
                "difficulty": 1,
                "tags": "字符串,基础算法",
                "knowledge_points": "字符串操作,循环",
                "hints": [{"step": 1, "content": "从后往前遍历字符串"}],
                "sample_explanation": "\"hello\"反转后是\"olleh\""
            },
            {
                "title": "统计字符个数",
                "content": "给定一个字符串和一个字符，统计该字符在字符串中出现的次数。",
                "input_example": "programming\nm",
                "output_example": "2",
                "difficulty": 1,
                "tags": "字符串,循环",
                "knowledge_points": "字符串遍历,计数",
                "hints": [{"step": 1, "content": "遍历字符串，逐个比较"}],
                "sample_explanation": "\"programming\"中有2个'm'"
            }
        ]
        
        for p in problems_data:
            self.add_problem(
                title=p["title"],
                content=p["content"],
                input_example=p["input_example"],
                output_example=p["output_example"],
                difficulty=p["difficulty"],
                language="java",
                tags=p["tags"],
                knowledge_points=p["knowledge_points"],
                hints=p["hints"],
                sample_explanation=p["sample_explanation"]
            )
    
    def generate_array_problems(self):
        """
        生成数组题目
        """
        problems_data = [
            {
                "title": "数组求和",
                "content": "给定n个整数，求它们的和。",
                "input_example": "5\n1 2 3 4 5",
                "output_example": "15",
                "difficulty": 0,
                "tags": "数组,循环",
                "knowledge_points": "数组操作,累加",
                "hints": [{"step": 1, "content": "先读取n，再读取n个整数"}],
                "sample_explanation": "1+2+3+4+5=15"
            },
            {
                "title": "找数组最大值",
                "content": "给定n个整数，找出其中的最大值。",
                "input_example": "5\n3 1 4 2 5",
                "output_example": "5",
                "difficulty": 0,
                "tags": "数组,循环",
                "knowledge_points": "数组遍历,比较",
                "hints": [{"step": 1, "content": "假设第一个数是最大值，然后逐个比较"}],
                "sample_explanation": "最大值是5"
            },
            {
                "title": "冒泡排序",
                "content": "输入n个整数，用冒泡排序算法将其从小到大排序后输出。",
                "input_example": "5\n3 1 4 2 5",
                "output_example": "1 2 3 4 5",
                "difficulty": 2,
                "tags": "排序,数组",
                "knowledge_points": "冒泡排序,双重循环",
                "hints": [{"step": 1, "content": "外层循环控制轮数"}, {"step": 2, "content": "内层循环两两比较交换"}],
                "sample_explanation": "排序后结果是1 2 3 4 5"
            }
        ]
        
        for p in problems_data:
            self.add_problem(
                title=p["title"],
                content=p["content"],
                input_example=p["input_example"],
                output_example=p["output_example"],
                difficulty=p["difficulty"],
                language="java",
                tags=p["tags"],
                knowledge_points=p["knowledge_points"],
                hints=p["hints"],
                sample_explanation=p["sample_explanation"]
            )
    
    def to_excel(self, filename="problems.xlsx"):
        """
        保存为Excel文件
        """
        df = pd.DataFrame(self.problems)
        df.to_excel(filename, index=False, engine="openpyxl")
        print(f"✅ Excel文件已生成: {filename}")
        print(f"   共 {len(self.problems)} 道题目")
    
    def to_csv(self, filename="problems.csv"):
        """
        保存为CSV文件
        """
        df = pd.DataFrame(self.problems)
        df.to_csv(filename, index=False, encoding="utf-8-sig")
        print(f"✅ CSV文件已生成: {filename}")
        print(f"   共 {len(self.problems)} 道题目")


def main():
    print("=" * 50)
    print("题目生成工具")
    print("=" * 50)
    
    generator = ProblemGenerator()
    
    print("\n📝 正在生成基础数学题目...")
    generator.generate_basic_math_problems()
    
    print("📝 正在生成字符串处理题目...")
    generator.generate_string_problems()
    
    print("📝 正在生成数组题目...")
    generator.generate_array_problems()
    
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    
    print("\n💾 正在保存文件...")
    generator.to_excel(f"problems_{timestamp}.xlsx")
    generator.to_csv(f"problems_{timestamp}.csv")
    
    print("\n🎉 完成！")
    print(f"   共生成 {len(generator.problems)} 道题目")


if __name__ == "__main__":
    try:
        main()
    except ImportError as e:
        print("❌ 缺少依赖库，请先安装:")
        print("   pip install pandas openpyxl")
    except Exception as e:
        print(f"❌ 发生错误: {e}")
