# Kite3D

A 3D maths library for Kotlin Multiplatform: three.js's `src/math` layer ported
file for file to common Kotlin, for anyone who needs vectors, matrices,
quaternions, bounding volumes and colour without a rendering engine attached.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.yuroyami/kite3d)](https://central.sonatype.com/artifact/io.github.yuroyami/kite3d)
[![CI](https://img.shields.io/github/actions/workflow/status/yuroyami/Kite3D/ci.yml?branch=main&label=CI)](https://github.com/yuroyami/Kite3D/actions/workflows/ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

## What you get

Every one of the 29 files in three.js r184's `src/math` has a Kotlin
counterpart: `Vector2`/`3`/`4`, `Matrix2`/`3`/`4`, `Quaternion`, `Euler`,
`Box2`/`Box3`, `Sphere`, `Plane`, `Ray`, `Line3`, `Triangle`, `Frustum`,
`FrustumArray`, `Spherical`, `Cylindrical`, `SphericalHarmonics3`, `Color`,
`ColorManagement`, `MathUtils`, and `Interpolant` with its five keyframe
subclasses.

Nothing above that layer is written. There is no `Object3D`, no scene graph, no
`BufferGeometry`, no camera, material, light, shader graph or GPU backend, so
Kite3D cannot draw anything and is not usable as an engine today. The maths
itself is finished and pinned by three.js's own unit suites, translated to
`kotlin-test` and run on ten target hosts. It is one artifact of pure
`commonMain` Kotlin — no `expect`/`actual`, no cinterop, no JNI — and its only
dependency is `kotlin-stdlib`.

```kotlin
import io.github.yuroyami.kite3d.math.*

// Would this box be inside a camera's view? Build the projection matrix by hand;
// there is no camera type yet.
val projection = Matrix4().makePerspective(-1.0, 1.0, 1.0, -1.0, 1.0, 100.0)
val frustum = Frustum().setFromProjectionMatrix(projection)

val box = Box3(Vector3(0.0, 0.0, 0.0), Vector3(1.0, 1.0, 1.0))
frustum.intersectsBox(box)                          // false — it is behind the camera

box.translate(Vector3(-1.0001, -1.0001, -1.0001))
frustum.intersectsBox(box)                          // true — it now crosses the near plane
```

## Install

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.yuroyami:kite3d:0.1.0")
        }
    }
}
```

Everything lives in `io.github.yuroyami.kite3d.math`, except the five
interpolants in `io.github.yuroyami.kite3d.math.interpolants`. There is no
facade object or entry point — you import the types you use.

## Conventions

Four rules cover the whole library.

**Objects are mutable, and methods return the receiver.**

```kotlin
val v = Vector3(1.0, 2.0, 3.0)
    .multiplyScalar(2.0)
    .add(Vector3(0.0, 1.0, 0.0))
    .normalize()
```

`a.add(b)` changes `a`. Call `clone()` first when you need the original. This is
three.js's design, and keeping it is what lets upstream's test suites carry over
unchanged.

**A query that computes a new value writes into a target you pass in.**

```kotlin
val box = Box3(Vector3(-1.0, -1.0, -1.0), Vector3(1.0, 3.0, 1.0))

val center = Vector3()
box.getCenter(center)     // writes into center, and returns it — (0.0, 1.0, 0.0)
```

`box.getCenter()` with no argument does not compile. There is no allocating
overload — not for `getCenter`, `getSize`, `projectPoint`, `intersectSphere` or
any of the others. Pass a fresh `Vector3()` when you have nowhere to keep the
result. Plain properties like `sphere.center`, `box.min` and `ray.origin` are
different; they read state the object already holds, so they take no argument.

**Constructors keep the object you hand them.**

```kotlin
val center = Vector3(0.0, 0.0, 0.0)
val sphere = Sphere(center, 1.0)

center.set(5.0, 0.0, 0.0)
sphere.center                 // (5.0, 0.0, 0.0) — the same Vector3
```

`Sphere`, `Box3`, `Ray`, `Line3`, `Plane` and `Triangle` store the vectors you
pass rather than copying them, exactly as upstream does. Clone them first if you
do not want that.

**Every scalar is `Double`.** three.js runs on JavaScript numbers, which are
64-bit floats, so the port uses `Double` everywhere and upstream's test
tolerances carry over unchanged. There are no `Float` overloads.

## What it does

### Build a transform and take it apart

```kotlin
val model = Matrix4().compose(
    Vector3(0.0, 2.0, 0.0),                                        // position
    Quaternion().setFromEuler(Euler(0.0, MathUtils.degToRad(90.0), 0.0)),
    Vector3(1.0, 1.0, 1.0),                                        // scale
)

