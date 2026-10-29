[![Qodana](https://github.com/Codetoil/curved-spacetime/actions/workflows/qodana_code_quality.yml/badge.svg)](https://github.com/Codetoil/curved-spacetime/actions/workflows/qodana_code_quality.yml)

# Curved Spacetime

Curved Spacetime is a work-in-progress computer program that simulates
General Relativity whose goal is that it should contain a UI normal people
can understand while simultaneously being powerful enough for Scientists
to use.

A Graphics Card with Ray Tracing and Vulkan 1.3 support is required to run this application.

Curved Spacetime supports the following Operating Systems and architectures,
 assuming you can get JDK 25 for that version (Not all are tested):

- FreeBSD on 64-Bit x86 (x86_64/AMD64)
- Linux on 64-Bit x86 (x86_64/AMD64)
- Linux on 32-Bit ARM (ARM32) (Requires custom JDK)
- Linux on 64-Bit ARM (AArch64/ARM64)
- Linux on 64-Bit Little-Endian PowerPC (PPC64LE)
- Linux on 64-Bit RISC-V (RISCV64)
- macOS on 64-Bit x86 (x86_64/AMD64)
- macOS on 64-Bit ARM (AArch64/ARM64)
- Windows on 64-Bit x86 (x86_64/AMD64)
- Windows on 32-Bit x86 (x86) (Requires custom JDK)
- Windows on 64-Bit ARM (AArch64/ARM64)

## Documentation

Published per release at <https://codetoil.io/curved-spacetime/>, which always links the latest
version:

- **Module System Specification** — the normative contract for modules and loaders: naming,
  entrypoints, the dependency handshake, and the configuration format. Source lives in this
  repository at [`specs/module-system.html`](specs/module-system.html).
- **Engine Specification** — the normative contract for engines: how an engine drives module
  initialization, and when callbacks' `init`, `loop` and `clean` run, on which thread, and what a
  failure does. Source lives in this repository at [`specs/engine.html`](specs/engine.html).
- **Javadocs** — aggregated Javadoc for every module.

The intended direction for the simulation layer is sketched in
[`specs/curved-spacetime.md`](specs/curved-spacetime.md). That one is an **exploratory draft, not a
specification** — nothing in it is implemented yet, and parts of it are still disputed. It is
included so that the architecture built around it reads as deliberate rather than accidental.

Contributors should start with [CONTRIBUTING.md](CONTRIBUTING.md), which covers module layout,
adding a module, code style, and the Javadoc conventions.

## Building and Distribution

To build the application, you need a copy of JDK 8 and JDK 25, along with the Vulkan SDK installed. Then run
`./gradlew build`.

If you plan to distribute binaries, you will need to distribute the
[Corresponding Source](https://www.gnu.org/licenses/gpl-faq.html#DistributeExtendedBinary) with it in accordance with
the GPLv3 or Later License.
And easy way to pack it is to run
`git archive --format=tar.gz -o ./source.tar.gz HEAD` within the repository. It will automatically exclude certain
unwanted folders like the build directories.