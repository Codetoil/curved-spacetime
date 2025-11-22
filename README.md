[![Qodana](https://github.com/Codetoil/curved-spacetime/actions/workflows/qodana_code_quality.yml/badge.svg)](https://github.com/Codetoil/curved-spacetime/actions/workflows/qodana_code_quality.yml)

# Curved Spacetime

Curved Spacetime is a work-in-progress computer program that simulates
General Relativity whose goal is that it should contain a UI normal people
can understand while simultaneously being powerful enough for Scientists
to use.

A Graphics Card with Ray Tracing and Vulkan 1.3 support is required to run this application.

There are three main variants of this software:

- The Quilt Variant, which runs Quilt Loader and is the most extendable. Requires Java 25 to be installed.
- The Closed-World Jar Variant, which is faster but less extendable. Requires Java 25 to be installed.
- The Closed-World Native Variant, which is the fastest but least extendable. Does not require Java 25 to be installed,
  but less Operating Systems are supported.

The Java Variants (Quilt Variant and Closed-World Jar Variant) support the following Operating Systems and
Architectures, assuming you can get JDK 25 for that version (Not all are tested):

- Linux on 64-Bit x86 (x86_64/AMD64)
- Linux on 32-Bit ARM (ARM32)
- Linux on 64-Bit ARM (AArch64/ARM64)
- Linux on 64-Bit Little-Endian PowerPC (PPC64LE)
- Linux on 64-Bit RISC-V (RISCV64)
- FreeBSD on 64-Bit x86 (x86_64/AMD64)
- macOS on 64-Bit x86 (x86_64/AMD64)
- macOS on 64-Bit ARM (AArch64/ARM64)
- Windows on 64-Bit x86 (x86_64/AMD64)
- Windows on 32-Bit x86 (x86)
- Windows on 64-Bit ARM (AArch64/ARM64)

The Native Variants (Closed-World Native Variants) support the following Operating Systems and Architectures:

- Linux on 64-Bit x86 (x86_64/AMD64)
- Linux on 64-Bit ARM (AArch64/ARM64)
- macOS on 64-Bit x86 (x86_64/AMD64) (Must be built from source)
- macOS on 64-Bit ARM (AArch64/ARM64)
- Windows on 64-Bit x86 (x86_64/AMD64)

## Build

To build the application, you need a copy of JDK 8, JDK 25, and GraalVM CE 25, along with the Vulkan SDK Installed.
Then run `./gradlew build nativeCompile`.