Vector3(1.0, 0.0, 0.0).applyMatrix4(model)   // (2.2e-16, 2.0, -1.0); x is noise for 0

val position = Vector3()
val rotation = Quaternion()
val scale = Vector3()
model.decompose(position, rotation, scale)   // (0, 2, 0), the rotation, (1, 1, 1)
```

`Euler` carries its own rotation order (`EulerOrder.XYZ` by default) and
`reorder` converts between orders. `Matrix4` also has `lookAt`, `makeBasis`,
`makeShear`, `makePerspective`, `makeOrthographic` and the per-axis
`makeRotationX`/`Y`/`Z`.

### Test whether volumes overlap

```kotlin
val bounds = Box3().setFromPoints(
    listOf(
        Vector3(-1.0, -1.0, -1.0),
        Vector3(2.0, 0.5, 1.0),
        Vector3(0.0, 3.0, -2.0),
    ),
)

bounds.getSize(Vector3())                                  // (3.0, 4.0, 3.0)
bounds.intersectsSphere(Sphere(Vector3(), 2.0))            // true
bounds.containsPoint(Vector3())                            // true
bounds.getBoundingSphere(Sphere()).radius                  // 2.9154759474226504
```

`Box3` intersects boxes, spheres, planes and triangles; `Sphere`, `Plane`,
`Triangle` and `Frustum` carry the matching predicates. `Frustum.containsPoint`,
`intersectsSphere` and `intersectsBox` are the three a culling pass needs.

### Cast a ray

```kotlin
val ray = Ray(Vector3(0.0, 0.0, 5.0), Vector3(0.0, 0.0, -1.0))

val hit: Vector3? = ray.intersectSphere(Sphere(Vector3(), 1.0), Vector3())
// (0.0, 0.0, 1.0), or null when the ray misses

ray.at(2.0, Vector3())                                     // (0.0, 0.0, 3.0)

ray.intersectTriangle(
    Vector3(-1.0, -1.0, 0.0),
    Vector3(1.0, -1.0, 0.0),
    Vector3(0.0, 1.0, 0.0),
    backfaceCulling = false,
    Vector3(),
)                                                          // (0.0, 0.0, 0.0)
```

`intersectSphere`, `intersectBox`, `intersectPlane` and `intersectTriangle`
return `null` on a miss. `intersectsSphere`, `intersectsBox` and
`intersectsPlane` answer the same question without producing a point.

### Orbit a point

```kotlin
val offset = Vector3(0.0, 5.0, 10.0)          // camera relative to what it looks at
val s = Spherical().setFromVector3(offset)    // radius 11.180339887498949

s.theta += 0.05                               // swing sideways
s.phi -= 0.05                                 // and upward
s.makeSafe()                                  // keep phi off the poles

offset.setFromSpherical(s)
```

`Cylindrical` is the same idea with `radius`, `theta` and `y`, paired with
`Vector3.setFromCylindrical`.

### Blend colours

```kotlin
val mixed = Color().lerpColors(Color(0xff0000), Color(0x0000ff), 0.5)
mixed.getHexString()          // "bc00bc"
```

`Color(0xff0000)` reads the hex as sRGB and stores it in the working colour
space, which `ColorManagement` defaults to linear sRGB. The blend therefore
happens in linear light and the midpoint comes back as `bc00bc` rather than
`7f007f`. All four readers take a colour space, but they do not agree on the default:
`getHex` and `getHexString` default to `ColorSpace.SRGB`, while `getRGB(target)`
and `getHSL(target)` default to `ColorManagement.workingColorSpace` — linear,
unless you changed it. Pass the space explicitly on those two if you want sRGB
numbers back. There is no string constructor; see
[Limits](#limits).

### Sample a keyframe track

```kotlin
import io.github.yuroyami.kite3d.math.interpolants.*

val times = doubleArrayOf(0.0, 1.0, 2.0)
val values = doubleArrayOf(0.0, 10.0, 20.0)
val track = LinearInterpolant(times, values, 1, DoubleArray(1))

