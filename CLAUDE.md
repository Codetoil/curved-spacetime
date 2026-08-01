# Curved Spacetime

Modular simulator for General Relativity. Java 25, Gradle (Kotlin DSL), JPMS, Quilt loader.

## Layout

Multi-module Gradle build. Every module is `curved-spacetime-<name>-module/`.

API/implementation pairs follow this convention:

- API module: `curved-spacetime-render-module` → JPMS `io.codetoil.curved_spacetime.render`
- Implementation: `curved-spacetime-glfw-render-module` → JPMS `io.codetoil.curved_spacetime.render.glfw`

The implementation's package is **nested under** the API's package. Directory name is
`<impl>-<api>-module`.

## Adding a module

Six files, plus a `settings.gradle.kts` edit. Copy an existing sibling rather than writing
from scratch — `curved-spacetime-glfw-render-module` is the reference for an implementation
of an API, `curved-spacetime-cli-module` for a standalone one.

1. `build.gradle.kts` — plugins: java, java-library, javadoc-links, maven-publish.
   `api(project(":curved-spacetime-main-module"))` plus the API module if implementing one.
   Jar `destinationDirectory` goes to `$rootDir/archive-quilt/<kind>-modules`.
2. `../curved-spacetime-state-gauge/curved-spacetime-operator-algebra-simulator-module/src/main/java/module-info.java` — requires `io.codetoil.curved_spacetime`,
   `io.codetoil.curved_spacetime.loader`, `java.logging`, plus the API module. Export the
   module's package and its `.entrypoint` subpackage.
3. `<X>ModuleEntrypoint implements ModuleInitializer` — the `main` entrypoint. Sets logger
   level from `MainModuleEngine.getInstance().mainModuleConfig.getLoggerLevel()`, loads its
   config, then `MainModuleEngine.callDependents("<x>_module_dependent", ...)`.
4. `<Api>ModuleDependent<X>ModuleEntrypoint implements <Api>ModuleDependentModuleInitializer`
   — only for implementations. Looks itself up via
   `getEntrypoints("main", ModuleInitializer.class)` and hands the API entrypoint to its own
   `getDependencyModuleTransferQueue().transfer(...)`.
5. `<X>ModuleConfig implements ModuleConfig` — `Properties`-backed, `load()` / `save()` /
   `isDirty()`, file under `config/`.
6. `entrypoint/<X>ModuleDependentModuleInitializer` — so other modules can depend on this one.

Resources: `quilt.mod.json` (entrypoints, depends) and `<module-name>.mixins.json`.

Then add both an `include(...)` and a `project(...).name = ...` line to `settings.gradle.kts`.

## Code style

- **Tabs** for indentation.
- **Allman braces** — opening brace on its own line, including for classes and methods.
- `this.` prefix on instance field access.
- Static constants referenced through the class name (`Foo.CONSTANT`, not bare `CONSTANT`).
- GPL-3.0-or-later header as a javadoc block at the top of every file, `<br>`-formatted.
  Copy it verbatim from a neighbouring file.
- Javadoc on public types and methods, with `@param` / `@return`.

## Verifying changes

Gradle needs network access for plugin resolution. When that isn't available, the whole
JPMS stack compiles with plain `javac` in dependency order — the only external requirement
is `java.logging`:

    javac -d /tmp/mods/io.codetoil.curved_spacetime.loader \
        $(find curved-spacetime-loader-module/src/main/java -name '*.java')
    javac -d /tmp/mods/io.codetoil.curved_spacetime --module-path /tmp/mods \
        $(find curved-spacetime-main-module/src/main/java -name '*.java')
    # then simulator, then the module under test

Run code against the result with
`java --module-path /tmp/mods --add-modules <module> -cp . <MainClass>`.

Prefer this over reasoning about whether something compiles.