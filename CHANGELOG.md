# Changelog

All notable changes to Kite3D are recorded here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project follows
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Kite3D is a port, so every entry names the three.js revision it tracks. The
per-file record — including every intentional deviation from upstream — lives in
[port-ledger.yaml](port-ledger.yaml).

## [Unreleased]

### Added

- `Matrix4.determinantAffine()` — the upper-left 3x3 determinant, matching three.js
  r184. `extractBasis`, `extractRotation` and `decompose` now guard on it instead of
  the full 4x4 `determinant()`. The two results agree for affine matrices but not for
  projective ones, so the previous code took the wrong branch on a projection matrix.
  Both upstream tests are ported.
- `Sphere.toJSON()` / `Sphere.fromJSON()`, flattened to `DoubleArray` in the same
  shape as `Box3`'s. Round-trip tests added for both.
- `CustomInterpolantTest` — ports the upstream suite that pins the `Interpolant`
  extension seam (a CUBICSPLINE-style subclass overriding `copySampleValue_`).
- Checked-in public-ABI dumps under `kite3d/api/`, covering the JVM and all 21 klib
  targets. `./gradlew :kite3d:checkKotlinAbi` gates the build on them.
- Tag-triggered Maven Central release workflow.

### Changed

- `Interpolant.copySampleValue_` is now `open`. Kotlin members are final by default,
  which made the GLTF cubic-spline subclass shape — the one upstream's own test
  demonstrates — impossible to express.
- CI runs every test task a GitHub-hosted runner can actually execute, including
  `wasmWasiNodeTest` and the tvOS/watchOS simulators, and the workflow now states
  which targets are compile-only and why.

### Fixed

- The build no longer fails at configuration. `rootProject.name` was `kite3d`, the
  same as the included module, so `TYPESAFE_PROJECT_ACCESSORS` generated
  `getKite3d()` twice and every Gradle invocation — including `./gradlew help` —
  died with "method getKite3d() is already defined". The root project is now
  `kite3d-root`.

## [0.1.0] — unreleased

First tagged release. Ports the `src/math` layer of three.js r184 to common Kotlin:
vectors, matrices, quaternion, euler; box/sphere/plane/ray/line3/triangle; frustum;
spherical/cylindrical; color and colour management; interpolants. The upstream unit
test suites are translated to `kotlin-test` and green on JVM, JS (Node), Wasm
(Node/WASI) and native.
