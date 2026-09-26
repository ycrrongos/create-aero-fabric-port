#!/bin/sh
# Usage: tools/port/mixin_check.sh <compiled classes dir> <mixins.json>... [-- extra classpath...]
# Checks mixin targets against the named 1.21.11 Minecraft jar and remapped mod dependencies.
ROOT=$(cd "$(dirname "$0")/../.." && pwd)
ASM=$(ls /root/.gradle/caches/modules-2/files-2.1/org.ow2.asm/asm/9.9.1/*/asm-9.9.1.jar)
TREE=$(ls /root/.gradle/caches/modules-2/files-2.1/org.ow2.asm/asm-tree/9.9.1/*/asm-tree-9.9.1.jar)
MC=$(ls /root/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/1.21.11-*/minecraft-merged-1.21.11-*[0-9].jar | head -1)
CLASSES=$1; shift
JSONS=""
while [ $# -gt 0 ] && [ "$1" != "--" ]; do JSONS="$JSONS $1"; shift; done
[ "$1" = "--" ] && shift
MODS=$(find "$ROOT/.gradle/loom-cache/remapped_mods" -name "*.jar" ! -name "*-sources.jar" 2>/dev/null)
exec java -cp "$ASM:$TREE" "$ROOT/tools/port/MixinCheck.java" $JSONS -- "$CLASSES" "$MC" "$@" $MODS
