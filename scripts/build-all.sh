#!/bin/sh
# Builds the application (all modules, with tests), then the AppImage and the .deb.
#
# Usage: ./scripts/build-all.sh

set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
SCRIPTS_DIR="$ROOT_DIR/scripts"
. "$SCRIPTS_DIR/_jdk.sh"

(cd "$ROOT_DIR" && ./gradlew build --console=plain)
"$SCRIPTS_DIR/build-appimage.sh"
"$SCRIPTS_DIR/build-deb.sh"
