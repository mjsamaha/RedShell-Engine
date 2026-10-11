# RedShell Engine — High-Level Overview

> A small, opinionated 2D game engine and boilerplate layer built on top of LibGDX (Java).

**Author:** LobsterChops
**Base package:** `com.lobsterchops.redshell`
**Language / build:** Java, Gradle
**Status:** Design phase

---

## 1. What RedShell Is

RedShell is a reusable engine layer that sits on top of LibGDX. The relationship is the same one a custom framework had with Java AWT: LibGDX supplies the platform plumbing, and RedShell supplies the structure, conventions and reusable systems that every game would otherwise rewrite.

RedShell is **not** a rewrite of LibGDX. It does not replace the renderer, the audio backend or the windowing layer. It wraps them behind a consistent, game-friendly API and owns the high-level concepts.

```
┌──────────────────────────────────────────────┐
│  Game Layer          (games/*)               │
│  Rules · Entities · Scenes · Game UI         │
├──────────────────────────────────────────────┤
│  RedShell Engine     (com.lobsterchops.      │
│                       redshell.*)            │
│  Lifecycle · Scenes · ECS · Rendering ·      │
│  Assets · Input · Audio · Events · Debug     │
├──────────────────────────────────────────────┤
│  LibGDX + Java                               │
│  OpenGL · Windowing · Input · Audio · Files  │
└──────────────────────────────────────────────┘
```

## 2. Goals

1. **Own the boilerplate.** A new game should start from a sandbox project and a handful of lines, never from raw `ApplicationListener` code.
2. **Hide LibGDX's lifecycle.** Games never manage `create / render / resize / dispose` directly.
3. **Prove it with real games.** The engine is considered valid when two different small games run on it without modifying engine source.
4. **Stay small.** Every system must be justified by a game that needs it.

## 3. Non-Goals (for now)

- A custom OpenGL renderer or shader pipeline
- A custom physics engine (simple AABB first, Box2D optional)
- Scripting languages or a modding system
- Networking and replication
- A visual scene editor
- Platforms beyond desktop and web (for example mobile and consoles)

These are deferred until an actual game requires them.

## 4. Design Principles

| Principle | Meaning |
| --- | --- |
| **Adapt, don't duplicate** | Wrap LibGDX (`AssetManager`, Scene2D, `Viewport`) instead of competing with it. |
| **Hybrid OOP + ECS** | Objects for services, scenes and lifecycle. ECS for high-volume gameplay entities. |
| **Explicit dependencies** | Systems receive the services they need through an `EngineContext` or constructors. No scattered static globals. |
| **One owner per resource** | Every disposable resource has exactly one owner: engine, scene or shared asset service. |
| **Logical input** | Game code reads named `Action`s, never raw key codes. |
| **Engine never depends on game** | Dependencies point downward only. The engine has no knowledge of any game or of debug tooling libraries. |
| **Minimal first** | Establish package boundaries early, fill them in as games demand. |

## 5. Major Subsystems

| Subsystem | Package | One-line purpose |
| --- | --- | --- |
| Application | `app` | Adapts LibGDX's `ApplicationListener` into the RedShell lifecycle. |
| Core | `core` | Engine bootstrap, configuration, context, clock and shutdown. |
| Scenes | `scene` | Scene lifecycle, scene stack and transitions. |
| Entities | `entity` | Entity identity, components and prefabs. |
| ECS | `ecs` | World, systems, ordering and queries. |
| Rendering | `rendering` | Cameras, viewports, sprites, layers and animation. |
| Assets | `assets` | Central asset registry, loading, caching and disposal. |
| Input | `input` | Action mapping, bindings and input contexts. |
| Physics | `physics` | Collision detection and optional Box2D integration. |
| Audio | `audio` | Music, sound effects and volume control. |
| UI | `ui` | Menus, HUDs and Scene2D integration. |
| Events | `events` | Decoupled messaging between systems. |
| Save | `save` | Versioned save data and settings (later phase). |
| Utilities | `util` | Math, timers, random and collection helpers. |
| Debug | `debug` | Logging, profiling, overlay and inspector. |

