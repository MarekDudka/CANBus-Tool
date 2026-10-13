#!/bin/sh
# Builds the Linux .deb installer via Compose's :app:packageDeb (requires dpkg/fakeroot).
#
# Usage: ./scripts/build-deb.sh

set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
. "$(dirname -- "$0")/_jdk.sh"

(cd "$ROOT_DIR" && ./gradlew :app:packageDeb --console=plain)

echo "Deb written to:"
find "$ROOT_DIR/app/build/compose/binaries/main/deb" -name '*.deb'
