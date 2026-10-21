# Changelog

All notable changes to Kite3D are recorded here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project follows
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Build

- The project builds with the Kotlin Toolchain (`./kotlin`) instead of Gradle, on
  Kotlin 2.4.21. The published artifacts keep their coordinates and klib names.

## [0.2.0] - 2026-09-12

### Added

- `Camera`: a perspective or orthographic projection and a view matrix. Place it
  with `lookAt`, call `updateMatrices()`, then `project` maps a point to
  normalised device coordinates. One `project` takes a `Vector3`. The other
  takes three floats and writes into a `FloatArray`, with no allocation.

## [0.1.0] - 2026-07-25

The first release.

### Added

- The three.js maths types, from release r184, in common Kotlin: `Vector2`,
  `Vector3`, `Vector4`, `Matrix2`, `Matrix3`, `Matrix4`, `Quaternion`, `Euler`,
  `Box2`, `Box3`, `Sphere`, `Plane`, `Ray`, `Line3`, `Triangle`, `Frustum`,
  `FrustumArray`, `Spherical`, `Cylindrical`, `SphericalHarmonics3`, `Color`,
  `ColorManagement`, `MathUtils`, and `Interpolant` with its five keyframe
  subclasses.
- `AttributeLike`, a small interface for vertex data, read by methods such as
  `fromBufferAttribute`.
- 452 tests in common code, run on the JVM, JavaScript, Wasm and native
  targets, and one JVM test that runs the shared types on 32 threads.
- A committed ABI dump for the JVM and all 21 klib targets. The build fails when
  the public API changes and the dump does not.

Differences from three.js:

- `Sphere.toJSON()` and `Sphere.fromJSON()` use a `DoubleArray`, in the same
  shape as `Box3`'s.
- `Interpolant.copySampleValue_` is `open`, so a subclass such as a glTF
  cubic-spline interpolant can override it.
- Some methods are missing, because they need scene types that Kite3D does not
  have: `Vector3.project` and `unproject`, `Box3.setFromObject` and
  `expandByObject`, `Frustum.intersectsObject` and `intersectsSprite`, the
  intersection methods of `FrustumArray`, and the CSS-string methods of `Color`
  (`setStyle`, `setColorName`, `getStyle`, `Color.NAMES`).
