#!/bin/sh
# Sourced by the build scripts: picks a JDK Gradle 8.12 and the bundled runtime can use.

# Gradle 8.12 (this project's wrapper version) doesn't run on very new JDKs (e.g. 25) — pin a
# known-compatible one if present, rather than silently inheriting whatever `java` the invoking
# shell happens to default to. Without this, running the script from a plain terminal can fail at
# the Gradle step while a JetBrains-configured IDE terminal (with its own Gradle JVM setting)
# succeeds, and — because that failure happens after `set -eu` but a stale AppImage from an
# earlier successful run is still sitting there — it's easy to not notice the rebuild didn't
# happen and run outdated code without any obvious error.
#
# Separately: whichever JDK builds the jlinked runtime gets bundled straight into the AppImage,
# and that choice affects more than compatibility — on at least Debian's OpenJDK 21 package,
# AWT's native MenuBar silently fails to render under this app's window manager (no error, no
# crash, the File/Help bar is just missing), while Amazon Corretto's build renders it fine. Any
# `~/.jdks/*` entry (how JetBrains IDEs install JDKs) is preferred first since that's most likely
# to be a non-Debian-packaged build; the openjdk-21 path is a compatibility-only fallback.
JAVA_HOME=""
for jdks_dir in "$HOME"/.jdks/*; do
    if [ -x "$jdks_dir/bin/java" ]; then
        JAVA_HOME="$jdks_dir"
        break
    fi
done
if [ -z "$JAVA_HOME" ]; then
    for candidate in /usr/lib/jvm/java-21-openjdk-amd64 /usr/lib/jvm/java-17-openjdk-amd64; do
        if [ -d "$candidate" ]; then
            JAVA_HOME="$candidate"
            break
        fi
    done
fi
if [ -n "$JAVA_HOME" ]; then
    export JAVA_HOME
    echo "Using JAVA_HOME=$JAVA_HOME"
fi
