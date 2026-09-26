# Create Aeronautics → Fabric 1.21.11 (Create Fly) port

WIP port of Create Aeronautics / Simulated / Sable onto **Minecraft 1.21.11 Fabric + Create Fly** (`com.zurrtum.create`).

See `ROADMAP.md` for phases, blockers, and layout.

## Layout

- `aeronautics-fabric/` — Fabric Loom port of Aeronautics (in progress)
- `sable/` — vendored Sable, branch work for 1.21.11 Fabric
- `Simulated-Project/` — vendored Simulated / Aeronautics upstream reference
- `libs/` — local jars (Create Fly, Sable build, Veil, shims)
- `notes/` — compile spike notes / logs (`.log` gitignored)
- `refs/` — reference ports (e.g. CEI-Fly patterns)

## Stack

- MC 1.21.11, Fabric, Java 21
- Create Fly instead of official Create
- Prefer FlClash proxy `127.0.0.1:7890` for Gradle downloads

This is unfinished work intended for continued agent porting (Claude Code / Cursor).
