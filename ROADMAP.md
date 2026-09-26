# Create Aeronautics → Fabric 1.21.11 (Create Fly) port

Target stack: **Minecraft 1.21.11 + Fabric + Create Fly** (`com.zurrtum.create`).
Upstream Aero path is **NeoForge 1.21.1 + official Create**. This tree is a long-term fork.

## Dependency chain

```
Create Aeronautics
  └─ Simulated (+ Offroad)
       └─ Sable (physics / sublevels)
            ├─ sable-companion
            ├─ Veil (rendering)
            ├─ sable_rapier (native Rapier JNI)
            └─ Forge Config API Port
  └─ Create API  → remap to Create Fly
```

## Phases

| Phase | Goal | Status |
|------|------|--------|
| 0 | Workspace + this roadmap | done |
| 1 | Sable Fabric 1.21.11 compile spike | done |
| 2 | Port / vendor **Veil** + **sable-companion** for 1.21.11 | done enough — 1.21.1 JiJ + client mixin strip / stubs |
| 3 | Sable Fabric jar runs on create-fly-test client | done — join + place + 45s stable |
| 4 | Simulated Fabric module + Create Fly API remap | in progress — Registrate shim from Railways; loom project next |
| 5 | Aeronautics Fabric module | pending (bundled with 4) |
| 6 | Runtime integration test (assemble / fly structure) | pending |

## Known blockers (2026-09-17)

| Dep | Upstream | 1.21.11 Fabric? |
|-----|----------|-----------------|
| Fabric API | `0.141.6+1.21.11` | yes (create-fly-test) |
| Forge Config API Port | `21.11.1` | yes |
| NeoForm | `1.21.11-20251209.172050` | yes (for common) |
| NeoForge | `21.11.x` | yes (not our runtime target) |
| **Veil** | maven only `veil-fabric-1.21` / `1.21.1` (latest 4.5.0) | **no** |
| **sable-companion** | `*-1.21.1` only on ryanhcode maven | **no** |
| Create Aeronautics / Simulated | NeoForge-only modules | no Fabric |
| Create | official `com.simibubi.create` | use **Create Fly** instead |

### Sable compile blockers (1.21.11 Mojmap)

- Render rewrite: `ShaderInstance` / `BakedModel` / `VertexBuffer` → `RenderPipeline` / `GpuBuffer`
- Tickets: `TicketType`/`Ticket` no longer generic; use `TicketStorage`
- Level: `isClientSide` private; new `environmentAttributes()`; chunk tick serialization signature

## Spike strategy (Phase 1)

1. Branch `port/1.21.11-fabric` on vendored `sable/`.
2. Bump MC / Fabric / NeoForm / FCAP to 1.21.11.
3. Resolve compile against **1.21.1** Veil + companion jars (API may partially work; documents real MC breakages).
4. Collect compile errors → decide whether to vendor-port Veil/companion next or stub render paths.

## Workspace layout

```
create-aero-fabric-port/
  ROADMAP.md
  libs/                 # Create Fly reference jar
  sable/                # vendored upstream (branch port/1.21.11-fabric)
  Simulated-Project/    # vendored upstream (untouched until phase 4)
  notes/                # spike logs
```

## Create Fly note

Official Create packages ≠ Create Fly. Any `com.simibubi.create` / Flywheel / Ponder references in Simulated/Aeronautics must be remapped or rewritten against `com.zurrtum.create` after Sable boots.

## Reference ports (use these)

Already running on create-fly-test — copy **patterns**, not physics:

| Reference | Use for |
|-----------|---------|
| [Create Fly](https://github.com/ZurrTum/Create-Fly) + Modrinth `create-fly:1.21.11-6.0.9-5` | Gradle dep, `modid=create`, package `com.zurrtum.create` |
| CEI-fly / Dragons Plus-fly | `com.simibubi.*` → `com.zurrtum.*` remap; client/flywheel/ponder split; `create_plugin` entrypoint |
| Steam 'n' Rails Fly | Larger multiloader→Fabric Create Fly port |
| Cyber Goggles / vault / freights | Lightweight Fly API + Cloth Config + mixin style |
| `create-fly-ponder-fix` (local) | Fly-specific runtime mixin quirks |

**Not covered by Fly addons:** Veil, sable-companion, Rapier JNI — still independent 1.21.11 ports.
