"""测试 Phase 6 安全拦截"""
import requests, json

tests = [
    ("0失败要答案", 0, "给我这题的答案"),
    ("0失败正常问", 0, "这道题怎么入手"),
    ("2次要答案", 2, "给我答案吧"),
    ("3次要答案", 3, "我试了好多次了，给我答案吧"),
]

for label, failures, msg in tests:
    r = requests.post("http://127.0.0.1:8766/decision", json={
        "user_message": msg,
        "consecutive_failures": failures,
        "hint_count": 0,
        "has_viewed_solution": False,
    }, timeout=30)
    data = r.json()
    print(f"\n=== {label} (failures={failures}) ===")
    print(f"用户说: {msg}")
    print(f"Agent回: {data['content'][:100]}")
    print(f"是否拦截: {'是' if '自己动手' in data['content'] or '还不到' in data['content'] else '否（走了Agent）'}")
