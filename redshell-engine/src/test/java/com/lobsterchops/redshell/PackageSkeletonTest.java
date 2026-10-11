package com.lobsterchops.redshell;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies that every package in docs/architecture.md section 2 exists with a package-info.java. */
class PackageSkeletonTest {

    private static final List<String> PACKAGES = Arrays.asList(
            "app",
            "core", "core.config", "core.context", "core.lifecycle", "core.time",
            "util", "util.math", "util.timer", "util.random", "util.collection",
            "events", "events.types",
            "assets", "assets.catalog", "assets.loader", "assets.validation",
            "input", "input.action", "input.binding", "input.context", "input.device",
            "entity", "entity.component", "entity.prefab",
            "ecs", "ecs.system", "ecs.query",
            "rendering", "rendering.camera", "rendering.sprite", "rendering.animation",
            "rendering.layer", "rendering.viewport",
            "physics", "physics.collision", "physics.layer", "physics.box2d",
            "audio", "audio.catalog",
            "scene", "scene.stack", "scene.transition", "scene.loading",
            "save", "save.data", "save.settings",
            "ui", "ui.hud", "ui.menu", "ui.theme",
            "debug", "debug.log", "debug.overlay", "debug.profiler", "debug.inspector");

    @Test
    void everyDocumentedPackageHasPackageInfo() {
        Path root = Paths.get("src", "main", "java", "com", "lobsterchops", "redshell");
        List<String> missing = new ArrayList<>();
        for (String name : PACKAGES) {
            Path info = root.resolve(name.replace('.', '/')).resolve("package-info.java");
            if (!Files.isRegularFile(info)) {
                missing.add(name);
            }
        }
        assertTrue(missing.isEmpty(), "Missing package-info.java for: " + missing);
    }
}