# Spike #3 (FlClash SOCKS) — failed

Proxy OK. Failed after remapping with:

`Class org.gradle.jvm.toolchain.JvmVendorSpec does not have member field '...IBM_SEMERU'`

Cause: `foojay-resolver-convention` **0.8.0** on Gradle **9.5**.

Fix applied: bump to **1.0.0** in `settings.gradle` → spike #4 running.
