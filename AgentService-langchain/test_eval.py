"""
Agent 评估集 —— 10 个用例覆盖 7 种教学策略。

每次改动 Agent 后跑一遍，看哪些用例挂了。
LLM 回复不稳定，所以用"特征检查"而非精确匹配。
"""
import asyncio, json
from app.main import infer_action, build_safety_rules, AgentRequest, ProblemInfo, SubmissionInfo


# ── 测试用例定义 ──────────────────────────────

class TestCase:
    def __init__(self, name, msg, failures, expected_action=None,
                 must_contain=None, must_not_contain=None,
                 has_code=False, has_solution=False):
        self.name = name
        self.msg = msg
        self.failures = failures
        self.expected_action = expected_action
        self.must_contain = must_contain or []
        self.must_not_contain = must_not_contain or []
        self.has_code = has_code
        self.has_viewed_solution = has_solution


CASES = [
    # 场景 1: 首次提问 → GUIDE_IDEA
    TestCase("首次提问", "这道题怎么做", 0,
             expected_action="GUIDE_IDEA",
             must_not_contain=["答案", "完整代码", "参考实现"]),

    # 场景 2: 直接要答案 + 无失败 → 拦截 GUIDE_IDEA
    TestCase("0次要答案", "给我这题的答案", 0,
             expected_action="GUIDE_IDEA",
             must_contain=["自己动手", "先试试"],
             must_not_contain=["完整代码"]),

    # 场景 3: 1-2次失败 → HINT
    TestCase("失败2次", "为什么不对", 2,
             expected_action="HINT",
             must_not_contain=["完整代码", "参考实现"]),

    # 场景 4: 3次失败 + 要答案 → 代码层拒绝
    TestCase("3次要答案", "求你了给我答案吧", 3,
             must_not_contain=["完整代码", "参考实现"]),

    # 场景 5: 有错误代码 → DIAGNOSE（LLM 版优先）
    TestCase("错误代码诊断", "我提交的代码错了", 1,
             has_code=True,
             expected_action="DIAGNOSE",
             must_contain=["行", "错"],  # 应指出具体哪行
             must_not_contain=["完整代码"]),

    # 场景 6: 概念问题 → EXPLAIN
    TestCase("概念讲解", "什么是哈希表", 0,
             expected_action="EXPLAIN"),

    # 场景 7: 搜题请求 → RECOMMEND
    TestCase("搜题请求", "有哪些数组题", 3,
             expected_action="RECOMMEND"),

    # 场景 8: 看过答案 → REFLECT
    TestCase("看过答案", "这道题我在想想", 0,
             has_solution=True,
             expected_action="REFLECT"),

    # 场景 9: >=3次失败 + 已诊断 → REVEAL_ANSWER
    TestCase("多次失败要答案", "我真的不会，给我答案", 4,
             expected_action="REVEAL_ANSWER"),

    # 场景 10: 闲聊 → 应简短友好
    TestCase("打招呼", "你好", 0,
             expected_action="EXPLAIN",
             must_not_contain=["完整代码"]),
]


# ── 运行评估 ──────────────────────────────────

def build_request(case: TestCase) -> AgentRequest:
    sub = None
    if case.has_code:
        sub = SubmissionInfo(
            status="WA",
            code='if (n / 2 == 0) return "偶数";',
            error_message="输入5时期望奇数，实际返回偶数"
        )
    return AgentRequest(
        user_message=case.msg,
        problem=ProblemInfo(title="判断奇偶数", difficulty="easy"),
        submission=sub,
        consecutive_failures=case.failures,
        hint_count=case.failures,
        has_viewed_solution=case.has_viewed_solution,
    )


def run_rule_check():
    """只测策略推断（不调 LLM，秒出结果）"""
    print("=" * 60)
    print("策略推断测试（关键词规则 + 硬规则）")
    print("=" * 60)

    passed = 0
    for c in CASES:
        req = build_request(c)
        action = infer_action(req)
        ok = c.expected_action is None or action == c.expected_action
        status = "✅" if ok else f"❌ (期望{c.expected_action} 实际{action})"
        if ok:
            passed += 1
        print(f"  {status} {c.name}: {c.msg[:30]} → {action}")

    print(f"\n结果: {passed}/{len(CASES)} 通过")


async def run_full_check():
    """调真实 Agent，检查回复特征"""
    from app.main import ask_agent, search_knowledge

    print("\n" + "=" * 60)
    print("回复特征测试（调真实 Agent，需服务在跑）")
    print("=" * 60)

    passed = 0
    for c in CASES:
        req = build_request(c)
        result = await ask_agent(user_message=c.msg, context_text="")

        content = result.get("content", "")
        all_ok = True

        # 检查必须包含的内容
        for keyword in c.must_contain:
            if keyword not in content:
                print(f"  ⚠️  {c.name}: 回复中未包含 '{keyword}'")
                all_ok = False

        # 检查不能包含的内容
        for keyword in c.must_not_contain:
            if keyword in content:
                print(f"  ❌ {c.name}: 回复中不应包含 '{keyword}'")
                all_ok = False

        if all_ok:
            passed += 1
            print(f"  ✅ {c.name}: 通过")

    print(f"\n结果: {passed}/{len(CASES)} 通过")


if __name__ == "__main__":
    run_rule_check()
    # 可以单独跑全量: asyncio.run(run_full_check())
