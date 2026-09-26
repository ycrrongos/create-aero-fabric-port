# Spike progress (network change 2026-09-17 14:59)

## Network (current)

| Host | Direct | FlClash SOCKS |
|------|--------|---------------|
| Maven Central | OK | OK |
| maven.neoforged.net | FAIL | OK |
| GitHub | — | OK |

**Rule:** curl/git via FlClash for NeoForged/GitHub. Do **not** set Java `socksProxyHost` (breaks TLS). Prefetch NeoForm tools into `~/.m2`, Gradle uses `mavenLocal()` + direct Central.

## Status

- NeoForm `1.21.11` artifacts generate OK
- Mechanical mojmap renames applied (`ResourceLocation`→`Identifier`, `Util`/`FileUtil` packages, `critereon`→`criterion`, entity package splits, render package moves)
- Optional compat + sodium sources excluded for spike

## Remaining (real 1.21.11 port work)

Compile still fails (~100 errors capped). Big buckets:

1. **Render rewrite** — `ShaderInstance`, `BakedModel`, `VertexBuffer`, old shader `Program` gone → `RenderPipeline` / `GpuBuffer` / new model APIs
2. **Tickets** — `TicketType` / `Ticket` no longer generic
3. **Level API** — `isClientSide` private; `environmentAttributes()`; chunk tick serialization signature
4. **Misc** — `RelativeMovement`, `ChunkSerializer`, `ChunkProgressListener`, `DebugPackets`, windcharge package, imgui stubs

Next: port Ticket API + stub/exclude heaviest render paths, or vendor a minimal Veil/render shim.
