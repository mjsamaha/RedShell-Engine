package com.lobsterchops.redshell;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Encodes the layer diagram and the dependency rules from docs/architecture.md section 1,
 * plus the GWT compatibility bans from docs/implementation-plan.md section 3.1.
 */
class LayeringRulesTest {

    private static final String ROOT = "com.lobsterchops.redshell";

    private static final String[] LAYER_0 = {"util"};
    private static final String[] LAYER_1 = {"core"};
    private static final String[] LAYER_2 = {"assets", "input", "events"};
    private static final String[] LAYER_3 = {"ecs", "entity", "rendering", "physics", "audio"};
    private static final String[] LAYER_4 = {"scene", "save"};
    // debug.log is a cross-cutting facility usable from every layer, so it is not part of layer 5.
    private static final String[] LAYER_5 = {"ui", "debug.overlay", "debug.profiler", "debug.inspector"};

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(ROOT);
    }

    @Test
    void utilDependsOnNoOtherRedShellPackage() {
        ArchRule rule = noClasses().that().resideInAPackage(pkg("util"))
                .should().dependOnClassesThat().resideInAnyPackage(allExcept("util"))
                .allowEmptyShould(true);
        rule.check(classes);
    }

    @Test
    void utilOnlyUsesJdkAndLibgdxMathAndUtils() {
        classes().that().resideInAPackage(pkg("util"))
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "com.badlogic.gdx.math..", "com.badlogic.gdx.utils..", pkg("util"))
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void debugLogOnlyDependsOnUtilAndJdk() {
        classes().that().resideInAPackage(pkg("debug.log"))
                .should().onlyDependOnClassesThat().resideInAnyPackage("java..", pkg("util"), pkg("debug.log"))
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void nothingDependsOnTheCompositionRoot() {
        noClasses().that().resideOutsideOfPackage(pkg("app"))
                .should().dependOnClassesThat().resideInAPackage(pkg("app"))
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void gdxStaticsAreNotUsedByPlatformIndependentPackages() {
        noClasses().that().resideInAnyPackage(pkg("core"), pkg("util"), pkg("debug.log"), pkg("events"),
                pkg("entity"), pkg("ecs"), pkg("scene"))
                .should().dependOnClassesThat().haveFullyQualifiedName("com.badlogic.gdx.Gdx")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void layersOnlyDependDownward() {
        assertNoUpwardDependencies(LAYER_0, above(LAYER_1, LAYER_2, LAYER_3, LAYER_4, LAYER_5));
        assertNoUpwardDependencies(LAYER_1, above(LAYER_2, LAYER_3, LAYER_4, LAYER_5));
        assertNoUpwardDependencies(LAYER_2, above(LAYER_3, LAYER_4, LAYER_5));
        assertNoUpwardDependencies(LAYER_3, above(LAYER_4, LAYER_5));
        assertNoUpwardDependencies(LAYER_4, above(LAYER_5));
    }

    @Test
    void coreDoesNotDependOnApp() {
        noClasses().that().resideInAPackage(pkg("core"))
                .should().dependOnClassesThat().resideInAPackage(pkg("app"))
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void engineNeverDependsOnGamesOrLaunchers() {
        noClasses().that().resideInAPackage(ROOT + "..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.lobsterchops.sandbox..",
                        "com.lobsterchops.redshell.desktop..",
                        "com.lobsterchops.redshell.html..",
                        "..games..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void box2dIsOnlyReferencedFromItsAdapterPackage() {
        noClasses().that().resideOutsideOfPackage(pkg("physics.box2d"))
                .should().dependOnClassesThat().resideInAPackage("com.badlogic.gdx.physics.box2d..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void engineSourcesStayGwtCompatible() {
        noClasses().that().resideInAPackage(ROOT + "..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "java.util.concurrent..",
                        "java.nio.file..",
                        "java.lang.reflect..")
                .orShould().dependOnClassesThat().haveFullyQualifiedName("java.io.File")
                .orShould().dependOnClassesThat().haveFullyQualifiedName("java.lang.Thread")
                .allowEmptyShould(true)
                .check(classes);
    }

    private static void assertNoUpwardDependencies(String[] layer, String[] forbidden) {
        noClasses().that().resideInAnyPackage(pkgs(layer))
                .should().dependOnClassesThat().resideInAnyPackage(forbidden)
                .allowEmptyShould(true)
                .check(classes);
    }

    private static String pkg(String name) {
        return ROOT + "." + name + "..";
    }

    private static String[] pkgs(String[] names) {
        String[] result = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            result[i] = pkg(names[i]);
        }
        return result;
    }

    private static String[] above(String[]... layers) {
        int size = 0;
        for (String[] layer : layers) {
            size += layer.length;
        }
        String[] result = new String[size];
        int index = 0;
        for (String[] layer : layers) {
            for (String name : layer) {
                result[index++] = pkg(name);
            }
        }
        return result;
    }

    private static String[] allExcept(String name) {
        String[][] groups = {LAYER_0, LAYER_1, LAYER_2, LAYER_3, LAYER_4, LAYER_5};
        java.util.List<String> result = new java.util.ArrayList<>();
        for (String[] group : groups) {
            for (String candidate : group) {
                if (!candidate.equals(name)) {
                    result.add(pkg(candidate));
                }
            }
        }
        return result.toArray(new String[0]);
    }
}