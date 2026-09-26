# Sable compile spike 26 — common + fabric SUCCESS

Date: 2026-09-17

## Baseline → result

| Spike | `:common:compileJava` | `:fabric:compileJava` | Notes |
|------|------------------------|------------------------|-------|
| 25 | 100 (cap) | n/a | entity/packet/registry Mojmap |
| **26** | **SUCCESS** | **SUCCESS** | API fixes + excludes + stubs |

## What changed

### API fixes (correct Mojmap 1.21.11)
- Tickets: `PhysicsChunkTicketManager` → non-generic `Ticket`/`TicketType` via `SableTicketTypes`; refcounted force-load via `ServerChunkCache.addTicket` + accessor `TicketStorage`
- Registry: `REGISTRY.getValue(id)`; Veil `register(path, …)` string overload; `ForceGroups` no longer uses Veil `RegistryObject` (ResourceLocation bytecode)
- Entity: `snapTo` / `hurtMarked`; Player ctor `(Level, GameProfile)`; Level ctor without profiler supplier
- Packets: `ClientboundPlayerPositionPacket.of` + `PositionMoveRotation`; snapshot info dual-arg ctor; heightmaps `Map<Heightmap.Types,long[]>`
- Chunk/palette: `Strategy.createFor*`, `LevelChunkSection(palettedContainerFactory)`, `getOrThrow` holders
- NBT UUID: `tag.store`/`read` + `UUIDUtil`; IntArrayTag dependency lists
- Misc: `GameProfile.id()`, `getCameraEntity()`, `isClientSide()`, ItemStack cooldowns, `Direction.getNearest(..., default)`, OBB `getPosition()`, `Commands.hasPermission(LEVEL_GAMEMASTERS)`, reload listeners → `SimpleJsonResourceReloadListener` + Codec
- Fabric: ConfigRegistry v5, SharedState reload, world path via `LevelResource.ROOT`

### Excludes / stubs (non-critical / huge rewrite)
- Excluded: pathfinding, entity stick client lerp, entity rendering, respawn, udp mixins, toast, water occlusion mixins, fancy/vanilla render dispatchers, SubLevelVertexConsumer, GizmoScreen, optional compat
- Stubs: gizmo handler, water occlusion renderer, shading preprocessor, chunk debug renderer, Noop render dispatcher (pre-existing)
- Compile shim: `net.minecraft.resources.ResourceLocation` empty class for unremapped Veil on `:common` only (excluded from fabric compile)

### Build wiring
- Fabric `compileJava` excludes mirror common sourceSet excludes (commonJava artifact is still the raw source dir)

### Remap / sable_rapier (post-compile)
- `sable_rapier` companion dep: `$minecraft_version` → `$dep_minecraft_version` (1.21.1 artifacts)
- `Util` / `Util.OS` → `net.minecraft.util.Util`; `getMinBuildHeight` → `getMinY`
- `:fabric:remapJar` **SUCCESS**

## Output jars
- `/home/rong/RongMC_Update/create-aero-fabric-port/sable/fabric/build/libs/sable-fabric-1.21.11-2.0.5-port.1.21.11.jar`
- `/home/rong/RongMC_Update/create-aero-fabric-port/sable/fabric/build/libs/sable-sable_rapier-1.21.11-2.0.5-port.1.21.11.jar` (nested via remapRapierJar)

## Do not
- Global regex on `.contains` / `.getInt`
- Gradle `socksProxyHost`
- Restore Fancy/ShaderInstance trees
