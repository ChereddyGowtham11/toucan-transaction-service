# AI Usage Disclosure

## Tools used

Claude Code (Anthropic's CLI coding agent, model Claude Fable 5), used as a
pair-programming partner throughout the exercise.

## What it was used for

- Inspecting the starter project and confirming the untouched test suite passed
  before any changes were made
- Discussing and writing the design (layering, entity design, validation
  placement, status transition rules, API shapes) — the design was reviewed and
  approved by me before any implementation code was written
- Generating the implementation code and the MockMvc test suite, one operation
  at a time, with the full test suite run after each step
- Drafting this disclosure and the README

## Decisions that were mine, not the AI's

- Since my invitation contained no candidate-specific variant, I chose the
  validation values myself and documented them as assumptions: currencies
  INR/USD/EUR, maximum amount 100,000, types PAYMENT/REFUND/TRANSFER,
  amount > 0, unique transaction ID, non-blank customer ID
- Approving the 3-status lifecycle (PENDING/COMPLETED/FAILED with terminal
  states) over a larger one; CANCELLED was considered and deliberately left out
- Approving layer-based packages, `BigDecimal` for money, the natural primary
  key, and server-assigned initial status

## Significant AI-generated suggestions I accepted

- Using the client-supplied transaction ID directly as the JPA primary key so
  the database enforces uniqueness behind the service-level check
- Rejecting unknown enum values at JSON parsing time and translating Jackson's
  error into a message that lists the allowed values
- Returning 200 with an empty list (not 404) for a customer with no transactions
- Adding a `.gitignore` and removing the committed `target/` build output that
  shipped in the starter repository

## What the AI got wrong / what I corrected

- Nothing produced by the AI failed compilation or tests during this exercise.
  Each step was verified immediately by running the full suite, so any mistake
  would have been caught at the step that introduced it; the honest finding is
  that none surfaced.
- One style fix during development: a test initially referenced Hamcrest
  matchers by fully-qualified name instead of static imports; it was rewritten
  and the suite re-run before committing.

## How I verified the result

`mvnw.cmd clean test` was run on the untouched starter first (1 test passing),
then after every implemented operation, and finally on the complete project:
15 tests, 0 failures. The test output is included in TEST_OUTPUT.txt. The
endpoints and error responses are asserted by full-stack MockMvc tests rather
than manual checking alone.
