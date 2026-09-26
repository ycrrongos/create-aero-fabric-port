# Spike #5b — failed (Java SOCKS TLS)

JDK 25 toolchain OK. Failed resolving `neoform-runtime` — `systemProp.socksProxyHost` breaks Java HTTPS TLS to Maven.

Fix: remove Gradle SOCKS; scope GitHub raw Maven with exclusiveContent; spike #6 running (direct Maven).

