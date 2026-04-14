# Learning Agent Phase 7 QA Report

## Summary

Phase 7 extends the unified Learning Agent chain beyond the problem page:

- wrong-book detail entry calls Java Agent API and routes to Python `REFLECT`
- learning-path level entry calls Java Agent API and routes to Python `RECOMMEND`
- path/wrong-book events write scoped event metadata
- feedback validation understands non-problem entry scope
- learning analysis page reads a Java-managed Agent event summary

## Implementation Notes

- Added `POST /api/agent/wrong-book/{wrongItemId}/reflect`
- Added `POST /api/agent/learning-path/{pathId}/level/{levelId}/recommend`
- Added `GET /api/agent/report/summary`
- Added V14 event scope migration for `entry_ref_type`, `entry_ref_id`, `path_id`, `level_id`, and `wrong_item_id`
- Python `StrategySelector` now forces `WRONG_BOOK_ENTRY -> REFLECT` and `LEARNING_PATH_ENTRY -> RECOMMEND`
- Frontend now shows Agent reflection/recommendation/summary panels without blocking the original page flow if Agent calls fail

## Automated QA

| Area | Command | Result |
| --- | --- | --- |
| Java compile | `.\mvn-local.cmd -q -DskipTests clean compile` | Passed |
| Java targeted tests | `.\mvn-local.cmd -q '-Dtest=AgentDtoJsonContractTest,AgentContextBuilderTest,AgentDecisionEnforcerTest,AgentFeedbackServiceTest,AgentEntryServiceTest,ProblemCoachAccessPolicyTest,ProblemCoachRecommendationServiceTest' test` | Passed; JVM CDS warning only |
| Python unit tests | `.\venv\Scripts\python.exe -m unittest discover -s tests -v` | Passed; 7 tests |
| Python compile | `.\venv\Scripts\python.exe -m compileall -q app` | Passed |
| Frontend build | `npm run build` | Passed; existing large chunk warning |
| Frontend lint | `npm run lint` | Passed with 0 errors and 2380 warnings |

## Core Scenario Coverage

- Wrong-book entry returns `REFLECT / reflection / REVIEW_AFTER_SOLUTION`
- Learning-path entry returns `RECOMMEND / recommendation / RECOMMEND_REMEDIATION`
- Wrong-book event records `entry_ref_type = WRONG_BOOK` and `wrong_item_id`
- Learning-path event records `entry_ref_type = LEARNING_PATH_LEVEL`, `path_id`, and `level_id` with `problem_id = NULL`
- Feedback accepts learning-path scope without `problem_id`
- Feedback rejects mismatched `level_id`
- Report summary groups action counts and weak points from Java-side event/state data

## Fixes During QA

- Repaired DTO JSON contract tests with clean V2 field assertions
- Added missing recent-query mapper methods for event, interaction, and feedback logs
- Added path-level null-problem handling so recommendation events do not mutate problem state
- Normalized frontend Agent decision fields from both snake_case and camelCase responses

## Known Risks

- Browser-level manual checks and 390x844 mobile screenshot checks were not run in this pass.
- Frontend lint has 2380 warnings, but 0 errors. This matches the current warning-tolerant gate.
- Vite reports large chunks for `element-plus` and `monaco`; tracked as a build warning.

## Phase 8 Readiness

Allowed to enter Phase 8. The Phase 7 integration points compile, test, and build successfully under the current automated gate.
