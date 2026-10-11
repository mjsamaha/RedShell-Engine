# RedShell Engine

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and a main class extending `Game` that sets the first screen.

## Platforms

- `redshell-engine`: The engine library (`com.lobsterchops.redshell`). No game code; must stay GWT-compatible.
- `redshell-desktop`: Desktop launcher using LWJGL3.
- `redshell-html`: Web launcher using GWT and WebGL. Supports only Java projects.
- `games:sandbox`: First proof-of-concept game, built on the public engine API.

See `docs/overview.md`, `docs/architecture.md` and `docs/implementation-plan.md`.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `redshell-html:dist`: compiles GWT sources. The compiled application can be found at `redshell-html/build/dist`: you can use any HTTP server to deploy it.
- `redshell-html:superDev`: compiles GWT sources and runs the application in SuperDev mode. It will be available at [localhost:8080/html](http://localhost:8080/html). Use only during development.
- `idea`: generates IntelliJ project data.
- `redshell-desktop:jar`: builds application's runnable jar, which can be found at `redshell-desktop/build/libs`.
- `redshell-desktop:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `redshell-engine:clean` removes `build` folder only from the `redshell-engine` project.
