# Contributing to Kite3D

Kite3D is a from-scratch port of [three.js](https://github.com/mrdoob/three.js)
(`r184`) to Kotlin Multiplatform. Almost all contributions right now are **ports**:
turning a three.js source file into common Kotlin.

## Getting the three.js reference

Porting means reading the upstream file side by side with its Kotlin counterpart,
so you need a local checkout of three.js at the pinned revision. It lives at
`three.js-ref/` and is **git-ignored**. It is not part of Kite3D and is never
committed:

```bash
git clone --depth 1 --branch r184 https://github.com/mrdoob/three.js.git three.js-ref
```

The sources you will want are `three.js-ref/src/math/…` and their suites in
`three.js-ref/test/unit/src/math/…`.

## Before you port a file

Read **[PORTING.md](PORTING.md)** first. It is the binding dialect, and it
covers structure, the JS→Kotlin language mapping, cross-layer seams, and the
trap catalogue. The catalogue lists the subtle `clamp`, `Math.round`, int32 and
`-0.0` differences that silently corrupt a port.

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
./gradlew :kite3d:wasmJsNodeTest   # Wasm (Node)
./gradlew :kite3d:wasmWasiNodeTest # Wasm (WASI)
```

A port is "done" only when its ported test suite is green on **jvm, one native
target, and js**. The three engines have different `libm` implementations, so a
tolerance that passes on the JVM can still fail on native. See the
transcendental-tolerance rule in PORTING.md.

## Public API changes

The public ABI is dumped to `kite3d/api/` and checked in. CI fails when the code
drifts from the dump:

```bash
./gradlew :kite3d:checkKotlinAbi
```

If the change is intended, regenerate and commit the dump in the same PR:

```bash
./gradlew :kite3d:updateKotlinAbi
```

Regenerate on macOS when you can. It is the only host that builds every target
in the matrix, so a dump produced anywhere else covers fewer klibs.

Toolchain: JDK 21 builds the project (`jvmToolchain(21)`), but the JVM bytecode
target is 11, so consumers on JDK 11+ and Android are supported.

## Local publishing

`RELEASE_SIGNING_ENABLED=true` in `gradle.properties` is meant for CI, which has
the signing key. For a local `publishToMavenLocal`, override it:

```bash
./gradlew publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false
```

## Style

`explicitApi()` is strict. Give every public declaration an explicit visibility
and return type. Match the surrounding code. The already-ported `Vector2.kt`,
`Box2.kt` and `MathUtils.kt` are the canonical examples.
