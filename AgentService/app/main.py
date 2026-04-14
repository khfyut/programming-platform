from datetime import datetime

from fastapi import FastAPI

from app.core.content import CONTENT_TYPE_BY_ACTION, ContentGenerator, PedagogicalGoalBuilder
from app.core.policy import ConstraintFilter, IntentRouter, StrategySelector
from app.schemas.context import AgentContext
from app.schemas.decision import AgentDecision, CandidateAction

app = FastAPI(title="Learning Agent Service")


@app.post("/decision", response_model=AgentDecision)
def make_decision(context: AgentContext):
    intent = IntentRouter().detect(context)
    available_actions, blocked_actions, applied_constraints = ConstraintFilter().check(context, intent)
    action_type = StrategySelector().select(context, intent, available_actions)

    content = ContentGenerator().generate(context, action_type, intent)
    pedagogical_goal = PedagogicalGoalBuilder().build(action_type)
    content_type = CONTENT_TYPE_BY_ACTION[action_type]

    candidate_actions = [
        CandidateAction(
            action_id=f"{action_type.lower()}_001",
            action_type=action_type,
            title=_action_title(action_type),
            description=_action_description(action_type),
            priority="high",
            content=content.main_response,
            required_conditions=_required_conditions(action_type),
        )
    ]

    for candidate in available_actions:
        if candidate == action_type:
            continue
        candidate_actions.append(
            CandidateAction(
                action_id=f"{candidate.lower()}_001",
                action_type=candidate,
                title=_action_title(candidate),
                description=_action_description(candidate),
                priority="medium",
                content="可作为后续教学动作。",
                required_conditions=_required_conditions(candidate),
            )
        )

    return AgentDecision(
        response_id=context.request_id,
        timestamp=datetime.now(),
        decision_summary=f"动作: {action_type}, 意图: {intent}",
        selected_strategy=action_type,
        decision_reason=_decision_reason(context, intent, action_type),
        applied_constraints=applied_constraints,
        blocked_actions=blocked_actions,
        candidate_actions=candidate_actions,
        recommended_action_id=f"{action_type.lower()}_001",
        action_type=action_type,
        pedagogical_goal=pedagogical_goal,
        content_type=content_type,
        main_response=content.main_response,
        next_suggestion=content.next_suggestion,
        error_tag=content.error_tag,
        weak_points=content.weak_points,
        confidence=content.confidence,
        used_knowledge_refs=content.used_knowledge_refs,
        content=content.main_response,
        suggested_next_action=content.next_suggestion,
    )


def _decision_reason(context: AgentContext, intent: str, action_type: str) -> str:
    return (
        f"trigger_source={context.trigger_source}, user_intent={intent}, "
        f"user_message={context.user_message!r}, requested_full_solution={context.requested_full_solution}, "
        f"consecutive_failures={context.consecutive_failures}, "
        f"hint_count={context.hint_count}, diagnose_count={context.diagnose_count}, "
        f"explain_count={context.explain_count}, recommend_count={context.recommend_count}, "
        f"reflect_count={context.reflect_count}, has_viewed_solution={context.has_viewed_solution}, "
        f"last_action_type={context.last_action_type}, last_goal={context.last_goal}, action={action_type}"
    )


def _required_conditions(action_type: str) -> list[str]:
    if action_type == "REVEAL_ANSWER":
        return ["HAS_TRUSTED_FAILURE", "USER_REQUESTED_FULL_SOLUTION"]
    if action_type == "DIAGNOSE":
        return ["MULTIPLE_FAILURES_OR_DETAILED_ERROR"]
    return []


def _action_title(action_type: str) -> str:
    return {
        "GUIDE_IDEA": "解题思路引导",
        "HINT": "轻提示",
        "DIAGNOSE": "错误诊断",
        "EXPLAIN": "知识点讲解",
        "RECOMMEND": "下一步推荐",
        "REFLECT": "答案后复盘",
        "REVEAL_ANSWER": "完整答案",
    }[action_type]


def _action_description(action_type: str) -> str:
    return {
        "GUIDE_IDEA": "在不直接给代码的前提下引导起步",
        "HINT": "只给下一步提示",
        "DIAGNOSE": "定位错误原因和薄弱点",
        "EXPLAIN": "讲解当前卡点背后的知识",
        "RECOMMEND": "推荐后续练习或复习方向",
        "REFLECT": "看过答案后复盘学习收获",
        "REVEAL_ANSWER": "在约束允许后释放完整答案",
    }[action_type]
