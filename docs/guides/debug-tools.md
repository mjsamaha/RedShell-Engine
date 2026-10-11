# Debug tools and validation

Enable with `EngineConfig.builder().debug(true)`. Overlay keys (own input service, independent of game bindings):

| Key | Does |
|-----|------|
| F3 | overlay: FPS, frame time, entities, draw calls, memory, recent log |
| F4 | collider bounds |
| F5 | per-system timings (ms, rolling average and max) |
| F6 | entity inspector panel |
| F7 / F8 | select next / previous entity |

Text needs the UI font loaded (`uiFontAssetId()` in an asset group the scene uses); without it only collider bounds draw.
The profiler is a `Profiler` service, usable from tests. The inspector is read-only.

**Logging:** `context().log()` writes to the console and an in-memory ring buffer (shown in the overlay).
On desktop, `GameDefinition.logFileName()` also writes to `~/.redshell/<name>`.

**Asset validation:** runs at startup in debug builds and with `gradlew :redshell-desktop:validateAssets
[-Pgame=<GameDefinition class>]`, which is part of `check` (and CI). It reports missing files, unsupported
file types (audio must be ogg/mp3/wav) and one file registered under two IDs.