# Changelog

All notable changes to Kite3D are recorded here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project follows
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Kite3D is a port, so every entry names the three.js revision it tracks. The
per-file record lives in [port-ledger.yaml](port-ledger.yaml). It includes every
intentional deviation from upstream.

## [0.1.0] - unreleased

The first release. Nothing has been published to Maven Central yet, so there is
no earlier version to compare against and this entry has no Changed or Fixed
counterpart.

### Added

All 29 files of three.js r184's `src/math`, ported to common Kotlin: `Vector2`,
`Vector3`, `Vector4`, `Matrix2`, `Matrix3`, `Matrix4`, `Quaternion`, `Euler`,
`Box2`, `Box3`, `Sphere`, `Plane`, `Ray`, `Line3`, `Triangle`, `Frustum`,
`FrustumArray`, `Spherical`, `Cylindrical`, `SphericalHarmonics3`, `Color`,
`ColorManagement`, `MathUtils`, `Interpolant` and the five keyframe
interpolants. `AttributeLike` is added on top as a Kite3D-only interface that a
later `BufferAttribute` will implement.

The upstream unit suites are translated to `kotlin-test`: 452 tests in common
code, green on JVM, JS (Node), Wasm (Node and WASI) and native, plus one
JVM-only test that runs the methods three.js backed with module-level scratch
variables on 32 threads and compares the results against a single-threaded
baseline.

Departures from upstream worth naming here:

- `Matrix4.determinantAffine()`: the upper-left 3x3 determinant, matching
  three.js r184. `extractBasis`, `extractRotation` and `decompose` guard on it
  instead of the full 4x4 `determinant()`. The two results agree for affine
  matrices but not for projective ones, so guarding on the 4x4 took the wrong
  branch on a projection matrix. Both upstream tests are ported.
- `Sphere.toJSON()` / `Sphere.fromJSON()` are flattened to `DoubleArray`, in the
  same shape as `Box3`'s. Round-trip tests cover both.
- `Interpolant.copySampleValue_` is `open`. Kotlin members are final by default,
  which made the glTF cubic-spline subclass shape impossible to express.
  Upstream's own test demonstrates that shape. `CustomInterpolantTest` ports
  that suite and pins the extension seam.
- Methods that reach into layers above math are omitted rather than stubbed:
  `Vector3.project`/`unproject`, `Box3.setFromObject`/`expandByObject`,
  `Frustum.intersectsObject`/`intersectsSprite`, every intersection method on
  `FrustumArray`, and `Color`'s CSS-string path (`setStyle`, `setColorName`,
  `getStyle`, `Color.NAMES`). Each is listed under `deferred` in
  [port-ledger.yaml](port-ledger.yaml) with what it is waiting on.

Build and release setup:

- Checked-in public-ABI dumps under `kite3d/api/`, covering the JVM and all 21
  klib targets. `./gradlew :kite3d:checkKotlinAbi` gates the build on them.
- CI runs every test task a GitHub-hosted runner can actually execute, including
  `wasmWasiNodeTest` and the tvOS/watchOS simulators, and states which targets
  are compile-only and why.
- Tag-triggered Maven Central release workflow.
- The root Gradle project is named `kite3d-root`. It was `kite3d`, the same as
  the included module, so `TYPESAFE_PROJECT_ACCESSORS` generated `getKite3d()`
  twice and every Gradle invocation, including `./gradlew help`, died with
  "method getKite3d() is already defined".
