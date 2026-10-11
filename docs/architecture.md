# RedShell Engine — Organizational Architecture

This document defines the package structure of RedShell Engine: what every package and sub-package owns, what it may depend on, and what it must never contain.

**Root package:** `com.lobsterchops.redshell`
**Companion document:** [`overview.md`](./overview.md)

---

## 1. Layering and Dependency Rules

Dependencies point **downward only**. A package may use packages beneath it and never above it.

```
Layer 5   debug (overlay/profiler/inspector) · ui
            │
Layer 4   scene · save
            │
Layer 3   ecs · entity · rendering · physics · audio
            │
Layer 2   assets · input · events
            │
Layer 1   core
            │
Layer 0   util          (depends on nothing but the JDK and LibGDX math/collections)
```

Rules:

1. `util` depends on no other RedShell package (only the JDK and LibGDX `math`/`utils`).
2. `core` defines the contracts (`EngineSystem`, `EngineContext`) that higher layers implement or consume.
3. Same-layer packages do not depend on each other directly. They communicate through `events` or through `EngineContext`.
4. The engine never imports anything from a `games/*` module.
5. `debug` (overlay, profiler, inspector) and `ui` may observe any layer, but nothing below them depends on them. The engine runs correctly with those removed. `debug.log` is the exception: it is cross-cutting infrastructure that depends only on `util` and the JDK, and any package may use it.
6. The engine never depends on third-party tooling UIs (such as ImGui). Those live in a separate optional module.
8. `app` is the composition root. It may use any engine package, nothing may depend on it, and games and launchers reach the engine through it.
7. Only `app` and the `rendering`/`assets`/`audio`/`input` adapters touch LibGDX's low-level API (`Gdx.*`) directly. Game code and higher layers go through RedShell abstractions.

## 2. Package Map

```
com.lobsterchops.redshell
├── app
├── core
│   ├── config
│   ├── context
│   ├── lifecycle
│   └── time
├── util
│   ├── math
│   ├── timer
│   ├── random
│   └── collection
├── events
│   └── types
├── assets
│   ├── catalog
│   ├── loader
│   └── validation
├── input
│   ├── action
│   ├── binding
│   ├── context
│   └── device
├── entity
│   ├── component
│   └── prefab
├── ecs
│   ├── system
│   └── query
├── rendering
│   ├── camera
│   ├── sprite
│   ├── animation
│   ├── layer
│   └── viewport
├── physics
│   ├── collision
│   ├── layer
│   └── box2d
├── audio
│   └── catalog
├── scene
│   ├── stack
│   ├── transition
│   └── loading
├── save
│   ├── data
│   └── settings
├── ui
│   ├── hud
│   ├── menu
│   └── theme
└── debug
    ├── log
    ├── overlay
    ├── profiler
    └── inspector
```

---

## 3. Layer 1 — Foundation

### 3.1 `app`

**Purpose:** The only bridge between LibGDX's application lifecycle and RedShell's. Games never implement `ApplicationListener`.

| Class | Responsibility |
| --- | --- |
| `RedShellApplication` | Implements LibGDX's `ApplicationListener`. Creates the `Engine` in `create()` and forwards `render`, `resize`, `pause`, `resume` and `dispose`. |
| `LaunchConfig` | Window title, resolution, fullscreen, VSync, target FPS. Read by the desktop and web launchers (VSync and FPS are ignored on web). |
| `GameDefinition` | Interface a game implements to describe itself: name, `LaunchConfig`, optional `EngineConfig`/`RenderConfig` overrides `AssetCatalog`, default `BindingSet`, prefab registration, and `createInitialScene()` (a scene with assets is shown behind a loading screen). |

**Rules:** No gameplay logic. No second independent loop. The LibGDX callbacks are the loop.

### 3.2 `core`

**Purpose:** Engine bootstrap, shared contracts and shutdown.

| Class | Responsibility |
| --- | --- |
| `Engine` | Owns the system list, the context and the main update/render pass. |
| `EngineBuilder` | Assembles the engine and registers services in a fixed order. |

