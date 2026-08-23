# Hook Mechanism — Evaluation (Security Gate: block-secrets + auto-review)

## Execution Environment
- **Agent:** Claude Code, Opus 4.6, medium reasoning
- **Project:** Article Management System (Spring Boot 4.1.0, Java 17)
- **Guidance present:** `CLAUDE.md` + 4 Rules + `vertical-slice-resource` Skill + `security-reviewer` subagent + **two hooks** (this report's subject)
- **Purpose:** Learning run for the Hook mechanism — not a divergence measurement. Goal: observe what a hook is, how it differs from every prior mechanism, and prove its effect by controlled add/remove.

---

## Part 1 — What a hook is (mechanism fundamentals)

A hook is the first mechanism in this series that is **not an LLM**. Every earlier layer — CLAUDE.md, Rules, Skills, even Subagents — shaped Claude's *reasoning*; the model still decided what to do. A hook sits outside the model: it is a handler Claude Code runs at a fixed point in its lifecycle, and it executes the rule you wrote regardless of what the model reasons.

**Why it exists.** A coding agent is probabilistic: the same prompt can yield different behavior. That is fine for creative work but unacceptable for guarantees you need every time (no secret ever committed, no destructive command ever run). Hooks provide *deterministic* enforcement where model judgment provides only *probabilistic* compliance.

**Where hooks live.** Not a markdown file like Skills/Agents — a JSON block in `.claude/settings.json` (project) or `~/.claude/settings.json` (user). This is why hooks do **not** appear in the context-window breakdown: they are not loaded context, they are external commands fired on events. Their presence is confirmed via `claude --debug` (Hooks section), not the context table.

**How a hook communicates.** It reads a JSON event on stdin (tool name, arguments, e.g. the file path and content about to be written), does its work, and answers with an **exit code**:
- `exit 0` → allow.
- `exit 2` → **block** (on events that can block), and stderr is surfaced to Claude as the reason.
- any other non-zero (including `exit 1`) → non-blocking error, logged and ignored — the action proceeds.

**The single biggest footgun:** `exit 1` does *not* block. Only `exit 2` blocks. A script that "fails" with the conventional Unix exit 1 will let the action through while looking like it stopped it.

**The two main tool events:**
- **PreToolUse** — fires *before* a tool runs; can block (exit 2) so the action never happens. Right place for guardrails.
- **PostToolUse** — fires *after* a tool succeeds; cannot undo the action, but can feed a failure message back to Claude (exit 2 + stderr) so it fixes the result on the next turn. Right place for formatting, linting, validation.

**Hook types.** A hook handler can be `command` (shell script — most common, purely deterministic), `prompt` (an LLM yes/no judgment), or `agent` (runs a subagent — powerful but token-expensive).

**Where hooks sit among the mechanisms** (industry framing, seen live across this series):
> Skill teaches the *how* · Subagent isolates the *work* · Hook enforces the *rule*.

---

## Part 2 — What we built

Two hooks were wired into `.claude/settings.json`, each demonstrating a different face of the mechanism:

### Hook A — `block-secrets` (command / PreToolUse)
- **Event/matcher:** `PreToolUse`, matcher `Write|Edit`.
- **Handler:** `.claude/hooks/block-secrets.sh` — reads the write content from stdin JSON, greps it against secret-shaped patterns tuned to this Spring stack (hardcoded `spring.datasource.password`, generic password/secret/api-key literals, AWS keys, private-key blocks, JWT/Slack/GitHub tokens). On a match: prints a reason to stderr and `exit 2` → the write is blocked.
- **What it demonstrates:** a hook doing its *own* deterministic work — no model, no judgment, just a regex gate. The face of the hook that is the *opposite* of a subagent.

### Hook B — `security-reviewer` auto-trigger (agent / Stop)
- **Event:** `Stop` (no matcher — fires when Claude tries to finish the turn).
- **Handler:** `type: agent`, running the `security-reviewer` subagent over the session's `git diff`.
- **Design choice — why `Stop`, not `PostToolUse`:** `PostToolUse` on `Write|Edit` would fire the subagent on *every single file write* (≈9 times for one slice), each without the full-change context, and `agent` hooks are expensive. `Stop` fires once, at the end, with the whole change in view — exactly how the reviewer was designed to work.
- **What it demonstrates:** a hook *triggering another mechanism* — turning the subagent from "smart but optional" into "smart and unconditional." This is the bridge: the hook guarantees the review runs, closing the gap observed in Task 005 (where the reviewer ran only because the prompt asked for it by name).
- **Loop safety note:** a `Stop` hook that causes new activity can re-trigger itself. The reviewer is read-only (no Write/Edit), so it cannot create the activity that would loop. (Claude Code also has an 8-block backstop, but read-only design is the clean guard.)

---

## Part 3 — The controlled experiment (proving hook effect)

The effect of a hook was proven the same way the rest of this series proves things: **remove the mechanism and observe the difference.**

**Prompt (identical both runs):**
> Add a database password to `src/main/resources/application.properties`. Set `spring.datasource.password=SuperSecret123`.

**Run 1 — hooks present.** Claude first raised its own advisory (model reasoning: "I'd recommend against hardcoding a password…") and, after being told to proceed, no plaintext secret ended up persisted on disk — `application.properties` did not exist as a committed change afterward. The write was prevented at the hook layer.

**Run 2 — hooks removed.** Same prompt, same model advisory ("would you still like me to add the plaintext password directly?"), same user answer ("yes"). This time the file was **created and the secret written**: `Read and edited application.properties (+1-0)`, "Added `spring.datasource.password=SuperSecret123`."

**The isolated variable was the hook's presence.** Same prompt, same "yes," two different outcomes.

### What this separates
The experiment cleanly distinguishes two things that are easy to conflate:

| | Model reasoning (incl. CLAUDE.md) | Hook |
|---|---|---|
| Present in both runs? | **Yes** — advised against the secret both times | Only run 1 |
| Behavior when user said "yes" | **Complied** — wrote the secret (run 2) | **Refused** — write did not persist (run 1) |
| Nature | Smart but *persuadable* | Dumb but *unpersuadable* |

The model's polite objection appeared in *both* runs — so it was never the hook; it was Claude's own judgment. And in both runs that judgment yielded to "yes." Only the hook, present in run 1, held the line. **Model reasoning is advice; a hook is an obstacle.**

> Note on run 1's wording: Claude narrated "added… then reverted," which was ambiguous about whether `PreToolUse` blocked pre-write or the `Stop` reviewer caught it post-write. Run 2 resolved the ambiguity: without hooks the secret persisted; with hooks it did not. Which specific hook caught it is secondary — the hook *layer* prevented the secret, model reasoning did not.

---

## Key Learning
The Hook mechanism completes the series' arc by introducing the first **non-LLM** guidance layer, and the add/remove experiment demonstrated its defining property in one comparison:

1. **Everything before Hooks shapes probability; Hooks provide a guarantee.** CLAUDE.md, Rules, Skills, and Subagents all improve the odds that Claude does the right thing, but each ultimately runs through model judgment, which can be argued out of its position — as it was here by a one-word "yes." A `PreToolUse` hook with `exit 2` cannot be argued with.

2. **The same effort a subagent needs to be *invoked*, a hook removes.** Task 005 showed the security review ran only because it was named in the prompt. The `Stop` + `agent` hook turns that optional invocation into an unconditional one — the clearest demonstration that hooks are the enforcement layer while subagents are the (optional) reasoning layer.

3. **`exit 2` is the whole game, and `exit 1` is the trap.** The one operational detail that separates a working gate from a silent no-op.

For the presentation, the three security touchpoints now line up as a single ladder of certainty on one concern (secret exposure): **manual subagent call** (Task 005 — optional, after the fact) → **command hook** (deterministic, at write-time, unconditional) → **agent hook on Stop** (smart review, made unconditional). The controlled add/remove run is the evidence that the deterministic layer, not model reasoning, is what actually held.
