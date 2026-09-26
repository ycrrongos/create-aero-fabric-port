# Spike #8 — failed

FlClash SOCKS on Gradle again broke TLS fetching `net.neoforged:mergetool:2.0.7:api`.

**Pattern:** curl+FlClash OK; Java `socksProxyHost` not OK for Maven HTTPS.

**Spike #9:** prefetch jars to `~/.m2`, `mavenLocal()` first, Gradle without SOCKS.
