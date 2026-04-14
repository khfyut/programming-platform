from datetime import datetime
from typing import Literal, Optional

from pydantic import BaseModel, Field


TriggerSource = Literal[
    "PROBLEM_PAGE_CHAT",
    "RUN_RESULT",
    "SUBMISSION_RESULT",
    "WRONG_BOOK_ENTRY",
    "LEARNING_PATH_ENTRY",
    "MANUAL_HELP_REQUEST",
]

UserIntent = Literal[
    "ASK_PROBLEM_SOLVING_IDEA",
    "ASK_FOR_HINT",
    "ASK_FOR_EXPLANATION",
    "ASK_FOR_FULL_SOLUTION",
    "ASK_FOR_CODE_REVIEW",
    "ASK_FOR_ERROR_ANALYSIS",
    "ASK_FOR_NEXT_STEP",
    "GENERAL_CHAT",
    "UNKNOWN",
]


class SessionContext(BaseModel):
    session_id: str
    trigger_reason: str
    user_explicit_request: Optional[str] = None
    last_action_id: Optional[str] = None


class ProblemContext(BaseModel):
    problem_id: int
    title: str
    difficulty: str = "unknown"
    knowledge_points: list[str] = Field(default_factory=list)
    problem_content: str = ""
    hints: str = ""
    sample_explanation: str = ""
    tags: str = ""
    language: str = "java"
    hint_shown_count: int = 0
    diagnosed_count: int = 0


class SubmissionContext(BaseModel):
    submit_id: int = 0
    status: str = "NONE"
    error_message: Optional[str] = None
    failed_test_cases: list[str] = Field(default_factory=list)
    code_content: Optional[str] = None
    execution_output: Optional[str] = None
    is_first_attempt: bool = True


class LearnerState(BaseModel):
    user_id: Optional[int] = None
    current_path_node: Optional[str] = None
    knowledge_mastery: dict[str, float] = Field(default_factory=dict)
    recent_error_pattern: Optional[str] = None


class AgentContext(BaseModel):
    request_id: str
    timestamp: datetime
    problem: ProblemContext
    submission: Optional[SubmissionContext] = None

    trigger_source: TriggerSource = "SUBMISSION_RESULT"
    user_intent: UserIntent = "UNKNOWN"
    user_message: Optional[str] = None
    requested_full_solution: bool = False

    consecutive_failures: int = 0
    has_viewed_solution: bool = False
    hint_count: int = 0
    diagnose_count: int = 0
    explain_count: int = 0
    recommend_count: int = 0
    reflect_count: int = 0
    last_action_type: Optional[str] = None
    last_goal: Optional[str] = None
    last_guidance_type: Optional[str] = None
    last_error_tag: Optional[str] = None
    weak_points: list[str] = Field(default_factory=list)
    learning_stage: str = "FIRST_TRY"
