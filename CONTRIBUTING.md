# Contributing to Kite3D

Kite3D follows the three.js maths API, release `r184`. A change either fixes
something or brings more of three.js's maths over to Kotlin. Every change lands
with its tests.

## Build and test

The project builds with the [Kotlin Toolchain](https://github.com/JetBrains/kotlin-toolchain):
the `./kotlin` script in the repository root (`kotlin.bat` on Windows). It downloads
the JDK, Kotlin/Native and Node.js that it needs.

```bash
./kotlin build -p jvm                          # fast compile check
./kotlin test -p jvm                           # JVM
./kotlin test -p macosArm64                    # native, on Apple silicon
python3 .github/scripts/web-tests/run.py       # JavaScript, Wasm and WASI, in Node
```

`./kotlin test` cannot run the JavaScript and WASI tests, and it runs the Wasm
tests only in a browser. The script links the three test builds with the
toolchain and runs them in Node. Add `--platform js` to run only one of them.

Before you open a pull request, run the tests on the JVM, one native target and
JavaScript. Each one computes `sin`, `cos` and `exp` slightly differently, so a
tolerance that passes on the JVM can still fail on native.

JDK 21 builds the project. The JVM bytecode level is 11, so the library runs on
JDK 11 and on Android.

## Public API changes

The public API is recorded in `kite3d/api/`. CI fails when the code no longer
matches that record:

```bash
./kotlin check abi       # the JVM record
./kotlin check klibAbi   # the native, JavaScript and Wasm record
```

The klib check reads the built klibs, so build those platforms first with
`./kotlin build`. CI shows the full command in `.github/workflows/ci.yml`.

When you change the public API on purpose, update the record and commit it with
the change:

```bash
./kotlin do updateAbi
./kotlin do updateKlibAbi
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

This command puts a build into `~/.m2`, for tests in other projects:

```bash
./kotlin publish mavenLocal -m kite3d
```

Release builds are signed, so a local publish also needs a signing key in the
`KOTLIN_TOOLCHAIN_SIGNING_KEY` and `KOTLIN_TOOLCHAIN_SIGNING_KEY_PASSPHRASE`
environment variables.

## Style

`explicitApi()` is on. Give every public declaration an explicit visibility and
return type, and match the code around it. `Vector2.kt`, `Box2.kt` and
`MathUtils.kt` are good examples.
