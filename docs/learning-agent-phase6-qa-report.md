# Learning Agent Phase 6 QA Report

## Summary
- Phase 6 implemented the Agent feedback loop and observability persistence on the Java side.
- Added `POST /api/agent/feedback` for six fixed feedback types: `accepted`, `ignored`, `asked_followup`, `revealed_solution`, `applied_draft`, and `solved_after_action`.
- Feedback is written to the new `agent_feedback_log` table and is validated against the current user's `learning_event_log` entry by `request_id`.
- Feedback recording does not update `learning_stage`; Java remains the only trusted state and permission boundary.
- The problem coach frontend now reports non-blocking feedback events for follow-up, answer reveal, draft application, and recommendation acceptance.

## Implementation Coverage
- Added `AgentFeedbackRequestDTO`, `AgentFeedbackLog`, `AgentFeedbackLogMapper`, `AgentFeedbackService`, and the `/api/agent/feedback` controller endpoint.
- Added Flyway migration `V13__add_agent_feedback_log.sql` with indexes for `request_id` and `user_id + problem_id`.
- Extended `LearningEventLogMapper` with `findByRequestIdAndUser` so feedback validation is tied to the authenticated user.
- Added frontend `sendAgentFeedback` API helper and wired telemetry in `ProblemCoachSidebar.vue`.
- Kept Phase 5 RAG behavior content-only; no RAG signal is used for action selection, answer release, or state persistence.

## Automated Verification
- `System`: `.\mvn-local.cmd -q -DskipTests compile`
  - Result: passed.
- `System`: `.\mvn-local.cmd -q '-Dtest=AgentDtoJsonContractTest,AgentContextBuilderTest,AgentDecisionEnforcerTest,AgentFeedbackServiceTest,ProblemCoachAccessPolicyTest,ProblemCoachRecommendationServiceTest' test`
  - Result: passed.
- `AgentService`: `.\venv\Scripts\python.exe -m unittest discover -s tests -v`
  - Result: passed, 5 tests.
- `AgentService`: `.\venv\Scripts\python.exe -m compileall -q app`
  - Result: passed.
- `frontend`: `npm run build`
  - Result: passed.
  - Note: Vite still reports existing large chunk warnings for bundled dependencies such as Element Plus and Monaco.
- `frontend`: `npm run lint`
  - Result: passed with exit code 0.
  - Note: existing lint baseline remains noisy: 2325 warnings, 0 errors.

## Interface Verification
- Valid feedback types are accepted and persisted.
- Invalid `feedback_type` values are rejected.
- Cross-user `request_id` feedback is rejected because lookup is scoped by current `user_id`.
- Cross-problem feedback is rejected when the request `problem_id` does not match the logged Agent event.
- Cross-submit feedback is rejected when both sides provide `submit_id` and the IDs differ.
- Feedback writing has no dependency on `UserProblemInteraction` state updates, so `learning_stage` is not modified by this API.

## Frontend Telemetry Verification
- Continuing a follow-up sends `asked_followup` when a previous Agent `request_id` and `action_type` exist.
- Requesting a solution sends `revealed_solution` before the solution request.
- Applying a draft sends `applied_draft` before emitting the draft action.
- Opening an accepted recommendation sends `accepted`.
- Feedback failures are caught and logged to the console; they do not block the coach interaction.

## Phase 5 Regression
- Two Sum idea guidance still returns `GUIDE_IDEA / guidance / GUIDE_INITIAL_THINKING` without full code.
- First failed submission still returns `HINT / hint / GIVE_LIGHT_HINT` with problem knowledge refs.
- Multi-failure diagnosis still returns `DIAGNOSE / diagnosis / DIAGNOSE_ERROR_CAUSE`.
- Explanation requests still use teaching-template knowledge refs such as `template:teaching:hashmap`.
- Python `compileall` confirms the Agent service remains importable after the Phase 6 backend/frontend changes.

## Known Risks And Follow-Ups
- `decision_latency`, `fallback_used`, and `constraint_blocked` were not expanded in this pass. Existing event logs already carry `request_id`, trigger, intent, action, goal, content type, execution status, block reason, and decision reason; latency metrics can be added as a later observability pass.
- Frontend lint has a large pre-existing warning baseline. This phase keeps the acceptance gate at 0 errors, matching the implementation plan.
- No browser-driven manual QA was run in this pass; validation is currently automated command-level QA plus unit coverage.

## Phase Gate
- Phase 6 is cleared to proceed to Phase 7.
- Recommended next focus: connect `REFLECT` to the wrong-book workflow while preserving the same feedback and observability conventions.
