#!/bin/sh
# Usage: tools/port/javac_sable.sh <output dir> <source files...>
# Compiles a subset of Sable sources against Sable's compile classpath and the last Gradle-compiled Sable classes,
# so work on separate parts of Sable can be type-checked without running Gradle.
# The classpath is exported with: ./gradlew -I <printcp.gradle> :sable:printCompileCp -DcpOut=tools/port/.sable-cp.txt
ROOT=$(cd "$(dirname "$0")/../.." && pwd)
OUT=$1; shift
mkdir -p "$OUT"
CP=$(cat "$ROOT/tools/port/.sable-cp.txt")
exec javac -nowarn -proc:none -encoding UTF-8 --release 21 -d "$OUT" -cp "$CP:$ROOT/sable/build/classes/java/main" "$@" 2>&1
