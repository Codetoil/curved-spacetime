# Curved Spacetime

Modular simulator for General Relativity. Java 25, Gradle (Kotlin DSL), JPMS, Quilt loader.

## Read these first

Project conventions are **not** duplicated here — they live where human contributors can find
them. Read the relevant one before writing code; do not infer conventions from surrounding
files alone.

| What you need | Where it lives |
| --- | --- |
| Module layout, adding a module, code style, Javadoc rules | [`CONTRIBUTING.md`](CONTRIBUTING.md) |
| Normative module/loader contract — entrypoint naming, the dependency handshake, config format | [`specs/module-system.html`](specs/module-system.html) |
| Toolchain requirements, build commands, supported platforms | [`README.md`](README.md) |
| Intended direction for the simulation layer — **exploratory draft, not normative** | [`specs/simulation.md`](specs/simulation.md) |

The specification is authoritative. Where `CONTRIBUTING.md` and the specification disagree, the
specification wins, and the disagreement is a bug worth reporting.

When you change module-system behaviour — entrypoint naming, the handshake, the config
contract — update `specs/module-system.html` in the same change.

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

## Repository notes

- Javadoc and specifications are published per version to
  <https://codetoil.io/curved-spacetime/>.
