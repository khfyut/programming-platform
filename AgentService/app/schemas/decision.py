from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


class ProposedStateUpdate(BaseModel):
    update_type: str
    value: str
    confidence: float = 0.5
    reason: str


class CandidateAction(BaseModel):
    action_id: str
    action_type: str
    title: str
    description: str
    priority: str
    content: str
    required_conditions: list[str] = Field(default_factory=list)


class AgentDecision(BaseModel):
    response_id: str
    timestamp: datetime
    decision_summary: str
    selected_strategy: str
    decision_reason: str
    applied_constraints: list[str] = Field(default_factory=list)
    blocked_actions: list[str] = Field(default_factory=list)
    candidate_actions: list[CandidateAction] = Field(default_factory=list)
    recommended_action_id: Optional[str] = None
    action_type: Optional[str] = None
    pedagogical_goal: Optional[str] = None
    content_type: str
    main_response: str
    next_suggestion: Optional[str] = None
    error_tag: Optional[str] = None
    weak_points: list[str] = Field(default_factory=list)
    confidence: float = 0.65
    used_knowledge_refs: list[str] = Field(default_factory=list)
    content: str
    suggested_next_action: Optional[str] = None
    proposed_updates: list[ProposedStateUpdate] = Field(default_factory=list)
