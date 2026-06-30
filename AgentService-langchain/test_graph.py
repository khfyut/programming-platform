"""测试 LangGraph 工作流"""
import asyncio
import json
from app.graph import run_graph


async def test():
    # 测试1：搜题请求（应走 TOOL_REQUEST → generate_response 分支）
    print("=== 测试1：搜数组题 ===")
    r = await run_graph("哈希表怎么用", consecutive_failures=3)
    print(json.dumps(r, ensure_ascii=False, indent=2))

    # 测试2：教学请求（应走 TEACHING → safety_check → generate_response 分支）
    print("\n=== 测试2：正常提问 ===")
    r = await run_graph("这道题怎么做", consecutive_failures=0)
    print(json.dumps(r, ensure_ascii=False, indent=2))

    # 测试3：闲聊（应走 CHAT → generate_response 分支）
    print("\n=== 测试3：打招呼 ===")
    r = await run_graph("你好")
    print(json.dumps(r, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    asyncio.run(test())
