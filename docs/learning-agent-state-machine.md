# Learning Agent State Machine

## Fixed Actions

The action space stays intentionally small:

- `GUIDE_IDEA`
- `HINT`
- `DIAGNOSE`
- `EXPLAIN`
- `RECOMMEND`
- `REFLECT`
- `REVEAL_ANSWER`

Each action must align with `pedagogical_goal` and `content_type`.

| action_type | pedagogical_goal | content_type |
| --- | --- | --- |
| `GUIDE_IDEA` | `GUIDE_INITIAL_THINKING` | `guidance` |
| `HINT` | `GIVE_LIGHT_HINT` | `hint` |
| `DIAGNOSE` | `DIAGNOSE_ERROR_CAUSE` | `diagnosis` |
| `EXPLAIN` | `EXPLAIN_CONCEPT` | `explanation` |
| `RECOMMEND` | `RECOMMEND_REMEDIATION` | `recommendation` |
| `REFLECT` | `REVIEW_AFTER_SOLUTION` | `reflection` |
| `REVEAL_ANSWER` | answer release after constraint approval | `solution` |

## State Updates

Java applies state updates only after the decision enforcer marks the action as executed.

| Action | State effect |
| --- | --- |
| `GUIDE_IDEA` | updates last action semantics; does not escalate `learning_stage` |
| `HINT` | increments `hint_count`; advances to at least `HINTED` |
| `DIAGNOSE` | increments `diagnose_count`; writes `error_tag` / `weak_points`; advances to at least `DIAGNOSED` |
| `EXPLAIN` | increments `explain_count`; advances to at least `EXPLAINED` |
| `RECOMMEND` | increments `recommend_count` only for problem-scoped events |
| `REFLECT` | increments `reflect_count` for wrong-book problem scope; does not mark mastered |
| `REVEAL_ANSWER` | sets `has_viewed_answer = true` only when Java allows execution |

## Non-Problem Scope

Learning-path recommendation uses `entry_ref_type = LEARNING_PATH_LEVEL` and does not update a problem interaction.

Wrong-book reflection uses `entry_ref_type = WRONG_BOOK` and may update the linked problem's `reflect_count`.

Feedback logs record user reactions but do not change `learning_stage`.

## Stale State Protection

Problem-page context treats Java submission facts as more trusted than stale interaction state.

- stale `last_submit_id` is ignored when it does not belong to the current user/problem
- cross-user submit access is rejected before context construction
- Python never decides whether a full answer is allowed
