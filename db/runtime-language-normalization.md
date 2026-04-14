# Runtime Language Normalization

`System/src/main/resources/db/migration/V5__normalize_problem_runtime_languages.sql`
fixes historical data where `problem.language` and
`problem_supported_language.language_code` stored content categories such as
`algorithm` or `database` instead of executable judge languages.

After V5 runs, problem pages should receive only executable runtime languages
from `/api/problem/{id}/supported-languages`:

- `java`
- `python`
- `cpp`
- `javascript`
- `typescript`
- `go`

Content categories should continue to live in tags or knowledge points, not in
runtime language fields.
