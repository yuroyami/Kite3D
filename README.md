# Kite3D

3D maths for Kotlin Multiplatform: vectors, matrices, quaternions, bounding
volumes and colour, with the same API as three.js.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.yuroyami/kite3d)](https://central.sonatype.com/artifact/io.github.yuroyami/kite3d)
[![CI](https://img.shields.io/github/actions/workflow/status/yuroyami/Kite3D/ci.yml?branch=main&label=CI)](https://github.com/yuroyami/Kite3D/actions/workflows/ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

**[Documentation](https://yuroyami.github.io/Kite3D/)** · a guided tour, a first
program, plus the generated API reference.

## What you get

29 maths types in common Kotlin: `Vector2`/`3`/`4`, `Matrix2`/`3`/`4`,
`Quaternion`, `Euler`, `Box2`/`Box3`, `Sphere`, `Plane`, `Ray`, `Line3`,
`Triangle`, `Frustum`, `FrustumArray`, `Spherical`, `Cylindrical`,
`SphericalHarmonics3`, `Color`, `ColorManagement`, `MathUtils`, and
`Interpolant` with its five keyframe subclasses.

Kite3D draws nothing. It has no `Object3D`, no scene graph, no
`BufferGeometry`, no camera, material, light, shader graph or GPU backend. Use
it for the maths. It is not an engine.

The library ships as one artifact of pure `commonMain` Kotlin. It uses no
`expect`/`actual`, no cinterop and no JNI. Its only dependency is
`kotlin-stdlib`.

```kotlin
import io.github.yuroyami.kite3d.math.*

// Is this box inside a camera's view? Build the projection matrix yourself.
// Kite3D has no camera type.
val projection = Matrix4().makePerspective(-1.0, 1.0, 1.0, -1.0, 1.0, 100.0)
val frustum = Frustum().setFromProjectionMatrix(projection)

val box = Box3(Vector3(0.0, 0.0, 0.0), Vector3(1.0, 1.0, 1.0))
frustum.intersectsBox(box)                          // false: the box is behind the camera

box.translate(Vector3(-1.0001, -1.0001, -1.0001))
frustum.intersectsBox(box)                          // true: the box now crosses the near plane
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

Everything lives in `io.github.yuroyami.kite3d.math`. The five interpolants live
in `io.github.yuroyami.kite3d.math.interpolants`. There is no facade object and
no entry point. Import the types you use.

## Conventions

Four rules cover the whole library.

**Objects are mutable, and methods return the receiver.**

```kotlin
val v = Vector3(1.0, 2.0, 3.0)
    .multiplyScalar(2.0)
    .add(Vector3(0.0, 1.0, 0.0))
    .normalize()
```

`a.add(b)` changes `a`. Call `clone()` first when you need the original.

**A query that computes a new value writes into a target you pass in.**

```kotlin
val box = Box3(Vector3(-1.0, -1.0, -1.0), Vector3(1.0, 3.0, 1.0))

val center = Vector3()
box.getCenter(center)     // writes (0.0, 1.0, 0.0) into center, and returns it
```

`box.getCenter()` with no argument does not compile. No query method has an
allocating overload. That is true of `getCenter`, `getSize`, `projectPoint`,
`intersectSphere` and all the others. Pass a fresh `Vector3()` when you have
nowhere to keep the result.

Plain properties work differently. `sphere.center`, `box.min` and `ray.origin`
read state the object already holds, so they take no argument.

**Constructors keep the object you hand them.**

```kotlin
val center = Vector3(0.0, 0.0, 0.0)
val sphere = Sphere(center, 1.0)

center.set(5.0, 0.0, 0.0)
sphere.center                 // (5.0, 0.0, 0.0): the same Vector3
```

`Sphere`, `Box3`, `Ray`, `Line3`, `Plane` and `Triangle` store the vectors you
pass. They do not copy them. Clone a vector first if you do not want that.

**Every scalar is `Double`.** There are no `Float` overloads.

## What it does

### Build a transform and decompose it

```kotlin
val model = Matrix4().compose(
    Vector3(0.0, 2.0, 0.0),                                        // position
    Quaternion().setFromEuler(Euler(0.0, MathUtils.degToRad(90.0), 0.0)),
    Vector3(1.0, 1.0, 1.0),                                        // scale
)

Vector3(1.0, 0.0, 0.0).applyMatrix4(model)   // (2.2e-16, 2.0, -1.0); x is 0 plus rounding error

val position = Vector3()
val rotation = Quaternion()
val scale = Vector3()
model.decompose(position, rotation, scale)   // (0, 2, 0), the rotation, (1, 1, 1)
```

`Euler` holds its own rotation order. The default is `EulerOrder.XYZ`. Call
`reorder` to convert between orders. `Matrix4` also has `lookAt`, `makeBasis`,
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

`Box3` intersects boxes, spheres, planes and triangles. `Sphere`, `Plane`,
`Triangle` and `Frustum` have the matching methods. A culling pass needs three
of them: `Frustum.containsPoint`, `Frustum.intersectsSphere` and
`Frustum.intersectsBox`.

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
return `null` when the ray misses. `intersectsSphere`, `intersectsBox` and
`intersectsPlane` answer the same question without producing a point.

### Orbit a point

```kotlin
val offset = Vector3(0.0, 5.0, 10.0)          // camera position, relative to its target
val s = Spherical().setFromVector3(offset)    // radius 11.180339887498949

s.theta += 0.05                               // rotate sideways
s.phi -= 0.05                                 // rotate upward
s.makeSafe()                                  // keep phi away from the poles

offset.setFromSpherical(s)
```

`Cylindrical` works the same way. It has `radius`, `theta` and `y`, and pairs
with `Vector3.setFromCylindrical`.

### Blend colours

```kotlin
val mixed = Color().lerpColors(Color(0xff0000), Color(0x0000ff), 0.5)
mixed.getHexString()          // "bc00bc"
```

`Color(0xff0000)` reads the hex value as sRGB. It then stores the colour in the
working colour space. `ColorManagement` sets that space to linear sRGB by
default. The blend therefore happens in linear light, and the midpoint comes
back as `bc00bc` rather than `7f007f`.

All four readers take a colour space, but their defaults differ:

| Reader | Default colour space |
| --- | --- |
| `getHex`, `getHexString` | `ColorSpace.SRGB` |
| `getRGB(target)`, `getHSL(target)` | `ColorManagement.workingColorSpace`, which is linear until you change it |

Pass the colour space explicitly to `getRGB` and `getHSL` when you want sRGB
numbers back. There is no string constructor. See [Limits](#limits).

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
`Interpolant`. Its `copySampleValue_` method is `open`, so a glTF cubic-spline
subclass can override it.

## Targets

One artifact, 22 targets.

| Family | Targets |
| --- | --- |
| JVM | `jvm`, at bytecode level 11 |
| Apple | `iosArm64`, `iosSimulatorArm64`, `iosX64`, `macosArm64`, `macosX64`, `tvosArm64`, `tvosSimulatorArm64`, `watchosArm32`, `watchosArm64`, `watchosSimulatorArm64`, `watchosDeviceArm64` |
| Native desktop | `linuxX64`, `linuxArm64`, `mingwX64` |
| Android NDK | `androidNativeArm32`, `androidNativeArm64`, `androidNativeX86`, `androidNativeX64` |
| Web | `js` (browser, Node), `wasmJs` (browser, Node), `wasmWasi` (Node) |

**There is no `androidTarget`.** Declaring it would require a configured Android
SDK to build the project, so Kite3D leaves it out on purpose. No `-android`
artifact is published. An Android module that depends on
`io.github.yuroyami:kite3d` resolves the `jvm` artifact instead.

That works, because the code is target-agnostic and the bytecode level is 11.
There is still no declared `minSdk` and no Android-specific variant. The
`androidNative*` entries above are the NDK targets. They are not a substitute.

## Limits

Kite3D renders nothing. The published artifact contains one package,
`io.github.yuroyami.kite3d.math`, and its sub-package of interpolants.

Some methods are missing, because they need types that Kite3D does not have:

| Missing | Use instead |
| --- | --- |
| `Vector3.project`, `Vector3.unproject` | Apply the view and projection matrices yourself with `Vector3.applyMatrix4`. |
| `Box3.setFromObject`, `Box3.expandByObject` | Collect the points or boxes yourself, then call `setFromPoints`, `expandByPoint` or `union`. |
| `Frustum.intersectsObject`, `Frustum.intersectsSprite` | Read the object's bounding box or sphere yourself, then call `Frustum.intersectsBox` or `Frustum.intersectsSphere`. |

The rest of the current limits:

- `FrustumArray` is public but does almost nothing. Only `clone()` and
  `coordinateSystem` are available. Every intersection method and
  `setFromArrayCamera` is missing. Use `Frustum` for culling.
- `Color` cannot read CSS strings. `setStyle`, `setColorName`, `getStyle` and
  `Color.NAMES` are missing, so you cannot build a colour from `"#ff0000"` or
  `"red"`. Use the `Int` hex constructor, three `Double` values, or `setHSL`.
- The element constructors are missing. `Matrix4(n11, n12, …)` does not exist.
  Use `Matrix4().set(…)`, which takes the same sixteen arguments in the same
  row-major order. `Matrix3` and `Matrix2` work the same way, with nine
  arguments and four.
- Results are not bit-identical across targets. `sin`, `cos` and `exp` differ
  slightly between JavaScript, the JVM and native `libm`. The same call can
  return a slightly different `Double` on another target. Compare results with a
  tolerance.
- No instance is thread-safe. `ColorManagement` is not thread-safe either,
  because it holds the global `enabled` and `workingColorSpace` values. Separate
  instances are safe to use at the same time on different threads. A JVM
  concurrency test checks that.
- Explicit API mode and a committed ABI dump under `kite3d/api/` lock the public
  API, so a signature change fails the build. That guard starts at 0.1.0. It
  does not make 0.1.0 a stable API.

## Testing

452 tests cover the maths in common code. One more test runs on the JVM only. It
puts 32 threads through the shared types and compares the results against a
single-threaded baseline. CI runs the common suite on ten hosts: JVM, JS (Node),
Wasm (Node), Wasm (WASI), `linuxX64`, `macosArm64`, `iosSimulatorArm64`,
`tvosSimulatorArm64`, `watchosSimulatorArm64` and `mingwX64`. The remaining
targets are compile-checked by `assemble`, because no GitHub-hosted runner can
execute them.

`Matrix2`, `BezierInterpolant` and `FrustumArray` have no tests.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Every change lands with its tests.
Regenerate the ABI dump with `./gradlew :kite3d:updateKotlinAbi` when you change
the public surface.

## License

MIT. Kotlin port © 2026 yuroyami; original three.js © 2010-2026 three.js authors.
See [LICENSE](LICENSE) and [CHANGELOG.md](CHANGELOG.md).

Part of the Kite family: [KiteCore](https://github.com/yuroyami/KiteCore),
[KitePDF](https://github.com/yuroyami/KitePDF),
[KiteQR](https://github.com/yuroyami/KiteQR).
