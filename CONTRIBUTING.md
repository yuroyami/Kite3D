# Contributing to Kite3D

Kite3D is a from-scratch port of [three.js](https://github.com/mrdoob/three.js)
(`r184`) to Kotlin Multiplatform. Almost all contributions right now are **ports**:
turning a three.js source file into common Kotlin.

## Before you port a file

Read **[PORTING.md](PORTING.md)** — the binding dialect. It covers structure,
the JS→Kotlin language mapping, cross-layer seams, and the trap catalogue (the
subtle `clamp`/`Math.round`/int32/`-0.0` differences that silently corrupt a port).

Then check **[port-ledger.yaml](port-ledger.yaml)** for the file's status and
dependency order. Port in dependency order; a class and its upstream test suite
land in the same commit.

## Building and testing

```bash
# Compile the common core for the JVM (fast inner loop)
./gradlew :kite3d:compileKotlinJvm

# Run the tests on a given engine
./gradlew :kite3d:jvmTest          # JVM
./gradlew :kite3d:jsNodeTest       # JS (Node)
./gradlew :kite3d:macosArm64Test   # native (on Apple silicon)
```

A port is "done" only when its ported test suite is green on **jvm, one native
target, and js** — the three engines have different `libm` implementations, so a
tolerance that passes on the JVM can still fail on native (see PORTING.md, the
transcendental-tolerance rule).

Toolchain: JDK 21 builds the project (`jvmToolchain(21)`), but the JVM bytecode
target is 11, so consumers on JDK 11+ and Android are supported.

## Local publishing

`RELEASE_SIGNING_ENABLED=true` in `gradle.properties` is meant for CI, which has
the signing key. For a local `publishToMavenLocal`, override it:

```bash
./gradlew publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false
```

## Style

`explicitApi()` is strict — give every public declaration an explicit visibility
and return type. Match the surrounding code; the already-ported `Vector2.kt` /
`Box2.kt` / `MathUtils.kt` are the canonical examples.
