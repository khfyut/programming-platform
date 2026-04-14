# Learning Agent QA Checklist

## Backend

- `.\mvn-local.cmd -q -DskipTests clean compile`
- `.\mvn-local.cmd -q '-Dtest=AgentDtoJsonContractTest,AgentContextBuilderTest,AgentDecisionEnforcerTest,AgentFeedbackServiceTest,AgentEntryServiceTest,ProblemCoachAccessPolicyTest,ProblemCoachRecommendationServiceTest' test`

Verify:

- old advice API still returns compatible fields
- problem coach chat still uses `/api/ai/problem-agent/chat`
- wrong-book reflection returns `REFLECT / reflection / REVIEW_AFTER_SOLUTION`
- learning-path recommendation returns `RECOMMEND / recommendation / RECOMMEND_REMEDIATION`
- path-level event can be logged without `problem_id`
- feedback rejects cross-user and mismatched scope requests
- feedback write does not mutate `learning_stage`

## Python Agent

- `.\venv\Scripts\python.exe -m unittest discover -s tests -v`
- `.\venv\Scripts\python.exe -m compileall -q app`

Verify:

- wrong-book entry routes to `REFLECT`
- learning-path entry routes to `RECOMMEND`
- Phase 5 RAG behavior still keeps content refs and content type aligned
- no-LLM fallback remains Chinese and action-specific

## Frontend

- `npm run build`
- `npm run lint`

Verify manually:

- wrong-book detail dialog loads the Agent reflection card and still shows original details if Agent fails
- learning-path level page loads the Agent recommendation card and still shows resources/problems if Agent fails
- learning analysis page shows an Agent event summary or a friendly empty state
- problem page coaching/advice remains callable
- 390x844 viewport does not introduce horizontal overflow in the updated Agent panels

## Final Acceptance

- Java is the only trusted state and permission owner
- Python does not mutate DB state or decide answer permissions
- RAG only enriches content
- feedback is evaluative only and does not drive automatic learning
- legacy APIs remain compatible