track.evaluate(0.5)[0]        // 5.0
track.evaluate(1.5)[0]        // 15.0
```

`LinearInterpolant`, `CubicInterpolant`, `DiscreteInterpolant`,
`QuaternionLinearInterpolant` and `BezierInterpolant` all extend the abstract
`Interpolant`. Its `copySampleValue_` is `open`, so a glTF cubic-spline subclass
can override it the way upstream's own test does.

## Targets

One artifact, 22 targets.

| Family | Targets |
| --- | --- |
| JVM | `jvm`, at bytecode level 11 |
| Apple | `iosArm64`, `iosSimulatorArm64`, `iosX64`, `macosArm64`, `macosX64`, `tvosArm64`, `tvosSimulatorArm64`, `watchosArm32`, `watchosArm64`, `watchosSimulatorArm64`, `watchosDeviceArm64` |
| Native desktop | `linuxX64`, `linuxArm64`, `mingwX64` |
| Android NDK | `androidNativeArm32`, `androidNativeArm64`, `androidNativeX86`, `androidNativeX64` |
| Web | `js` (browser, Node), `wasmJs` (browser, Node), `wasmWasi` (Node) |

**There is no `androidTarget`.** It is left out on purpose, because declaring it
would require a configured Android SDK to build the project. No `-android`
artifact is published, so an Android module that depends on
`io.github.yuroyami:kite3d` resolves the `jvm` one instead. That works — the
code is target-agnostic and the bytecode level is 11 — but there is no declared
`minSdk` and no Android-specific variant. The `androidNative*` entries above are
the NDK targets and are not a substitute.

## Limits

- Kite3D renders nothing. The published artifact contains one package,
  `io.github.yuroyami.kite3d.math`, and its sub-package of interpolants.
- Methods whose implementation lives above the maths layer are absent rather
  than stubbed: `Vector3.project` and `unproject` (they need a camera),
  `Box3.setFromObject` and `expandByObject` (they need `Object3D`),
  `Frustum.intersectsObject` and `intersectsSprite` (they need `Object3D` and
  `Sprite`).
- `FrustumArray` is public but cannot do anything yet. Every intersection method
  and `setFromArrayCamera` needs `ArrayCamera`; only `clone()` and
  `coordinateSystem` are ported.
- `Color` has no CSS path. `setStyle`, `setColorName`, `getStyle` and
  `Color.NAMES` are not ported, so a colour cannot be built from `"#ff0000"` or
  `"red"`. Use the `Int` hex constructor, three `Double`s, or `setHSL`.
- The row-major element constructors are not ported. `Matrix4(n11, n12, …)` does
  not exist; use `Matrix4().set(…)`, which takes the same sixteen arguments in
  the same order. `Matrix3` and `Matrix2` work the same way, with nine and four.
- No instance is thread-safe, and neither is `ColorManagement`, which holds the
  global `enabled` and `workingColorSpace`. Distinct instances are safe to use
  concurrently — that is what the JVM concurrency test guards.
- The public API is locked by explicit API mode and a committed ABI dump under
  `kite3d/api/`, so a signature change fails the build. That guard starts at
  0.1.0; it does not make 0.1.0 a stable API.

## Testing

452 tests in common code, translated from three.js's own `test/unit/src/math`
suites, plus one JVM-only test that runs the methods three.js backed with
module-level scratch variables on 32 threads and checks the results against a
single-threaded baseline.

CI runs the common suite on ten hosts: JVM, JS (Node), Wasm (Node and WASI),
`linuxX64`, `macosArm64`, `iosSimulatorArm64`, `tvosSimulatorArm64`,
`watchosSimulatorArm64` and `mingwX64`. The remaining targets are compile-checked
by `assemble`, because no GitHub-hosted runner can execute them. Each engine
earns its run. `sin`, `cos` and `exp` are not bit-identical across JavaScript,
the JVM and native `libm`, so a tolerance that passes in one place can fail in
another.

Three files ship without a ported test, because three.js r184 has no suite for
them: `Matrix2`, `BezierInterpolant` and `FrustumArray`.

## Documentation

[Guides and the full API reference](https://yuroyami.github.io/Kite3D/).

[port-ledger.yaml](port-ledger.yaml) records every ported file, its test status
and each intentional difference from upstream. [PORTING.md](PORTING.md) is the
dialect those decisions follow.

## Contributing

Work right now is porting three.js source files to Kotlin in dependency order.
Read [CONTRIBUTING.md](CONTRIBUTING.md), then [PORTING.md](PORTING.md). Every
entry in [port-ledger.yaml](port-ledger.yaml) is `ported`, so the next work is
adding a ledger entry for a file above the maths layer, not claiming one. A class
and its
upstream test suite land in the same commit. Regenerate the ABI dump with
`./gradlew :kite3d:updateKotlinAbi` when you change the public surface.

## License

MIT. Kotlin port © 2026 yuroyami; original three.js © 2010-2026 three.js authors.
See [LICENSE](LICENSE) and [CHANGELOG.md](CHANGELOG.md).

Part of the Kite family: [KiteCore](https://github.com/yuroyami/KiteCore),
[KitePDF](https://github.com/yuroyami/KitePDF),
[KiteQR](https://github.com/yuroyami/KiteQR).
