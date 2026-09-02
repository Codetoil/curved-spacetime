# Contributing to Curved Spacetime

Curved Spacetime is a modular simulator for General Relativity. Java 25, Gradle (Kotlin DSL),
JPMS, Quilt loader.

This document covers conventions and day-to-day mechanics. For the normative contract that
modules and loaders must satisfy — entrypoint naming, the dependency handshake, the
configuration format — see the
[Module System Specification](https://codetoil.io/curved-spacetime/). Where this document and
the specification disagree, the specification wins.

**The specification describes how the module system *should* work, and the existing code does
not yet conform to it.** Module directory names, the engine class name, and the derived
entrypoint and configuration names have all changed. Follow the specification for new work; when
it disagrees with the sibling module you are copying from, the sibling is the one that is
wrong.

## Layout

Multi-module Gradle build. Every module lives in `<name>-module/`.

API and implementation modules pair up like this:

| Role | Directory | JPMS module |
| --- | --- | --- |
| API | `curved-spacetime-render-module` | `io.codetoil.curved_spacetime.render` |
| Implementation | `curved-spacetime-render-glfw-module` | `io.codetoil.curved_spacetime.render.glfw` |

The implementation's package is **nested under** the API's package, and the directory name
mirrors that order segment for segment — API first, implementation last, `-module` at the end.
`curved-spacetime-render-vulkan-glfw-module` therefore maps to
`io.codetoil.curved_spacetime.render.vulkan_glfw`.

The directories in the tree today are still ordered the other way round
(`curved-spacetime-glfw-render-module`) and do not yet match.

## Adding a module

Six files, plus a `settings.gradle.kts` edit. Copy an existing sibling rather than writing from
scratch — `curved-spacetime-render-glfw-module` is a reference for an implementation of the corresponding
API `curved-spacetime-render-module`. A proper SDK will be made later.

1. **`build.gradle.kts`** — plugins: java, java-library, javadoc-links, maven-publish.
   `api(project(":curved-spacetime-main-module"))`, plus the API module.
   **Leave the jar's `destinationDirectory` alone.** Jar output stays at the Gradle default,
   `build/libs`. To place the module in a distribution, add a row to `quiltDistribution` (or the
   `closedWorldDistribution`) in the root `build.gradle.kts` — naming the module, its subdirectory
   within the archive, and the task producing the artifact. The root build file explains why
   redirecting a jar task instead makes the archive a task *output*, which Gradle then empties
   from under the IDE.
2. **`src/main/java/module-info.java`** — requires `io.codetoil.curved_spacetime`,
   `io.codetoil.curved_spacetime.loader`, `java.logging`, plus the API module. Export the
   module's package and its `.entrypoint` subpackage.
3. **`<X>ModuleEntrypoint implements ModuleInitializer`** — the `main` entrypoint, where `<X>` is
   the module key in `PascalCase` with `-module` dropped. Sets its logger level from
   `CurvedSpacetimeMainModuleEngine.getInstance().mainModuleConfig.getLoggerLevel()`, loads its
   config, then calls `CurvedSpacetimeMainModuleEngine.callDependents("<key_>_dependent", …)`,
   where `<key_>` is the module key with every `-` replaced by `_`.
4. **`<Module 1>ModuleDependent<Module 2>ModuleEntrypoint implements <Module 1>ModuleDependentModuleInitializer`**
   — one per module you depend on, where module 1 is the depended-on module and module 2 is this
   one. Looks itself up via `getEntrypoints("main", ModuleInitializer.class)` and hands the
   received entrypoint to its own `getDependencyModuleTransferQueue().transfer(…)`.
5. **`<X>ModuleConfig implements ModuleConfig`** — `Properties`-backed, with `load()`, `save()`,
   and `isDirty()`; the file is `config/<key>.config`.
6. **`entrypoint/<X>ModuleDependentModuleInitializer`** — required of *every* module, not only
   those something already depends on, so that any module can be depended upon later.

Resources: `quilt.mod.json` — required of every module, with `id`, `group`, `entrypoints`, and
`depends` — and `<module-name>.mixins.json`.

Then add **both** an `include(…)` and a `project(…).name = …` line to `settings.gradle.kts`.

A module that declares dependencies blocks until it receives them, so its dependency count, its
`quilt.mod.json` entrypoints, and its `depends` array must all agree. A mismatch hangs startup
rather than failing loudly. The specification covers this in detail.

## Code style

- **Tabs** for indentation.
- **Allman braces** — opening brace on its own line, including for classes and methods.
- `this.` prefix on instance field access.
- Static constants referenced through the class name (`Foo.CONSTANT`, not bare `CONSTANT`).
- GPL-3.0-or-later header as a javadoc block at the top of every file, `<br>`-formatted. Copy
  it verbatim from a neighbouring file.

### Javadoc

Follow the [Documentation Comment Specification][javadoc-spec]. The aggregated Javadoc is
published to the project site on every release, so it is a shipped artifact, not just inline
commentary.

- Every public type and method gets a doc comment.
- The first sentence is a summary **fragment** ending in a period — it is what appears in the
  generated summary tables.
- Third-person declarative, not imperative: "Returns the logger for this module.", not "Return
  the logger for this module."
- Block tags in the order `@param`, `@return`, `@throws`. No `@return` on a `void` method.
- Block tag descriptions are lowercase phrases with no trailing period:
  `@param logger the logger to write module diagnostics to`.
- `{@code …}` for identifiers, keywords, and literals; `{@link …}` for cross-references.
- `<p>` to separate paragraphs.

The GPL header is the sole exception — it stays `<br>`-formatted and verbatim.

[javadoc-spec]: https://docs.oracle.com/en/java/javase/25/docs/specs/javadoc/doc-comment-spec.html

## Building

See [README.md](README.md) for toolchain requirements and the build commands. In short:
`./gradlew assemble` for the jar variants, `./gradlew nativeCompile` for the native variant.

## The specification

The Module System Specification lives at `specs/module-system.html` in this repository and is
published per version alongside the API documentation, at `specs/<version>/`. The publishing
cadence tracks the project's [SemVer 2.0.0](https://semver.org/spec/v2.0.0.html) maturity:

| Project stage | A new specification version is cut |
| --- | --- |
| Before `0.1.0` (current) | per build |
| `0.x.y` | per minor version |
| `1.0.0` and later | per major version |

`0.1.0` will be released once the project is functional, even if incomplete; the cadence drops
to per minor version from that point.

Changes to the module system — entrypoint naming, the handshake, the config contract — should
update `specs/module-system.html` in the same change that alters the behaviour.

### Format states status

A specification's file format records whether it is binding, so changing the format changes the
document's standing:

| Format | Status |
| --- | --- |
| HTML, styled with `spec.css` | **Normative.** Implementations must conform; where the code disagrees, the code is wrong. |
| Markdown | **Draft.** A design exploration, binding on nothing. |

`specs/simulation.md` is a draft deliberately. It is published as-is, so a browser shows Markdown
source — headings, table rules and unrendered LaTeX — rather than a finished page. **That
provisional appearance is intended and is not a defect to fix.** Converting a draft to HTML is
the act of promoting it to normative, so do that as a considered decision about the document's
status, never as a presentation improvement.

## Licence

By contributing you agree that your contributions are licensed under GPL-3.0-or-later, matching
the project.