#### `core.config`

Immutable engine configuration (final classes with builders, plus enums; records are deferred per ADR-001).

- `EngineConfig` — global switches such as fixed timestep on/off and debug on/off
- `ConfigRegistry` — central lookup for named configuration objects

#### `core.context`

Dependency access for engine-managed code.

- `EngineContext` — holds the clock, configs and log, plus a typed service registry (`service(Class)`) filled by `EngineBuilder` and sealed after construction, so lower layers never import higher-layer services. Explicit and small. It is not a catch-all locator.

#### `core.lifecycle`

The lifecycle contract.

- `EngineSystem` — `initialize`, `fixedUpdate`, `update`, `render`, `resize`, `pause`, `resume`, `dispose` (all default methods)
- `RenderBracket` — `beginRender`/`endRender` hooks the engine calls around all system renders (implemented by `Renderer2D`) without depending on `rendering`
- `SystemRegistry` — ordered registration and ordered disposal (reverse of initialization)
- `Disposable` conventions and ownership notes for resources

#### `core.time`

- `GameClock` — frame delta, total time, time scale, pause
- `FixedStepAccumulator` — optional fixed-timestep driver with a cap on accumulated time after stalls

---

## 4. Layer 0 — Utilities

### 4.1 `util`

**Purpose:** Small, dependency-free helpers.

