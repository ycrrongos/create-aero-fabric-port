# Sable compile spike #1 (2026-09-17)

**Result:** BUILD FAILED (configure `:fabric`)

**Error:** `Failed to download maven info to fabric-api-0.141.6+1.21.11.pom`  
(at `fabric/build.gradle` line 32 — `fabricApi.module(...)`)

**Note:** Direct HTTP GET to Fabric Maven for that POM returns 200; likely transient/network or stale loom cache lock from canceled build.

**Follow-up:** spike #2 retry started; loom reported orphan cache lock and is rebuilding.
