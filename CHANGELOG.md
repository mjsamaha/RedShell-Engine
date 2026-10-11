# Changelog

## 0.1.0

First release of RedShell. Built and verified on desktop (LWJGL3) and compiled for the browser (GWT).

### Engine
- Core: engine loop with optional fixed timestep, system registry, service registry, logging with pluggable sinks.
- Scenes: scene stack with push/pop/replace/load, fade transitions, input contexts, asset groups per scene.
- ECS: entities, components, queries, systems with priorities, deferred changes through a command buffer, prefabs.
- Rendering: viewport policies, camera and camera controller, sprite sheets, animation, layered sprite drawing.
- Input: actions, bindings, contexts, keyboard and mouse devices, runtime rebinding.
- Assets: catalog with groups, reference-counted loading and unloading, validation (missing files, wrong types, duplicate files).
- Physics: box and circle colliders, layer matrix, triggers, raycast and overlap queries.
- Audio: sound and music with a mixer (master, categories, mute) and fades.
- UI: menus, HUD elements, settings screen on scene2d with a shared theme.
- Save and settings: versioned saves with migrations, settings persisted through a platform-neutral store.
- Debug tools: overlay (FPS, frame time, entities, draw calls, memory, system timings, collider bounds, entity inspector), profiler, memory and file logs.

### Games, examples and tooling
- `games:sandbox` and `games:arena`: two different games on the same engine, both launched from the same desktop and web launchers.
- `examples`: six focused examples.
- Gradle tasks: `validateAssets` (part of `check`), `leakCheck` (needs a GL context), `runExample`, `dist` and `distZip` for the web build, `jar` for the desktop build.
- Architecture rules (ArchUnit) enforce layering and GWT compatibility on every build.

### Known gaps
- No swept collision (fast entities can tunnel).
- No gamepad adapter, no Box2D, no ImGui tooling.
- Native desktop bundles (no JRE) are not set up.
- Audio uses WAV in the sample games; browsers prefer OGG or MP3.