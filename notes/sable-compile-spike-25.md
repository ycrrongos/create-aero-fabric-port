# Sable compile spike 25 — render stub + Mojmap progress

Date: 2026-09-17

## Done this session

### RenderPipeline spike (goal of todo 6)
- Excluded ShaderInstance/VertexBuffer/Fancy/Vanilla mesh trees from `:common` compile.
- Added `NoopSubLevelRenderDispatcher` / `NoopSubLevelRenderData`; `SubLevelRenderer` only exposes `NOOP`.
- Stubbed `WaterOcclusionRenderer`, `SableSkyLightShadows`, preprocessors, `SimpleCulledRenderRegion`, `FancySubLevelShaderProcessor`, Iris compat.
- `SubLevelRenderDispatcher.renderSectionLayer` takes `Object` shader until RenderPipeline port.
- Platform `BakedModel` → `BlockStateModel` (impl no-op tesselate).
- Stripped broken client mixins from `sable.mixins.json` (Compass, GameRendererAccessor, AmbientOcclusionFace, RenderChunkRegion, debug/decal LevelRenderer).

### Mechanical 1.21.11
- `ChunkSerializer` → `SerializableChunkData`; `ChunkMap` ctor + `TicketStorage`.
- CompoundTag Optional getters (`get*Or` / `getCompoundOrEmpty` / `getListOrEmpty`) — **tag-scoped only** after a bad global rewrite.
- ClickEvent/HoverEvent → `CopyToClipboard` / `SuggestCommand` / `ShowText` records.
- `setBlockState(..., boolean)` → `Block.UPDATE_*`.
- Camera/MC: `getTimer`→`getDeltaTracker`, Camera `getPosition`→`position`, `getEntity`→`entity`.
- `Direction.getNormal`→`getUnitVec3i`, `getAllKeys`→`keySet`, `registryOrThrow`→`lookupOrThrow`, height `getMinY`/`getMaxY`/`getMinSectionY`.
- `ResourceKey.location`→`identifier` (via `dimension().identifier()`).
- Plot ticks: `PackedTicks` + `SavedTick.codec` store/read.

## Baseline

| Spike | `:common:compileJava` errors (javac cap 100) | Notes |
|------|-----------------------------------------------|-------|
| 19 | ~100 | Almost all ShaderInstance/VertexBuffer/BakedModel |
| 20 | 100 | Render cleared; NBT Optional + ClickEvent wave |
| 22 | 7 | Syntax from bad contains rewrite (fixed) |
| 25 | 100 (cap) | No ShaderInstance left; entity/packet/registry Mojmap |

## Remaining (next)

1. Entity sync: `lerpTo` / teleport packet APIs, `hasImpulse`, Path Extension casts.
2. Registry `Optional<Reference<T>>` unwrap (`PhysicsBlockPropertyTypes`).
3. Custom payload constructors (`ByteBuf` vs int).
4. `ClientChunkCacheMixin.replaceWithPacketData` heightmap map type.
5. `PalettedContainer.Strategy` / `getHolderOrThrow` on ServerLevelPlot.
6. Real RenderPipeline port to replace Noop (Veil 1.21.11 + companion first).

## Do not

- Global regex on `.contains(a,b)` / `.getInt(` — hits BoundingBox, BlockPos.containing, fastutil maps, Brigadier.
- Gradle `socksProxyHost` (use FlClash only for curl/git).
