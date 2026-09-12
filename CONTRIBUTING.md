# Contributing to Kite3D

Kite3D follows the three.js maths API, release `r184`. A change either fixes
something or brings more of three.js's maths over to Kotlin. Every change lands
with its tests.

## Build and test

```bash
./gradlew :kite3d:compileKotlinJvm   # fast compile check
./gradlew :kite3d:jvmTest            # JVM
./gradlew :kite3d:jsNodeTest         # JavaScript (Node)
./gradlew :kite3d:macosArm64Test     # native, on Apple silicon
./gradlew :kite3d:wasmJsNodeTest     # Wasm (Node)
./gradlew :kite3d:wasmWasiNodeTest   # Wasm (WASI)
```

Before you open a pull request, run the tests on the JVM, one native target and
JavaScript. Each one computes `sin`, `cos` and `exp` slightly differently, so a
tolerance that passes on the JVM can still fail on native.

JDK 21 builds the project. The JVM bytecode level is 11, so the library runs on
JDK 11 and on Android.

## Public API changes

The public API is recorded in `kite3d/api/`. CI fails when the code no longer
matches that record:

```bash
./gradlew :kite3d:checkKotlinAbi
```

When you change the public API on purpose, update the record and commit it with
the change:

```bash
./gradlew :kite3d:updateKotlinAbi
```

Run it on macOS if you can. Only macOS builds every target, so a record made on
another host covers fewer of them.

## Adding code from three.js

Read [PORTING.md](PORTING.md) first. It has the rules for turning three.js
JavaScript into Kotlin, and the language differences that change results
without an error.

Keep a copy of three.js beside your work. The `three.js-ref/` folder is ignored
by git:

```bash
git clone --depth 1 --branch r184 https://github.com/mrdoob/three.js.git three.js-ref
```

The maths sources are in `three.js-ref/src/math/`, and their tests are in
`three.js-ref/test/unit/src/math/`. Bring the tests over with the code.

## Local publishing

Release builds are signed. For a local `publishToMavenLocal`, turn signing off:

```bash
./gradlew publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false
```

## Style

`explicitApi()` is on. Give every public declaration an explicit visibility and
return type, and match the code around it. `Vector2.kt`, `Box2.kt` and
`MathUtils.kt` are good examples.
