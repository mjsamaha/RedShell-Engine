# RedShell Engine — Implementation Plan

> A phased delivery plan for RedShell Engine v0.1, derived from [`overview.md`](./overview.md) and [`architecture.md`](./architecture.md).

**Author:** LobsterChops
**Base package:** `com.lobsterchops.redshell`
**Target milestone:** RedShell v0.1 (see overview §9)
**Status:** Planning

---

## 1. How to Read This Plan

Every phase has:

- **Goal** — what is true when the phase is finished.
- **User story** — who benefits and why, written from the point of view of the person using the engine.
- **Acceptance criteria** — testable statements. The phase is not done until all of them pass.
- **Work breakdown** — ordered tasks mapped to packages and classes from `architecture.md`.
- **Proof** — the example or sandbox feature that demonstrates the phase works (overview goal 4: *every system must be justified by a game that needs it*).
- **Tests** — what is unit-tested headlessly and what is verified by hand.
- **Decisions & risks** — open decisions from overview §10 that must be closed in this phase, and known risks.

Phases are an ordering, not deadlines. A phase may start only when the previous phase's exit criteria are met, except where a task is explicitly marked *can start early*.

### Personas used in the user stories

| Persona | Who they are |
| --- | --- |
| **Engine developer** | The person building RedShell itself. |
| **Game developer** | The person building a game *on* RedShell, using only its public API. |
| **Player** | The person playing a game built on RedShell. |

---

## 2. Phase Overview

| Phase | Theme | Primary persona | Outcome |
| --- | --- | --- | --- |
| **0** | Project alignment | Engine developer | Repository matches the documented layout, toolchain and package rules. |
| **1** | Foundation | Game developer | A game boots through `GameDefinition`, never touching `ApplicationListener`, and draws to a camera. |
| **2** | Playable runtime | Game developer | Scenes, entities, ECS, sprites, animation, input actions and assets work together. |
| **3** | Gameplay systems | Game developer / Player | Collision, audio, events, UI/HUD, timers and save/settings. |
| **4** | Developer experience | Engine developer / Game developer | Debug overlay, profiler, inspector, asset validation, examples and packaging. |
| **5** | v0.1 validation | Game developer | Sandbox meets the v0.1 Definition of Done and a second small game runs without engine changes. |

```
Phase 0 ──► Phase 1 ──► Phase 2 ──► Phase 3 ──► Phase 4 ──► Phase 5 (v0.1)
alignment   foundation  runtime     gameplay    tooling     validation
```

---

## 3. Phase 0 — Project Alignment DONE

> **Status: implemented.** See the status notes at the end of this section.

### Goal

The repository structure, toolchain and package naming match the documentation, so that later phases build on the intended boundaries rather than on the generated LibGDX template.

### Current state (gap analysis)

The repository is currently an unmodified gdx-liftoff template. It differs from the documents in several ways that must be resolved before Phase 1:

| Area | Current | Documented target | Action |
| --- | --- | --- | --- |
| Modules | `core`, `lwjgl3`, `html` | `redshell-engine`, `redshell-desktop`, `redshell-html`, `games/sandbox`, `examples` | Rename / restructure modules; **keep** the web module. |
| Package | `com.mjsamaha.redshellengine` | `com.lobsterchops.redshell` (engine), `com.lobsterchops.sandbox` (game) | Move packages. |
| Java level | `sourceCompatibility = 8` | Records are used for immutable config (architecture §11) | Raise desktop/engine tooling to Java 17, **but** the engine source must also compile under GWT (see below). |
| HTML/GWT | `html` module present (GWT 2.11.0) | Web (browser) deployment is a required target | **Keep** the module and make the engine GWT-compatible from day one (see §3.1). |
| Entry point | `RedShell extends Game` + `FirstScreen` | Games never implement `ApplicationListener` | Replace in Phase 1 with `RedShellApplication`. |
| Tests | None | "Tests where sensible" (overview §8) | Add JUnit 5 to the engine module. |
| Doc links | Docs link `OVERVIEW.md` / `ARCHITECTURE.md` | Files are lower-case | Fix links or rename files. |

### 3.1 Web (GWT) compatibility — a cross-cutting constraint

The browser build is a first-class target. LibGDX's HTML backend compiles Java to JavaScript with GWT, which restricts what engine code may use. These rules apply to **every** phase and to all engine and game source (not test source):

| Area | GWT restriction | Engine rule |
| --- | --- | --- |
| Language level | GWT 2.11 supports Java 11 syntax only; records need a newer GWT (2.12+, to be verified in the Phase 0 spike). | Use records only if the spike confirms support; otherwise use small final classes. Avoid pattern matching, sealed types and text blocks until confirmed. |
| Reflection | Not available. | No reflection or `Class.forName`; use factories and explicit registration (prefabs, catalogs, events). Any LibGDX reflection use needs GWT reflection includes. |
| Threads | Single-threaded. | No `Thread`, `ExecutorService`, `synchronized`-dependent design or blocking waits. Async loading goes through `AssetManager`, which is GWT-safe. |
| File I/O | No `java.io.File` / `java.nio.file`. | Files only via `Gdx.files`; save data and settings via `Preferences` or `FileHandle` (browser local storage). `FileLogSink` and Gradle-side validation are desktop-only. |
| JDK APIs | Partial JRE emulation. | Prefer LibGDX collections, `MathUtils` and `TimeUtils`; no `String.format`-heavy or `java.util.concurrent` code in runtime paths. |
| Third-party libs | Must ship a GWT module (`.gwt.xml`) and sources. | Ashley (has a GWT module) is acceptable; Box2D needs the GWT Box2D backend; FreeType needs the GWT FreeType extension or pre-baked bitmap fonts. Check each dependency before adopting it. |
| Platform differences | Browser fullscreen, audio autoplay and storage differ. | Route them through `LaunchConfig` / `AudioService` / `SettingsService`; first user gesture unlocks audio. |
| Entry point | `GwtLauncher` in `html`. | Same pattern as desktop: a thin `RedShellGwt.launch(GameDefinition)`. |