Full details for every package and sub-package live in [`architecture.md`](./architecture.md).

## 6. Core Concepts in One Page

**Engine and context.** A single `Engine` object is created by the application adapter. It builds an `EngineContext` holding the services (assets, input, audio, events) and drives every update and render pass.

**Lifecycle contract.** Everything engine-managed follows one predictable shape:

```java
public interface EngineSystem {
    default void initialize(EngineContext context) {}
    default void update(float deltaTime) {}
    default void render(float deltaTime) {}
    default void resize(int width, int height) {}
    default void dispose() {}
}
```

**Scenes.** A `Scene` owns its entities, its scene-scoped resources and its UI. Scenes live on a stack, so `Menu → Level → Pause` works without rebuilding anything.

**Entities.** An entity is an identity, a component is data, a system is behavior. A player is an entity with `TransformComponent`, `SpriteComponent`, `MovementComponent` and `ColliderComponent`. A `MovementSystem` and a `RenderSystem` act on them.

**Time.** A `GameClock` supplies frame delta time, with an optional fixed-timestep accumulator for games that need deterministic simulation. Simple games use plain delta time.

**Resource ownership.**

- Engine-owned: live for the whole application.
- Scene-owned: released when the scene unloads.
- Shared assets: managed centrally by `AssetService`.
- Entities never dispose shared textures. Worker threads never make OpenGL calls.

## 7. Project Layout (Gradle)

```
redshell/
├── redshell-engine/          # The engine library (no game code)
│   └── src/main/java/com/lobsterchops/redshell/...
├── redshell-desktop/         # LibGDX LWJGL3 launcher, kept separate from engine core
├── redshell-html/            # LibGDX GWT (browser) launcher; engine code must stay GWT-compatible
├── games/
│   └── sandbox/              # First proof-of-concept game
│       ├── src/
│       └── assets/
├── examples/                 # Small focused demos (sprite, ECS, scene stack)
├── docs/
│   ├── architecture/
│   └── guides/
├── build.gradle
├── settings.gradle
├── overview.md
└── architecture.md
```

Only create a new Gradle module when it represents a real dependency boundary. The engine is one module with strict package rules, not fifteen modules.

## 8. Roadmap

| Phase | Theme | Deliverables |
| --- | --- | --- |
| **1** | Foundation | Gradle project, launcher, lifecycle, game clock, logging, basic rendering and camera |
| **2** | Playable runtime | Scene stack, entity/component model, sprite animation, input actions, asset service |
| **3** | Gameplay systems | Collision, audio service, UI/HUD, events and timers, save/load and settings |
| **4** | Developer experience | Debug overlay, inspector, asset validation, example games, packaging and tests |

Phases are an ordering, not deadlines. A feature is complete when it works in a small example game, has tests where sensible, and is documented well enough to use without reading its source.

## 9. Definition of Done for v0.1

RedShell v0.1 is reached when the sandbox game is built **entirely through the public engine API** and includes:

1. A main menu scene and a gameplay scene
2. A controllable player plus a few entity types
3. Sprite animation, camera follow and collision
4. Sound effects and music
5. Asset loading, scene transitions and clean disposal
6. A debug overlay showing FPS, frame time and entity count

## 10. Open Decisions

| Decision | Options | Notes |
| --- | --- | --- |
| ECS implementation | Ashley vs. minimal in-house ECS | Start by evaluating Ashley. Write the in-house version only if it falls short. |
| Physics | Plain AABB vs. Box2D | Begin with AABB, add Box2D behind the `physics` package if needed. |
| Fixed timestep | Always on vs. opt-in | Opt-in per game is the working assumption. |
| Dependency access | `EngineContext` passed in vs. service locator | Prefer explicit injection. Revisit only if constructor lists become unmanageable. |
