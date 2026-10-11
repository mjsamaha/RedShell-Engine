# Scenes, ECS and rendering

## Scenes
A `Scene` declares its asset group (`assets()`), then overrides `onEnter`, `onUpdate`, `onRender`, `onExit`.
`context().scenes()` pushes, pops and replaces scenes with an optional transition. Flags say whether a scene
blocks the ones below it: `OPAQUE`, `TRANSPARENT`, `OVERLAY` (visible but frozen below). Scenes are single use.
Lower layers are reached through `context().engine().service(X.class)`.

## ECS
Every scene owns a `world()`. Entities hold components (data). Systems (behaviour) run in priority order
(`SystemPriority`: INPUT, MOVEMENT, ANIMATION, LIFETIME, CAMERA, RENDER). Query with `Query.all(...)`.
Never create or destroy entities while iterating: use `world.commands()`. See `EcsExample`.

## Rendering
`Renderer2D` owns the batch and camera; the viewport policy (FIT, FILL, STRETCH, ...) comes from `RenderConfig`.
Use `SpriteComponent` + `RenderSystem` for entities, `renderer.fillRect` for quick shapes, `SpriteSheet` and
`AnimationController` for animation. If you draw with another batch, call `renderer.flush()` first.