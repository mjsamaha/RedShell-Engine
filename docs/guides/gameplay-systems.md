# Physics, audio, UI and saves

- **Physics:** `ColliderComponent` (box or circle, `trigger`, `immovable`, layer) + `CollisionSystem` with a
  `CollisionMatrix` saying which layers touch. Solid contacts are pushed apart; triggers publish
  `TriggerEntered` / `TriggerExited`. There is no swept collision yet: keep speed x frame time below collider size.
- **Audio:** `AudioService.playSound(id)`, `playMusic(id, fade)`, `mixer()` for master/category volume and mute.
  Use OGG or MP3 for web; WAV is for desktop tests.
- **UI:** `UiService` builds on scene2d with a shared `UiTheme`; the font asset must be in the scene's group.
- **Saves and settings:** `SaveService` / `SettingsService` store data through a platform-neutral backend
  (files on desktop, browser storage on web). Settings changes publish an event.