Every `.gwt.xml` module descriptor must list the engine and game source paths (the template's `RedShell.gwt.xml` becomes `RedShellEngine.gwt.xml`).

### User story

> **As an** engine developer,
> **I want** the Gradle project to be organised into an engine library, desktop and web launchers and a sandbox game with enforced package boundaries,
> **so that** engine code can never accidentally depend on game code or on LibGDX internals it should not touch, the same game runs on desktop and in a browser, and every later phase has a clean home.

### Acceptance criteria

1. `settings.gradle` includes `redshell-engine`, `redshell-desktop`, `redshell-html` and `games:sandbox`.
2. `redshell-engine` has no dependency on `redshell-desktop`, `redshell-html` or any `games:*` project (verified by Gradle dependency report).
3. All engine source lives under `com.lobsterchops.redshell`.
4. Every package in architecture §2 exists with a `package-info.java` stating its purpose and allowed dependencies (architecture §11–12).
5. `./gradlew build` succeeds on Java 17, and `./gradlew test` runs JUnit 5 (at least one placeholder test).
6. An automated architecture test (ArchUnit) fails the build if a layer depends upward, or if `games.*` is imported from the engine (architecture §1 rules 1–5).
7. `./gradlew :redshell-desktop:run` still opens a window.

### Work breakdown

1. Rename `core` → `redshell-engine`, `lwjgl3` → `redshell-desktop`; create `games/sandbox` and an empty `examples` folder.
2. Remove `html` from `settings.gradle`.
3. Set Java toolchain to 17 in the root `build.gradle`.
4. Move packages to `com.lobsterchops.redshell` (engine) and `com.lobsterchops.sandbox` (game).
5. Create the full package skeleton with `package-info.java` files.
6. Add JUnit 5 and ArchUnit as `testImplementation` in `redshell-engine`; write `LayeringRulesTest` encoding the layer diagram.
7. Make the desktop and web launchers depend on a game module rather than on the engine template class.
8. Fix the cross-document links in `overview.md` and `architecture.md`.
9. **GWT spike (time-boxed):** compile the skeleton for the browser; confirm which language level and whether records work with the current GWT 2.11.0 (upgrade `gwtFrameworkVersion` if a newer GWT supports them); record the outcome in an ADR.
10. Rename the template GWT descriptors (`RedShell.gwt.xml`, `GdxDefinition*.gwt.xml`) to match the new module and package names, and inherit the engine module from the web module.
11. Add a GWT-compatibility check to CI (`./gradlew :redshell-html:gwtCompile`), plus an ArchUnit rule banning `java.io.File`, `java.nio.file`, `java.util.concurrent`, `Thread` and `java.lang.reflect` in engine packages (the exceptions are desktop-only modules).
12. Optional: add a CI workflow running `./gradlew build test`.

### Proof

The desktop launcher opens an empty window **and** the web build (`:redshell-html:superDev`) renders the same empty canvas in a browser from the restructured project, and `LayeringRulesTest` passes.

### Decisions & risks

- **Decision:** Keep the root project name `redshell-root` or rename to `redshell` (overview §7). Cosmetic, decide once.
- **Decision:** Records vs. final classes for configuration, depending on the GWT spike result.
- **Risk:** IDE run configurations in `.idea` point to old module names. Regenerate them after the rename.
- **Risk:** GWT compatibility regressions creep in silently. Mitigation: the CI GWT compile and the ArchUnit ban list run on every change from this phase on.

### Exit criteria

All acceptance criteria pass on both desktop and web. No production engine code has been written yet beyond the skeleton.

### Implementation status (Phase 0)

- Modules: `redshell-engine`, `redshell-desktop`, `redshell-html`, `games:sandbox` (root project name `redshell`); `examples/` is an empty placeholder.
- Packages: engine under `com.lobsterchops.redshell` with all 57 `package-info.java` files; launchers under `...redshell.desktop` / `...redshell.html`; game under `com.lobsterchops.sandbox`.
- Java levels: engine, games and web at `--release 11`, desktop at 17 (see `docs/architecture/ADR-001-java-level-and-gwt.md`). Records are deferred until GWT supports them.
- `LayeringRulesTest` covers util independence, no upward layer dependencies, core not using app, engine not using games/launchers, Box2D confinement and the GWT bans. The same-layer rule (architecture §1 rule 3) is intentionally not encoded yet because the documents themselves need `app` to use `core` and `ecs` to use `entity`.
- `TempSandboxApp` was replaced in Phase 1 by `SandboxGame implements GameDefinition`.

---

## 4. Phase 1 — Foundation DONE

### Goal

A game describes itself through `GameDefinition` and the engine owns the LibGDX lifecycle, the clock, logging and a camera-backed renderer.

**Packages:** `app`, `core` (`config`, `context`, `lifecycle`, `time`), `util`, `debug.log`, minimal `rendering`, `rendering.camera`, `rendering.viewport`.

### User story

> **As a** game developer,
> **I want** to start a new game by implementing a single `GameDefinition` and launching it from the desktop **or web** module,
> **so that** I never write `create / render / resize / dispose` boilerplate and can immediately draw something in world coordinates with a correctly-resizing camera.

### Acceptance criteria

1. The sandbox contains no class that implements or extends a LibGDX `ApplicationListener`, `Game` or `Screen`.
2. `RedShellApplication` forwards `create`, `render`, `resize`, `pause`, `resume`, `dispose` to `Engine` and contains no gameplay logic.
3. `LaunchConfig` controls window title, resolution, fullscreen, VSync and target FPS; the desktop and web launchers both read it (VSync/FPS are ignored on web, where the browser owns the frame loop).
4. `EngineSystem`s are initialised in registration order and disposed in reverse order (unit-tested).
5. `GameClock` reports delta, total time, time scale and pause; pausing stops game time but not rendering.
6. `FixedStepAccumulator` runs N fixed steps per frame and caps accumulated time after a stall (unit-tested; opt-in via `EngineConfig`).
7. `Log` supports levels and tags, writes to a console `LogSink`, and uncaught engine exceptions are logged once with context.
8. `Renderer2D` owns a single `SpriteBatch`; `GameCamera` + `ViewportPolicy` keep a fixed world size across window resizes.
9. `util.math`, `util.timer`, `util.random` and `util.collection` have unit tests and no RedShell imports.
10. The sandbox draws a textured sprite and moves the camera with a hard-coded key (temporary, replaced by input actions in Phase 2).

### Work breakdown

1. **`util`** *(can start early)* — `MathUtil` (clamp, lerp, easing, angles), `Timer`, `Cooldown`, `DelayedAction`, `SeededRandom`, ring buffer.
2. **`core.lifecycle`** — `EngineSystem` interface (all default methods), `SystemRegistry` with ordered init and reverse disposal.
3. **`core.config`** — `EngineConfig` record (fixed timestep, debug flag, step size, max frame time), `ConfigRegistry`.
4. **`core.time`** — `GameClock`, `FixedStepAccumulator`.
5. **`core.context`** — `EngineContext` (small and explicit: clock, config, log; later phases add services).
6. **`core`** — `Engine` (`frame()` implements the frame flow in architecture §9 as far as current systems allow), `EngineBuilder` with fixed service registration order.
7. **`app`** — `GameDefinition` (name, `LaunchConfig`, initial scene factory — replaced by an initial-scene factory in Phase 2), `LaunchConfig` record, `RedShellApplication`.
8. **`debug.log`** — `Log`, `LogLevel`, `LogSink`, `ConsoleLogSink`, engine exception reporter.
9. **`rendering`** — `Renderer2D`, `RenderContext`.
10. **`rendering.camera`** — `GameCamera`, `CoordinateConverter`.
11. **`rendering.viewport`** — `ViewportPolicy` (FIT, FILL, STRETCH, EXTEND), `ResizeHandler`.
12. **`redshell-desktop`** — generic launcher: `RedShellDesktop.launch(GameDefinition)`.
12b. **`redshell-html`** — generic GWT launcher: `RedShellGwt.launch(GameDefinition)`, canvas sizing from `LaunchConfig`, GWT module descriptors.
13. **Sandbox** — `SandboxGame implements GameDefinition` drawing one sprite.

### Proof

`examples/hello-redshell`: window opens, one sprite drawn, resizing keeps aspect ratio, log shows ordered init/dispose — on desktop **and** in the browser (canvas resize, tab hidden/visible → `pause`/`resume`).

### Tests

- Unit: `SystemRegistry` order, `GameClock` scale/pause, `FixedStepAccumulator` step count and stall cap, all `util` classes.
- Headless: boot `Engine` with LibGDX `HeadlessApplication` and assert lifecycle forwarding.
- Manual: resize and minimise the window; confirm no GL errors and clean shutdown.
- Web: resize the browser window and hide/show the tab; confirm the loop pauses/resumes and the canvas scales.

### Decisions & risks

- **Close decision — Fixed timestep:** confirm *opt-in per game* (overview §10) and encode it in `EngineConfig`.
- **Close decision — Dependency access:** confirm *explicit `EngineContext` injection*.
- **Risk:** `EngineContext` growing into a service locator. Mitigation: it only exposes service interfaces, and every new field requires a justification in its Javadoc.

### Exit criteria

All acceptance criteria pass, and the sandbox boots through the public API only.

### Implementation status (Phase 1)

Implemented and verified (`gradlew build` with 31 tests, `:redshell-html:dist`, desktop window run):

- `util`: `MathUtil`, `Timer`/`Cooldown`/`DelayedAction`, `SeededRandom`, `RingBuffer`.
- `debug.log`: `Log` with levels, tags and pluggable `LogSink`s; `ConsoleLogSink`; `ExceptionReporter` (one report per failure).
- `core`: `Engine`, `EngineBuilder`, `EngineConfig` (fixed timestep opt-in, off by default), `ConfigRegistry`, `GameClock` (scale, pause, stall cap), `FixedStepAccumulator`, `EngineSystem`, `SystemRegistry` (ordered init, reverse dispose, failing dispose does not stop others), `EngineContext`.
- `rendering`: `Renderer2D`, `GameCamera`, `ViewportPolicy`, `ResizeHandler`, `CoordinateConverter`, `RenderConfig`.
- `app`: `RedShellApplication`, `LaunchConfig`, `GameDefinition`; launchers `RedShellDesktop.launch(...)` and abstract `RedShellGwt`.
- Sandbox: `SandboxGame` plus `SandboxView`, which draws a sprite grid and moves the camera with WASD/arrows. It uses no LibGDX `Game`/`Screen`/`ApplicationListener`.

Deviations and decisions:

- `RootView` (removed in Phase 2) stood in for scenes; `RenderBracket` lets the engine wrap renders without depending on `rendering`.
- `debug.log` is exempt from the "nothing below depends on `debug`" rule; `app` is the composition root (see `architecture.md` §1). `LayeringRulesTest` encodes both, plus util and `debug.log` dependency whitelists and a ban on `Gdx` in `core`/`util`/`debug.log`/`events`.
- GWT lacks `Class.cast` and `Class.isInstance`, so typed lookups use unchecked casts.
- `examples/hello-redshell` is deferred; the sandbox is the proof of the public API.
- The same-layer dependency rule remains unencoded (see Phase 0).
- Not verified: the web build is compiled and linked only, not run in a browser; there is no headless `RedShellApplication` test.
---

## 5. Phase 2 — Playable Runtime DONE

### Goal

A game can be structured into scenes containing entities processed by systems, drawn as animated sprites, driven by logical input, with assets loaded and released by a central service.

**Packages:** `scene` (`stack`, `transition`, `loading`), `entity` (`component`, `prefab`), `ecs` (`system`, `query`), `rendering.sprite`, `rendering.animation`, `rendering.layer`, `input` (all sub-packages), `assets` (`catalog`, `loader`), plus a minimal `events` core (pulled forward from Phase 3 because `SceneManager` fires `SceneChangedEvent`).

### User story

> **As a** game developer,
> **I want** to build a main menu scene and a gameplay scene where an animated player entity moves in response to named actions like `MOVE_LEFT` and `JUMP`,
> **so that** I can create a playable prototype by composing scenes, entities and systems, without touching raw key codes, `AssetManager` or resource disposal myself.

### Acceptance criteria

1. **Scenes** — `SceneStack` supports push, pop and replace; `SceneFlags` controls whether the scene below keeps updating and/or rendering (pause overlay renders over a frozen level).
2. Scene lifecycle order is `onEnter → update/render → onExit → dispose`, and unloading a scene releases its `World`, scene-scoped assets and event subscriptions (verified by a leak test).
3. `FadeTransition` and `InstantTransition` work between two scenes.
4. `LoadingScene` shows progress while an `AssetGroup` loads asynchronously, then activates the target scene.
5. **ECS** — Ashley vs. in-house decision is recorded; `World` runs systems in explicit priority order; structural changes during iteration go through `EntityCommandBuffer`.
6. `Query` selects entities by required/excluded components; `QueryCache` avoids per-frame rescans.
7. Built-in components exist: `Transform`, `Sprite`, `Animation`, `Velocity`, `Lifetime` (plus `Collider` as data only — collision logic is Phase 3).
8. Built-in systems exist: `MovementSystem`, `AnimationSystem`, `LifetimeSystem`, `RenderSystem`.
9. `Prefab` / `PrefabRegistry` build an entity from an ID.
10. **Rendering** — `RenderLayer` + `LayerSorter` order draws by layer then Y/z-index; sprites come from atlas regions via `AtlasRegionRef`.
11. `Animation2D` supports loop modes; `AnimationController` switches state (idle/run/jump); `AnimationEvent` fires on a chosen frame.
12. `CameraController` follows a target entity with bounds.
13. **Input** — game code reads `ActionState` (pressed/held/released/analog) only; `BindingSet` maps keyboard and gamepad; `ContextStack` disables `GAMEPLAY` actions when a `MENU` context is pushed.
14. `InputRouter` owns the single `InputMultiplexer`.
15. **Assets** — `AssetCatalog` maps logical IDs to paths/types; `AssetGroup`s load/unload together; `GdxAssetService` reports progress; entities never dispose shared assets.
16. No sandbox class references `Gdx.input`, `Gdx.files`, `AssetManager` or `Texture.dispose()`.

### Work breakdown

1. **ECS evaluation spike (first, time-boxed)** — prototype the sandbox player on Ashley; record the decision. If adopted, `ecs` becomes a thin adapter and `entity` wraps Ashley types so games never import Ashley directly.
2. **`events` (minimal)** — `EventBus`, `Event`, `Subscription`, `SceneChangedEvent`, `WindowResizedEvent`.
3. **`assets`** — `AssetService`, `GdxAssetService`, `AssetHandle<T>`, `AssetCatalog`, `AssetGroup`.
4. **`input`** — device adapters → `Binding`/`BindingSet` → `Action`/`ActionState` → `InputService` → `InputContext`/`ContextStack` → `InputRouter`.
5. **`entity` + `ecs`** — `Entity`, `EntityId`, `EntityTag`, components, `World`, `EntitySystem`, `Query`, `QueryCache`, `EntityCommandBuffer`, `Prefab`, `PrefabRegistry`.
6. **`rendering.sprite` / `layer` / `animation`** — `SpriteSheet`, `AtlasRegionRef`, `Sprite2D`, `RenderLayer`, `LayerSorter`, `Animation2D`, `AnimationController`, `AnimationEvent`.
7. **Engine systems** — `MovementSystem`, `AnimationSystem`, `LifetimeSystem`, `RenderSystem`.
8. **`rendering.camera`** — `CameraController` (follow, bounds; zoom and shake can follow later in this phase).
9. **`scene`** — `Scene`, `SceneContext`, `SceneManager`, `SceneStack`, `SceneFlags`, transitions, `LoadingScene`, `SceneLoader`.
10. Replace the Phase 1 temporary `RootView` hook in `GameDefinition` with an initial scene factory.
11. **Sandbox** — `MenuScene`, `GameplayScene`, `PauseScene` (overlay), player prefab with idle/run animations, camera follow.

### Proof

Sandbox flow: `Loading → Menu → Gameplay ⇄ Pause → Menu`, animated player controlled by actions, camera follow, clean exit with zero leaked textures.

### Tests

- Unit: `SceneStack` transitions and flags, `ContextStack` action masking, `Query` matching, `EntityCommandBuffer` deferral, `LayerSorter` ordering, `Animation2D` frame timing, `AssetCatalog` duplicate-ID rejection.
- Headless: `GdxAssetService` load/unload reference counting with a headless backend.
- Manual: rapid scene switching, alt-tab during loading, gamepad hot-plug.
- Web: run the sandbox flow in the browser; async asset loading shows progress without blocking, and keyboard input works when the canvas has focus.

### Decisions & risks

- **Close decision — ECS implementation:** Ashley vs. minimal in-house (overview §10).
- **Risk:** Scene-scoped vs. shared asset ownership is the most likely source of leaks and double-disposal. Mitigation: `AssetService` is the only class calling `unload`; scenes declare their `AssetGroup` rather than loading directly.
- **Risk:** Phase 2 is the largest phase. Split delivery into three internal milestones: *2a* assets + input, *2b* ECS + rendering, *2c* scenes + sandbox integration.

### Exit criteria

All acceptance criteria pass; the sandbox is a playable (if collision-free) prototype built only on the public API.

---

### Implementation status (Phase 2)

Implemented and verified (`gradlew build` with 60 tests incl. ArchUnit, `:redshell-html:dist` GWT compile, desktop run through Menu → Loading → Gameplay → Pause → Menu):

- `events`: `EventBus` (typed, exact-class dispatch, snapshot iteration, failing listener is logged), `Subscription`/`SubscriptionGroup`, `SceneChangedEvent`, `WindowResizedEvent`, `AssetsLoadedEvent`, `PausedEvent`.
- `assets`: `AssetService`/`GdxAssetService` (ref-counted groups over `AssetManager`), `AssetCatalog` (logical IDs, groups, duplicate/unknown rejection), `AssetHandle`, `AssetValidator`.
- `input`: `InputService` (actions with pressed/held/released/analog), `BindingSet`/`BindingLoader` (text format `ACTION=KEY:A,KEY:LEFT`), `ContextStack` (opaque contexts mask actions; no phantom press on pop; empty stack restricts nothing), `InputRouter` (single `InputMultiplexer`), keyboard and mouse devices.
- `rendering`: `RenderLayer` + `LayerSorter`, `Sprite2D`, `SpriteSheet`, `Animation2D`/`AnimationController` (once/loop/ping-pong, frame events), `CameraController` (follow, smoothing, bounds), `Renderer2D.draw(Sprite2D)`/`fillRect`/`fillScreen`.
- `entity`/`ecs`: entities, components, prefabs, `World` with priority-ordered systems, cached `Query`, `EntityCommandBuffer` (structural changes during a system throw unless deferred), built-in movement/animation/lifetime/render/camera-follow systems.
- `scene`: `Scene` (owns its world, subscriptions, input context and one asset-group reference), `SceneStack` with OPAQUE/OVERLAY/TRANSPARENT flags, `SceneManager` (queued push/pop/replace/load), `FadeTransition`, `LoadingScene` + `SceneLoader`.
- `GameDefinition` now has `createInitialScene()`, `assetCatalog()`, `defaultBindings()`, `registerPrefabs(...)`; `RootView` was removed.
- Sandbox: `MenuScene`, `GameplayScene` (30×20 tile map, animated player, bounded camera follow), `PauseScene` overlay. Sandbox code touches no `Gdx.input`, `Gdx.files`, `AssetManager` or `Texture.dispose()`.

Deviations and decisions:

- ECS is in-house, not Ashley (see `docs/architecture/ADR-002-in-house-ecs.md`). No spike against Ashley was prototyped; the decision was made on GWT control and API size.
- **Gamepad adapter deferred.** Gamepad binding types and the `InputDevice` extension point exist, but no device implements them (needs gdx-controllers and a GWT module).
- `PrefabRegistry` only stores prefabs; spawn through `World.spawn` or the command buffer, because `entity` may not depend on `ecs`.
- `SceneChangedEvent` carries scene names (strings), because `events` may not depend on `scene`.
- Key names in binding files use LibGDX names (case-insensitive), e.g. `KEY:ENTER`, `KEY:ESCAPE`; `BACKSPACE` is not a valid name.
- The `examples/hello-redshell` project is deferred; the sandbox serves as the example.
- The same-layer dependency rule is still not encoded in ArchUnit (docs conflict within layers). The Gdx-statics ban now also covers `entity`, `ecs` and `scene`.
- The web build is compiled (GWT) but not run in a browser. GWT forbids `Class.cast`/`isInstance`/array `clone()`; code avoids them.
---

## 6. Phase 3 — Gameplay Systems DONE 

### Goal

Games have collision, audio, decoupled events, timers, Scene2D-based UI and persistent settings/saves.

**Packages:** `physics` (`collision`, `layer`, optional `box2d`), `audio` (`catalog`), `events` (completed, `types`), `ui` (`hud`, `menu`, `theme`), `save` (`data`, `settings`), `util.timer` integration.

### User story

> **As a** game developer,
> **I want** my player to collide with walls and pick up coins that play a sound, update a HUD counter and persist my volume and key bindings between sessions,
> **so that** I can ship a small but complete game loop without writing collision, audio, UI or persistence plumbing myself.

*(Player perspective: "As a player, I want my audio volume and controls to be remembered next time I launch the game.")*

### Acceptance criteria

1. **Physics** — `AABB` and `Circle` shapes with `CollisionResult`; `CollisionSystem` resolves solid contacts and raises trigger events; `CollisionLayer` + `CollisionMatrix` filter pairs; `Raycast` returns the first hit.
2. A broad phase (uniform grid or sort-and-sweep) keeps collision cost sub-quadratic for a few hundred entities.
3. No class outside `physics.box2d` imports Box2D (ArchUnit rule). Box2D adapter is only built if a game requires it.
4. **Audio** — `AudioService` plays sounds by logical ID from `AudioCatalog`; `AudioCategory` volumes (`MUSIC`, `SFX`, `UI`, `AMBIENT`) are independent; mute works; music crossfades between scenes.
5. No game code references LibGDX `Sound` or `Music`.
6. **Events** — `EventBus` is complete and typed; listeners registered by a scene are removed automatically when it unloads; engine events `AssetsLoadedEvent` and `PausedEvent` are published.
7. **UI** — `UiService` owns the `Stage` and registers it with `InputRouter` at higher priority than gameplay; UI consumes events it handles.
8. `UiTheme` loads a skin and fonts (FreeType); `ui.menu` provides main menu, pause menu, settings screen and dialog building blocks; `ui.hud` provides counters, bars and prompts.
9. **Timers** — `Timer`, `Cooldown` and `DelayedAction` respect `GameClock` time scale and pause.
10. **Save** — `SettingsService` persists volume, bindings and display options; `SaveService` reads/writes/lists/deletes versioned `SaveSlot`s; a `Migration` upgrades a v1 save to v2 in a test.

### Work breakdown

1. **`events`** — complete `EventBus` (typed, safe removal during dispatch), `events.types`, scene-owned subscription tracking.
2. **`physics`** — shapes, `CollisionResult`, `CollisionLayer`, `CollisionMatrix`, `CollisionWorld` (broad + narrow phase), `Raycast`, `PhysicsService`, `CollisionSystem`.
3. **`audio`** — `SoundDefinition` record, `AudioCategory`, `AudioCatalog`, `AudioService`, `GdxAudioService`.
4. **`ui`** — `UiService`, `UiScreen`, `UiTheme`, menu and HUD building blocks; integrate into frame flow after scene render.
5. **`save`** — `Settings`, `SettingsService` (LibGDX `Preferences`), `SaveData`, `SaveSlot`, `SaveService` (JSON), `Migration`.
6. Wire `BindingSet` persistence into `SettingsService`; add rebinding to the settings screen.
7. **Sandbox** — walls, coins (trigger colliders), a hazard enemy entity type, HUD coin counter, SFX and music, settings screen.

### Proof

Sandbox: player collides with level geometry, collects coins (SFX + HUD update via events), menu and settings screens built with `ui`, settings survive restart.

### Tests

- Unit: shape overlap and penetration, `CollisionMatrix` filtering, raycast hits, `EventBus` subscribe/unsubscribe during dispatch, category volume maths, save migration, settings round-trip.
- Manual: UI click does not trigger gameplay action underneath; audio mute persists.
- Web: audio starts only after the first click/key press; settings persist in browser storage across reloads; collision and event code runs without reflection or threads.

### Decisions & risks

- **Close decision — Physics:** confirm plain AABB is sufficient for the sandbox; defer Box2D (overview §10).
- **Risk:** Tunnelling of fast entities through thin walls. Mitigation: clamp max velocity or sweep tests in `CollisionSystem`.
- **Risk:** Browser audio autoplay policy and format support (use OGG/MP3 that both backends support). Mitigation: `GdxAudioService` defers playback until the first input and the asset validator flags unsupported web formats.
- **Risk:** FreeType and Box2D are not GWT-ready out of the box. Mitigation: use bitmap fonts for the web build (or the GWT FreeType extension), and keep Box2D out of the sandbox.
- **Note:** `save.data` is not part of the v0.1 Definition of Done; if Phase 3 runs long, `SaveService` may slip after v0.1 while `SettingsService` stays in scope.

### Exit criteria

All acceptance criteria pass; the sandbox has a complete core loop with collision, audio and UI.

---

### Implementation status (Phase 3)

**Status: implemented.** `gradlew build` is green (unit tests + GWT compile) and the desktop sandbox runs Menu → Settings → Loading → Gameplay without errors or warnings.

- **Physics:** in-house AABB/circle shapes, uniform-grid broad phase, collision layers/matrix, triggers, raycast and overlap queries (`PhysicsService`), contact events via the `EventBus`. `CollisionSystem` does one push pass; there is no swept test, so very fast bodies can tunnel (documented in its javadoc). Box2D adapter deferred, as planned; ArchUnit confines Box2D imports to `physics.box2d`.
- **Audio:** `AudioService`/`AudioMixer` (master, per-category, mute), `AudioCatalog`, music crossfade; on web, audio stays locked until the first key press or touch.
- **UI:** Scene2D `UiService` with a code-built `UiTheme`, `MenuScreen`, `SettingsScreen` (volume, mute, fullscreen, key rebinding), HUD widgets. Pointer capture stops game clicks passing through UI.
- **Save/settings:** versioned `SaveData` (own text format; LibGDX Json needs reflection on GWT), `SaveService` with migration chain, `SettingsService` backed by `Preferences` (localStorage on web).
- **Sandbox:** walls, coins (triggers), patrolling hazards, HUD counter, SFX/music, pause and settings screens; art, font and WAV audio are generated.

Deviations / open items:
- WAV audio instead of OGG/MP3; `AssetValidator` does not yet flag unsupported web audio formats.
- The web build compiles but has not been run in a browser (audio unlock, storage persistence unverified).
- UI assumes the FIT viewport policy; gamepad adapter still deferred.
- No dedicated `CollisionSystem` or timer-under-pause tests yet.

## 7. Phase 4 — Developer Experience DONE

### Goal

The engine is observable, assets are validated before runtime, focused examples exist for each subsystem, the build produces a distributable desktop package and a deployable browser build.

**Packages:** `debug.overlay`, `debug.profiler`, `debug.inspector`, `debug.log` (file/in-memory sinks), `assets.validation`, `examples/*`, packaging.

### User story

> **As a** game developer,
> **I want** to toggle an in-game debug overlay showing FPS, frame time, entity count and collision bounds, and to have missing assets reported before the game even starts,
> **so that** I can diagnose performance and content problems quickly without attaching a debugger or reading engine source.

### Acceptance criteria

1. `DebugOverlay` (toggle key bound via an action) shows FPS, frame time, entity count, draw calls (render calls from `SpriteBatch`) and memory.
2. Collision bounds and the active system list can be toggled on the overlay.
3. `Profiler` measures named scopes around every system update and render pass; `FrameStats` shows rolling averages.
4. `EntityInspector` lists entities, components and tags read-only at runtime.
5. Removing the `debug` package (or disabling it via `EngineConfig`) does not change game behaviour (ArchUnit rule: nothing below Layer 5 imports `debug.overlay`/`profiler`/`inspector`).
6. `AssetValidator` runs at startup (and as a Gradle task) and reports missing files, duplicate IDs and unsupported types.
7. `LogSink` supports file and in-memory output; the overlay can show recent log lines.
8. `examples/` contains at least: `sprite`, `ecs`, `scene-stack`, `input-actions`, `collision`, `audio`.
9. `./gradlew :games:sandbox:dist` (or equivalent) produces a runnable desktop JAR; optional `jpackage`/Construo native bundle.
9b. `./gradlew :redshell-html:dist` produces a static folder (HTML, JS, assets) that runs from any web server; the sandbox plays in a current desktop browser.
9c. The debug overlay and asset validator work on web (file and memory log sinks: only the memory sink is available in the browser).
10. Each package has a short guide in `docs/guides/` sufficient to use it without reading source (overview §8).

### Work breakdown

1. `debug.profiler` — `Profiler`, `FrameStats`; instrument `Engine.frame()` and `World`.
2. `debug.overlay` — `DebugOverlay` drawn after UI, using RedShell `ui` (no ImGui in the engine).
3. `debug.inspector` — `EntityInspector` (read-only).
4. `debug.log` — `FileLogSink`, `MemoryLogSink`.
5. `assets.validation` — `AssetValidator` + Gradle `validateAssets` task.
6. Examples modules, each one focused screen of code.
7. Packaging tasks for desktop and web (`dist` for the GWT build, assets list generation, optional deployment to a static host such as GitHub Pages or itch.io).
8. Guides under `docs/guides/`.

### Proof

Sandbox with overlay toggled, inspector open on the player entity, and a deliberately broken catalog entry caught by `validateAssets`.

### Tests

- Unit: `FrameStats` rolling average, `AssetValidator` on fixture catalogs.
- Build: `validateAssets`, `dist` and `:redshell-html:dist` run in CI.
- Manual: overlay overhead stays under ~0.5 ms/frame in the sandbox.

### Decisions & risks

- **Risk:** Debug code leaking into engine layers. Mitigation: ArchUnit rule and running the full sandbox test with debug disabled.
- **Optional:** An ImGui tooling module, only as a separate Gradle module depending on the engine (architecture §8.2).

### Exit criteria

All acceptance criteria pass; a new contributor can run examples and read guides without help.

---

### Implementation status (Phase 4)

Done:
- Debug overlay (F3 stats, F4 colliders, F5 system timings, F6/F7/F8 entity inspector, recent log lines) in `debug.overlay`, backed by `Profiler`/`FrameStats` and the read-only `EntityInspector`.
- Profiling through a `ScopeListener` hook in `SystemRegistry` and `World`; the profiler is only wired in when `EngineConfig.debug` is on.
- Logging: in-memory ring buffer sink (also shown in the overlay) and a desktop-only file sink at `~/.redshell/<logFileName>`.
- `AssetValidator`: missing files, unsupported extensions per type, one file under two IDs. Runs at startup (debug) and as `gradlew :redshell-desktop:validateAssets`, which is part of `check`/CI. Verified by breaking a catalog entry: the task fails with the file named.
- `examples` module with six examples (`runExample -Pexample=<name>`), `:games:sandbox:dist` desktop JAR, CI step, and guides in `docs/guides/`.
- Tests: `DebugToolsTest`, plus an ArchUnit rule keeping the debug tools out of the lower layers.

Deviations and open items:
- The overlay text needs the UI font in the scene's asset group; otherwise only collider bounds are drawn.
- Draw calls count the world batch only.
- Examples are desktop only (not GWT-compiled). The overlay on web is compiled but not tested in a browser; the file sink is desktop-only by design.
- Overlay overhead (<0.5 ms) is not measured.
- Native bundle (Construo) and the ImGui-based tooling are not done.

---

## 8. Phase 5 — v0.1 Validation DONE

### Goal

Prove the engine against the overview's success criteria: the sandbox meets the v0.1 Definition of Done (overview §9), and a second, different small game runs on RedShell without modifying engine source (overview goal 3).

### User story

> **As a** game developer,
> **I want** to build a second, different small game (for example a top-down arena shooter alongside the sandbox platformer) using only RedShell's public API,
> **so that** I have confidence the engine is genuinely reusable and not secretly shaped around a single game.

### Acceptance criteria

The sandbox, built entirely through the public engine API, includes:

1. A main menu scene and a gameplay scene.
2. A controllable player plus a few entity types.
3. Sprite animation, camera follow and collision.
4. Sound effects and music.
5. Asset loading, scene transitions and clean disposal (zero leaked native resources on exit).
6. A debug overlay showing FPS, frame time and entity count.

And additionally:

7. A second game module (`games/<second-game>`) is playable and required **zero** changes to `redshell-engine` source during its development. Any engine change discovered as necessary is fed back as a Phase 2–4 fix *before* the second game is considered done, and the second game is then re-verified.
8. Public API is documented (Javadoc on every public type in the engine) and the engine is versioned `0.1.0`.
9. All tests and ArchUnit rules pass in CI.
10. Both games run on **desktop and in a browser** (`redshell-html`) from the same game code, with no platform-specific code in either game module.

### Work breakdown

1. Audit the sandbox against the six Definition-of-Done items; close gaps.
2. Run a disposal/leak check (LibGDX `GLProfiler` and texture counts across repeated scene switches).
3. Build the second game in a new `games/` module.
4. Log every engine friction point; fix in the engine, then re-run both games.
5. Review the public API surface; mark internal classes package-private where possible.
6. Tag `v0.1.0`, publish a changelog, and update `overview.md` status from *Design phase* to *v0.1*.

### Proof

Two different games, launched from the same `redshell-desktop` and `redshell-html` launchers, both built only on `com.lobsterchops.redshell.*`.

### Exit criteria

RedShell v0.1 is released.

### Implementation status (Phase 5)

Acceptance criteria:
1. Sandbox DoD items 1-6: all present and exercised (menu and gameplay scenes, controllable player with coins and patrolling hazards, animation, camera follow, collision, sound and music, scene transitions, debug overlay). Verified earlier by running it on desktop.
2. Disposal: `gradlew :redshell-desktop:leakCheck` loads and unloads every asset group 25 times (also overlapping loads) for both games and found no assets or textures left (sandbox peak 6 textures, arena peak 4, zero left). Not part of `check` because it needs a GL context. `GLProfiler` is not used.
3. Second game: `games/arena`, a top-down twin-stick arena shooter (own scenes, systems, collision layers, assets and sounds). It used only `com.lobsterchops.redshell.*` and needed no engine source changes. One friction point was found and not changed: creating entities from a system must go through `world.commands().spawn(Prefab)`; the engine's error message says so and the guides now state it.
4. Both games run from the same launchers: `run -Pgame=sandbox|arena` on desktop and `index.html` / `index.html?game=arena` in the browser. Both were loaded in headless Chrome: the menus render and the arena was played past the menu.
5. Javadoc: every public top-level and nested type in `redshell-engine` has Javadoc, and every package has a `package-info.java` (checked by script, not enforced by the build). Version is `0.1.0` (`gradle.properties`, jar name `RedShell Engine-0.1.0.jar`), with `CHANGELOG.md`.
6. `build` (tests, ArchUnit, asset validation for both games, GWT compile) passes.

Open items:
- The `v0.1.0` git tag is not created: this folder is not a git repository. Tag the release commit yourself: `git tag v0.1.0`.
- Public API narrowing (making internal classes package-private) was not done; only documentation was reviewed.
- Audio in the browser was not heard (headless run); WAV is still used by both games.
- No automated tests exist inside the two game modules.
- Game modules still contain no platform-specific code; only the launchers choose between games.

---

## 9. Cross-Cutting Practices (all phases)

| Practice | Rule |
| --- | --- |
| **Definition of done per feature** | Works in a small example, has tests where sensible, and is documented well enough to use without reading source (overview §8). |
| **Layering** | Enforced by ArchUnit from Phase 0 onward; a failing layering test blocks merge. |
| **Ownership** | Every disposable has one owner (engine, scene or `AssetService`); new resource types state their owner in Javadoc. |
| **Threading** | No OpenGL calls off the GL thread; async loading only through `AssetManager`. No threads at all in engine code (GWT is single-threaded). |
| **Web compatibility** | Every engine and game source file must compile under GWT (§3.1). CI compiles the web build on every change, and sandbox is smoke-tested in a browser at the end of each phase. |
| **Config** | No magic numbers in systems; tunables live in records under `config`. |
| **Low-level access** | Only `app` and the `rendering`/`assets`/`audio`/`input` adapters touch `Gdx.*` (architecture §1 rule 7). |
| **Package docs** | Every package keeps an up-to-date `package-info.java`. |
| **Minimal first** | A class is added only when an example or game needs it (overview goal 4). |

## 10. Decision Log Schedule

| Decision (overview §10) | Closed in | Default assumption |
| --- | --- | --- |
| Fixed timestep: always on vs. opt-in | Phase 1 | Opt-in per game |
| Dependency access: `EngineContext` vs. service locator | Phase 1 | Explicit `EngineContext` injection |
| Records vs. final classes (GWT language level / GWT version upgrade) | Phase 0 (spike) | Upgrade GWT if records are supported, otherwise final classes |
| ECS: Ashley vs. in-house | Phase 2 (spike first) | Evaluate Ashley first; it must be GWT-compatible, otherwise go in-house |
| Physics: AABB vs. Box2D | Phase 3 | AABB; Box2D only if a game needs it |

Record each outcome as a short ADR in `docs/architecture/` (`ADR-001-java-level-and-gwt.md` already records the Phase 0 decision; later ones follow, for example `ADR-002-fixed-timestep.md`).

## 11. Traceability — v0.1 Definition of Done

| v0.1 requirement (overview §9) | Delivered in |
| --- | --- |
| Main menu and gameplay scene | Phase 2 (scenes), Phase 3 (menu UI) |
| Controllable player + entity types | Phase 2 (ECS, input), Phase 3 (enemy/coin types) |
| Sprite animation, camera follow, collision | Phase 2 (animation, camera), Phase 3 (collision) |
| Sound effects and music | Phase 3 |
| Asset loading, scene transitions, clean disposal | Phase 2, verified in Phase 5 |
| Debug overlay (FPS, frame time, entity count) | Phase 4 |
| Runs on desktop and web | Phase 0 (setup), every phase (GWT CI), Phase 5 (verified) |
| Built entirely through the public API | Enforced every phase; verified in Phase 5 |
