# Examples

Small, single-purpose programs in the `examples` module (desktop only). Run one with:
```
gradlew :redshell-desktop:runExample -Pexample=<name>
```
| Name | Shows | Controls |
|------|-------|----------|
| `sprite` | tiles, sprite-sheet animation, tint and rotation | none |
| `ecs` | custom component and systems, command buffer | none (coins expire) |
| `scene-stack` | pushing and popping an overlay scene | SPACE |
| `input-actions` | action bindings | WASD / arrows, SPACE |
| `collision` | solid walls and a trigger zone (see log) | WASD / arrows, F4 |
| `audio` | sound effects, music, master volume | SPACE, E, W/S |

All use the sandbox's art and sounds, 640x360 virtual resolution, and the debug overlay (F3).
Each is one file; `ExampleGame` holds the shared setup.