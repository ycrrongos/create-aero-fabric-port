# Aeronautics Fabric compile spike #1 (2026-09-17)

## Status

**Jar does NOT build yet.** `compileJava` fails (~100 errors, javac cap).

Project: `create-aero-fabric-port/aeronautics-fabric/`
- Loom Remap 1.21.11 + Fabric API `0.141.6+1.21.11`
- Sources: Simulated + Aeronautics **common** copied + Create Fly import remap
- Fabric entrypoints + ServiceLoader impls present
- Deps (files): create-fly, sable, registrate-shim, FCAP, veil, sable-companion

## Top error classes (compile-2)

| Category | Examples |
|----------|----------|
| **Create Fly API drift** | `AllTags` not in `com.zurrtum.create`; `SafeBlockEntityRenderer` missing; `ValueSettings` missing; `ContraptionMatrices` / `renderInContraption` removed from `MovementBehaviour` |
| **MC 1.21.11 Mojmap** | `net.minecraft.client.renderer.RenderType` moved; `ItemInteractionResult` package change; `net.minecraft.Util` path |
| **Excluded compat still referenced** | `DockingConnectorBlockEntity` → `compat.computercraft.wired` (compat package excluded from compile) |
| **Registrate / Create client split** | Many `foundation.*` types now under `com.zurrtum.create.client.*` (bulk auto-remap done; residual remain) |

## Logs

- `notes/aeronautics-compile-1.log` — first compile (before Veil/companion on classpath)
- `notes/aeronautics-compile-2.log` — after Veil + companion + package auto-remap

## Next coding steps (do not resurvey)

1. Map remaining Create Fly symbols (`AllTags`, `ValueSettings`, `SafeBlockEntityRenderer`) via `jar tf` / CEI-fly patterns.
2. Fix 1.21.11 render/interaction renames (`RenderType`, `ItemInteractionResult`).
3. Stop excluding `compat/computercraft` **or** stub/guard those imports in docking connector.
4. Iterate `compileJava` → `remapJar` → install to `create-fly-test` mods.

## Not done

- No `aeronautics*.jar` in `create-fly-test` server/client mods
- Offroad module not included
- Levitite fluids still NeoForge stubs
