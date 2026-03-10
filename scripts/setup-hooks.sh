#!/bin/sh
# Install Git hooks from scripts/git-hooks/ into .git/hooks/
# Run from repo root: ./scripts/setup-hooks.sh

set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
HOOKS_SRC="$ROOT/scripts/git-hooks"
HOOKS_DST="$ROOT/.git/hooks"

for hook in "$HOOKS_SRC"/*; do
  [ -f "$hook" ] || continue
  name="$(basename "$hook")"
  dest="$HOOKS_DST/$name"
  cp "$hook" "$dest"
  chmod +x "$dest"
  echo "Installed hook: $name"
done
echo "Done. Pre-commit will run: make test"
