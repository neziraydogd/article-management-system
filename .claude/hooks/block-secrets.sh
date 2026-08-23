#!/bin/bash
# PreToolUse hook: block writing hardcoded secrets into files.
#
# Wired to the Write and Edit tools. Claude Code passes a JSON event on stdin
# describing the tool call. This script pulls out the content Claude is about
# to write, scans it for secret-shaped patterns, and:
#   - exit 2  -> BLOCK the write, and send the reason to Claude via stderr
#   - exit 0  -> allow the write
#
# IMPORTANT (the classic hook footgun): only exit 2 blocks. exit 1 is treated
# as a non-blocking error and the write proceeds anyway. If you want to see
# this for yourself, change the `exit 2` below to `exit 1` and watch a secret
# sail straight through.

set -euo pipefail

INPUT=$(cat)

# The field holding the to-be-written content differs by tool:
#   Write -> tool_input.content
#   Edit  -> tool_input.new_string
# Concatenate whatever is present so one script covers both tools.
CONTENT=$(printf '%s' "$INPUT" | jq -r '
  (.tool_input.content // "") + "\n" + (.tool_input.new_string // "")
')

# Also grab the target path, purely to make the block message clearer.
FILE_PATH=$(printf '%s' "$INPUT" | jq -r '.tool_input.file_path // "(unknown)"')

# Nothing to scan -> allow.
if [ -z "${CONTENT//[$'\n\t ']/}" ]; then
  exit 0
fi

# --- Secret patterns, tuned to this Spring Boot / Java stack -------------
# Each pattern is deliberately narrow to avoid false positives on ordinary code.
# grep -nE: show line numbers, extended regex. -i where case shouldn't matter.
#
# We collect matches into a report; if the report is non-empty, we block.
REPORT=""

add_match() {
  local label="$1"; local pattern="$2"; local flags="${3:-}"
  local hits
  # shellcheck disable=SC2086
  hits=$(printf '%s' "$CONTENT" | grep -nE $flags "$pattern" || true)
  if [ -n "$hits" ]; then
    REPORT="${REPORT}
  [$label]
$(printf '%s' "$hits" | sed 's/^/    /')"
  fi
}

# Spring datasource / mail / server passwords set to a real (non-empty,
# non-placeholder) value. Allows empty and obvious placeholders through.
add_match "spring hardcoded password" \
  '(spring\.(datasource|mail)\.password|server\.ssl\.key-store-password)[[:space:]]*=[[:space:]]*[^[:space:]$#{].*' "-i"

# Generic hardcoded password/secret assignment in properties or Java.
add_match "hardcoded password/secret literal" \
  '(password|passwd|secret|api[_-]?key|token)[[:space:]]*[=:][[:space:]]*["'"'"']?[A-Za-z0-9/+_.-]{6,}["'"'"']?' "-i"

# AWS access key id.
add_match "AWS access key id" \
  'AKIA[0-9A-Z]{16}'

# AWS secret access key (40-char base64-ish assigned to an aws secret field).
add_match "AWS secret access key" \
  'aws_secret_access_key[[:space:]]*=[[:space:]]*[A-Za-z0-9/+=]{40}' "-i"

# Private key blocks.
add_match "private key block" \
  '-----BEGIN [A-Z ]*PRIVATE KEY-----'

# JWT-shaped tokens (three base64url segments).
add_match "JWT-shaped token" \
  'eyJ[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}'

# Slack / GitHub style tokens.
add_match "Slack/GitHub token" \
  '(xox[baprs]-[A-Za-z0-9-]{10,}|gh[pousr]_[A-Za-z0-9]{20,})'

# --- Decide ------------------------------------------------------------
if [ -n "$REPORT" ]; then
  # stderr is what Claude sees as the block reason on exit 2.
  {
    echo "BLOCKED: potential hardcoded secret in write to ${FILE_PATH}."
    echo "Move secrets to environment variables or an untracked local config"
    echo "(e.g. an env var referenced as \${DB_PASSWORD} in application.properties)."
    echo "Matches:"
    echo "$REPORT"
  } >&2
  exit 2   # <-- THE ONE THAT BLOCKS. exit 1 here would NOT block.
fi

exit 0
