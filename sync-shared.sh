#!/usr/bin/env bash
# Pull canonical textures and lang from main. Run on a port branch.
set -euo pipefail
git checkout main -- \
  src/main/resources/assets/saltandpepper/textures \
  src/main/resources/assets/saltandpepper/lang
echo "Synced textures and lang from main. Review with: git status"