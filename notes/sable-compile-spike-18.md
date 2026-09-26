# Spike #17–18 — Ticket API port

## Done

- `SableTicketTypes` registered into `BuiltInRegistries.TICKET_TYPE`
- `InhabitedChunkTicket` / `PhysicsChunkTicketManager` use non-generic `Ticket` + `TicketStorage` via `ServerChunkCacheAccessor`
- One MC ticket per chunk while any sub-level inhabits it (type+level identity)
- `ServerChunkCacheMixin` ctor updated (dropped `ChunkProgressListener`; `lookupOrThrow`)
- `.isClientSide` → `.isClientSide()`
- WindCharge package move; imgui inspector deferred

## Error count

| Spike | Errors |
|-------|--------|
| 15–16 | ~100 (capped) |
| 17 (Ticket) | 58 |
| 18 | **53** |

## Remaining (mostly 1.21.11 render rewrite)

`ShaderInstance`, `BakedModel`, `VertexBuffer`, `RenderChunkRegion`, `Program`, `FogShape`, plus `ChunkSerializer` / `RelativeMovement` / `ChunkProgressListener` / `DebugPackets`.

**Next:** port or stub client render paths against `RenderPipeline` / `GpuBuffer`.
