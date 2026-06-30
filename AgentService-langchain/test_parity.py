"""测试 Agent 分级教学 —— 判断奇偶数"""
import requests, json

# 一道"判断奇偶"题目的错误答案（常见错误：% 写成 / 了）
WRONG_CODE = """
public class Solution {
    public String check(int n) {
        if (n / 2 == 0) {        // 错误！应该是 n % 2
            return "偶数";
        }
        return "奇数";
    }
}
"""

ERROR_MSG = "输入 5 时期望 '奇数'，实际返回 '偶数'"

cases = [
    # 场景1：首次失败 → 应 GUIDE_IDEA（引导思路，不给具体提示）
    ("首次失败", 0, "这题怎么做"),
    # 场景2：失败2次 → 应 HINT（给提示）
    ("失败2次", 2, "为什么我的代码不对"),
    # 场景3：失败3次+代码 → 应 DIAGNOSE（诊断错误）
    ("失败3次带代码", 3, f"我提交的代码是：\n{WRONG_CODE}\n错误信息：{ERROR_MSG}"),
    # 场景4：失败4次要答案 → 应 EXPLAIN（讲解概念）
    ("失败4次要答案", 4, "求你了，给我答案吧"),
]

for label, failures, msg in cases:
    r = requests.post("http://127.0.0.1:8766/decision", json={
        "user_message": msg,
        "problem": {"title": "判断奇偶数", "description": "输入一个整数 n，判断它是奇数还是偶数。", "difficulty": "easy"},
        "consecutive_failures": failures,
        "hint_count": failures,
        "has_viewed_solution": False,
    }, timeout=60)
    data = r.json()
    action = data.get("action_type", "?")
    print(f"\n{'='*60}")
    print(f"【{label}】→ Agent 选择策略: {action}")
    print(f"用户说: {msg[:50]}")
    print(f"Agent回: {data.get('main_response', '')[:200]}")
    print(f"scope: {data.get('answer_scope')}")
