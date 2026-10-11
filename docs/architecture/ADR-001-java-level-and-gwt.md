# ADR-001 — Java language level and GWT compatibility

**Status:** Accepted (Phase 0)
**Context:** `docs/implementation-plan.md` §3 (Phase 0) and §3.1

## Context

RedShell targets desktop (LWJGL3) and the browser (GWT). Engine and game sources are compiled by GWT, so their language level and library usage are bound by what the GWT compiler supports. The template uses GWT 2.11.0 with `sourceLevel = '1.11'`. The architecture documents mention records for immutable configuration.

## Spike

The Phase 0 skeleton (engine with all `package-info.java` files, sandbox, launchers) was compiled with `./gradlew :redshell-html:dist` on GWT 2.11.0 with sources compiled at `--release 11`. The browser build compiles and links successfully.

Records were **not** adopted: GWT 2.11.0 targets Java 11 syntax and, to our knowledge, only newer GWT releases accept Java 16+ language features. Upgrading `gwtFrameworkVersion` also has to match the GWT backend shipped with LibGDX (`gdxVersion`), so it is not done speculatively.

## Decision

| Module | Compiler `--release` | Reason |
| --- | --- | --- |
| `redshell-engine` | 11 | Compiled by GWT |
| `games:*` | 11 | Compiled by GWT |
| `redshell-html` | 11 | GWT launcher |
| `redshell-desktop` | 17 | JVM-only launcher; not seen by GWT |

- Immutable configuration (`EngineConfig`, `LaunchConfig`, `SoundDefinition`, ...) is written as small `final` classes with final fields (and builders where useful), **not** records, until the project moves to a GWT release that supports records.
- Do not use pattern matching for `instanceof`, sealed types, text blocks or switch expressions in engine or game code.
- Compatibility is enforced by `LayeringRulesTest` (bans `java.io.File`, `java.nio.file`, `java.util.concurrent`, `java.lang.reflect` and `Thread` in engine packages) and by compiling the web build in CI.

## Revisit when

A LibGDX release ships a GWT backend built against a GWT version with Java 17 language support. At that point raise the engine and game `--release` to 17, switch configuration types to records and update this ADR.