| Sub-package | Contents |
| --- | --- |
| `util.math` | Clamp, lerp, easing functions, angle helpers, small geometry helpers |
| `util.timer` | `Timer`, `Cooldown`, `DelayedAction` driven by the clock |
| `util.random` | Seedable random wrapper, weighted picks, range helpers |
| `util.collection` | Pools, ring buffers and small typed collections (building on LibGDX's `Array` and `Pool` where possible) |

**Rule:** Utilities hold no engine state and never reference other RedShell packages.

---

## 5. Layer 2 — Services

### 5.1 `events`

**Purpose:** Decoupled messaging between systems.

| Class | Responsibility |
| --- | --- |
| `EventBus` | Typed publish/subscribe with lifecycle-safe listener removal |
| `Event` | Marker base type |
| `Subscription` | Handle used to unsubscribe |

#### `events.types`

Engine-defined events: `SceneChangedEvent`, `WindowResizedEvent`, `AssetsLoadedEvent`, `PausedEvent`. Games define their own events in their own modules.

**Rules:** Events are immutable. Listeners are removed when their owner (scene or system) is disposed.

### 5.2 `assets`

**Purpose:** The single owner of shared resources. Wraps LibGDX's `AssetManager` rather than competing with it.

| Class | Responsibility |
| --- | --- |
| `AssetService` | Interface for loading, retrieving, unloading and progress reporting |
| `GdxAssetService` | Implementation backed by `AssetManager` |
| `AssetHandle<T>` | Typed reference to an asset |

#### `assets.catalog`

Declarative registry of every asset the game uses.

- `AssetCatalog` — maps logical IDs to file paths and types
- `AssetGroup` — named bundles (for example `MENU`, `LEVEL_1`) loaded and unloaded together

#### `assets.loader`

Custom `AssetLoader` implementations for formats LibGDX does not handle natively (kept to a minimum).

#### `assets.validation`

- `AssetValidator` — checks catalog entries against the file system before the game runs
- Reports missing files, duplicate IDs and unsupported types

**Rules:** Entities never dispose shared assets. Loading happens on the GL thread or through `AssetManager`'s async mechanism. Worker threads make no OpenGL calls.

### 5.3 `input`

**Purpose:** Logical input. Game code reads actions, never key codes.

| Class | Responsibility |
| --- | --- |
| `InputService` | Polls devices and exposes action state |
| `InputRouter` | Registers a single `InputMultiplexer` and enforces priority between UI and gameplay |

#### `input.action`

- `Action` — enum or interface for named actions (`MOVE_LEFT`, `JUMP`, `INTERACT`, `PAUSE`)
- `ActionState` — pressed, held, released and analog value

#### `input.binding`

- `Binding` — maps a physical input to an action
- `BindingSet` — a full profile that can be saved and rebound
- `BindingLoader` — reads bindings from configuration

#### `input.context`

Input contexts that switch which actions are active.

- `InputContext` — for example `GAMEPLAY`, `MENU`, `DIALOG`
- `ContextStack` — pushing a pause menu disables gameplay actions without touching game code

#### `input.device`

Adapters for keyboard, mouse, touch and gamepad. Isolated so LibGDX input details stay out of everything else.

---

## 6. Layer 3 — Runtime Systems

### 6.1 `entity`

**Purpose:** Identity and data. No behavior.

| Class | Responsibility |
| --- | --- |
| `Entity` | Identity plus its component set |
| `EntityId` | Stable identifier |
| `EntityTag` | Tags for lookup (for example `PLAYER`, `ENEMY`) |

#### `entity.component`

Plain data components shipped with the engine:

- `TransformComponent` — position, rotation, scale, optional parent
- `SpriteComponent` — region reference, tint, layer, flip
- `AnimationComponent` — current animation and playback state
- `ColliderComponent` — shape, layer mask, trigger flag
- `VelocityComponent` — linear velocity
- `LifetimeComponent` — auto-removal timer

**Rule:** Components are data holders. Logic belongs in systems.

#### `entity.prefab`

- `Prefab` — a reusable recipe for building an entity with preset components
- `PrefabRegistry` — lookup by ID

### 6.2 `ecs`

**Purpose:** Processing entities through systems. Evaluate Ashley before building every primitive. If Ashley is adopted, this package becomes a thin adapter around it.

| Class | Responsibility |
| --- | --- |
| `World` | Holds entities and systems for a scene |
| `EntityCommandBuffer` | Deferred add/remove so entities can be safely created or destroyed during system updates |

#### `ecs.system`

Base types and engine-provided systems:

- `EntitySystem` — base class with a priority for ordering
- `MovementSystem`, `AnimationSystem`, `LifetimeSystem`, `RenderSystem`, `CollisionSystem`

#### `ecs.query`

- `Query` — selects entities by required and excluded components
- `QueryCache` — avoids re-scanning every frame

**Rules:** Systems declare their ordering explicitly. Structural changes (add/remove entity or component) during iteration go through the command buffer.

### 6.3 `rendering`

**Purpose:** Clean 2D rendering on top of LibGDX's `SpriteBatch`.

| Class | Responsibility |
| --- | --- |
| `Renderer2D` | Owns the batch, begin/end, and draw ordering |
| `RenderContext` | Per-frame state passed to render systems |

#### `rendering.camera`

- `GameCamera` — wraps `OrthographicCamera`
- `CameraController` — follow target, zoom, bounds, shake
- `CoordinateConverter` — screen-to-world and world-to-screen

#### `rendering.sprite`

- `SpriteSheet`, `AtlasRegionRef` — region lookup through the asset catalog
- `Sprite2D` — lightweight draw descriptor

#### `rendering.animation`

- `Animation2D` — frames, timing, loop mode
- `AnimationController` — state-based animation switching
- `AnimationEvent` — hooks at specific frames

#### `rendering.layer`

- `RenderLayer` — named draw layers (for example `BACKGROUND`, `ACTORS`, `FOREGROUND`, `UI`)
- `LayerSorter` — ordering by layer, then by Y or z-index

#### `rendering.viewport`

- `ViewportPolicy` — fit, fill, stretch or extend, wrapping LibGDX viewports
- `ResizeHandler` — applies viewport changes on window resize

**Rule:** A custom shader pipeline is out of scope until a game needs it.

### 6.4 `physics`

**Purpose:** Collision first, full physics only if required.

| Class | Responsibility |
| --- | --- |
| `PhysicsService` | Interface used by the rest of the engine |
| `CollisionWorld` | Broad-phase and narrow-phase queries |

#### `physics.collision`

- `AABB`, `Circle` — shapes
- `CollisionResult` — contact info
- `Raycast` — line queries

#### `physics.layer`

- `CollisionLayer` and `CollisionMatrix` — which layers collide with which

#### `physics.box2d`

Optional adapter for Box2D. Nothing outside this package references Box2D types, so it can be removed or swapped.

### 6.5 `audio`

**Purpose:** Music and sound effects behind a service interface.

| Class | Responsibility |
| --- | --- |
| `AudioService` | Interface: play, stop, volume, mute |
| `GdxAudioService` | LibGDX-backed implementation |
| `SoundDefinition` | Immutable record describing a sound (asset ID, volume, pitch, category) |
| `AudioCategory` | `MUSIC`, `SFX`, `UI`, `AMBIENT` for independent volume control |

#### `audio.catalog`

- `AudioCatalog` — registry of every `SoundDefinition`, keyed by a sound enum or ID

**Rule:** Game code asks for a sound by logical ID. It never touches `Sound` or `Music` objects directly.

---

## 7. Layer 4 — Orchestration

### 7.1 `scene`

**Purpose:** Owns the lifecycle of a screen of gameplay or UI, and everything scoped to it.

| Class | Responsibility |
| --- | --- |
| `Scene` | Base class: `onEnter`, `onExit`, `update`, `render`, `resize`, `dispose`. Owns its `World`, scene-scoped assets and UI. |
| `SceneManager` | Creates, switches and destroys scenes. Fires `SceneChangedEvent`. |
| `SceneContext` | What a scene receives on entry: engine services plus transition data |

#### `scene.stack`

- `SceneStack` — push, pop, replace. Supports overlay scenes such as pause menus that render over a paused scene underneath.
- `SceneFlags` — whether a scene below it keeps updating or rendering

#### `scene.transition`

- `SceneTransition` — interface
- `FadeTransition`, `InstantTransition` — built-in transitions

#### `scene.loading`

- `LoadingScene` — shows progress while an `AssetGroup` loads
- `SceneLoader` — drives async asset loading before the next scene becomes active

**Rules:** Unloading a scene releases everything it owns: entities, scene assets, event subscriptions and UI. A scene never owns shared engine services.

### 7.2 `save`

**Purpose:** Persistence. Deferred until Phase 3.

#### `save.data`

- `SaveSlot`, `SaveData` — versioned save format
- `SaveService` — read, write, list and delete slots
- `Migration` — upgrades old save versions

#### `save.settings`

- `Settings` — volume, bindings, display options
- `SettingsService` — load and persist using LibGDX `Preferences` or files

---

## 8. Layer 5 — Presentation and Tooling

### 8.1 `ui`

**Purpose:** Game-facing UI built on Scene2D rather than a competing toolkit.

| Class | Responsibility |
| --- | --- |
| `UiService` | Owns the `Stage` and registers it with the input router |
| `UiScreen` | Base for a UI layout attached to a scene |

#### `ui.hud`

In-game overlays: health bars, counters, prompts.

#### `ui.menu`

Menu building blocks: main menu, pause menu, settings screen, dialogs.

#### `ui.theme`

- `UiTheme` — skin loading, fonts and shared styles

**Rule:** UI input has priority over gameplay input and consumes events it handles.

### 8.2 `debug`

**Purpose:** Make the engine observable. Removable without affecting behavior.

#### `debug.log`

- `Log` — thin facade with levels and tags
- `LogSink` — console, file or in-memory output
- Centralized exception reporting for engine-level failures

#### `debug.overlay`

- `DebugOverlay` — FPS, frame time, entity count, draw calls, memory
- Toggleable collision bounds and system list

#### `debug.profiler`

- `Profiler` — named timing scopes around system updates and render passes
- `FrameStats` — rolling averages

#### `debug.inspector`

- `EntityInspector` — read-only view of entities, components and tags at runtime. Editing comes after read-only works.

**Rule:** In-engine debug tooling uses RedShell's own UI. Any heavier tooling UI (for example ImGui) lives in a separate optional module that depends on the engine, never the other way round.

---

## 9. Typical Frame Flow

```
LibGDX render()
  └─ RedShellApplication.render()
       └─ Engine.frame()
            ├─ GameClock.tick()
            ├─ InputService.poll()
            ├─ [fixed steps] SceneStack.fixedUpdate()      (opt-in)
            ├─ SceneStack.update(delta)
            │     └─ World systems in priority order
            ├─ Renderer2D.begin()
            ├─ SceneStack.render()
            │     └─ RenderSystem → layers → batch
            ├─ UiService.draw()
            ├─ DebugOverlay.render()
            └─ Renderer2D.end()
```

## 10. Game Module Convention

Games live outside the engine and mirror the same discipline.

```
games/sandbox/src/main/java/com/lobsterchops/sandbox
├── SandboxGame.java          # implements GameDefinition
├── config
├── scenes                    # MenuScene, GameplayScene
├── entities                  # Prefabs and game-specific components
├── systems                   # Game-specific systems
├── events                    # Game-defined events
└── ui                        # HUD and menu layouts
```

The engine package is `com.lobsterchops.redshell.*`. Each game uses its own package under `com.lobsterchops.<gamename>`.

## 11. Naming and Style Conventions

- Interfaces describe the role (`AudioService`). Implementations name the backend (`GdxAudioService`).
- Immutable configuration uses final classes with builders, and enums (records are deferred until GWT supports them, see ADR-001).
- Catalog classes (`AssetCatalog`, `AudioCatalog`) hold declarative registries. They contain no loading logic.
- Name methods by intent. Break long methods into small, intent-named helpers.
- No magic numbers in systems. Tunables live in config classes.
- Every package has a short `package-info.java` stating its purpose and its allowed dependencies.

## 12. Build Order for Packages

| Phase | Packages to establish |
| --- | --- |
| 1 | `app`, `core`, `util`, `debug.log`, minimal `rendering` and `rendering.camera` |
| 2 | `scene`, `entity`, `ecs`, `rendering.sprite`, `rendering.animation`, `input`, `assets` |
| 3 | `physics`, `audio`, `ui`, `events`, `save` |
| 4 | `debug.overlay`, `debug.profiler`, `debug.inspector`, `assets.validation`, example modules |

Create the empty packages and their `package-info.java` early so boundaries exist from day one. Fill each in only when a game needs it.

## Phase 2 notes (as built)

- Scenes own their World, event subscriptions, input context and one asset-group reference; all are released when the scene is disposed. Scenes are single use.
- Only AssetService loads/unloads assets. LoadingScene loads a target scene's group, hands ownership to it, then replaces itself.
- PrefabRegistry lives in entity and only stores prefabs; spawning goes through World.spawn or EntityCommandBuffer.
- Structural ECS changes while a system runs throw IllegalStateException; use world.commands().
- SceneChangedEvent uses scene names so events does not depend on scene.

## Phase 3 notes (as built)

- Scenes reach UiService, AudioService and SettingsService through context().engine().service(...) from game code, since scene (layer 4) must not import ui (layer 5).
- Physics is plain AABB/circle with a uniform-grid broad phase; CollisionSystem (priority 150) rebuilds the world each frame, resolves solids and publishes ContactEvents.
- Music is owned by GdxAudioService (asset group udio.music.<id>), not scene groups, so it survives loading transitions.
- Timers use the engine's scaled delta, so they respect time scale and pause.

## Phase 4 notes (as built)

- Profiling uses a `ScopeListener` (core.lifecycle) that SystemRegistry and World call around each system; `debug.profiler.Profiler` implements it. No debug type is referenced by engine code below layer 5.
- The debug overlay owns its own `InputService`, so its keys work in any input context and do not appear in game bindings.
- Any code drawing with another batch or ShapeRenderer must call `Renderer2D.flush()` first.
- `debug.log.*` may depend only on util and the JDK, so the file sink lives in `debug.filelog` (desktop only).
- Tools that need `java.io` (`ValidateAssets`, `ExampleLauncher`) live in `redshell-desktop`.