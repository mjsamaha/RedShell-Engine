# Getting started

## Build and run
```
gradlew build                      # compile, test, validate assets, GWT compile
gradlew :redshell-desktop:run      # the sandbox game on desktop (add -Pgame=arena for the second game)
gradlew :redshell-html:dist        # the sandbox in a browser (redshell-html/build/dist)
```
Needs JDK 17+ (JDK 21 for the web build).

## Your first game
A game is one class implementing `GameDefinition` in its own module (copy `games/sandbox`):

- `name()`, `launchConfig()`, `engineConfig()`, `renderConfig()`: window and virtual resolution.
- `assetCatalog()`: every file the game uses, by ID. `audioCatalog()`: sounds and music by ID.
- `defaultBindings()`: input actions to keys.
- `createInitialScene()`: the first `Scene`.

The second game, `games/arena`, shows a different genre built the same way: a twin-stick arena shooter with its own scenes, systems and assets.

Launch it with `RedShellDesktop.launch(game)`; the web launcher does the same through GWT.

## Rules that keep the web build working
Engine and game code compile with GWT: no reflection, threads, `java.io.File`, `java.nio.file`,
`String.format`, records or `.clone()` on arrays. The build enforces this with ArchUnit tests.