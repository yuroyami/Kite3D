<div class="kite-hero" markdown>

# Kite3D

three.js's `src/math` layer, ported file for file to common Kotlin. Vectors,
matrices, quaternions, bounding volumes and colour — on every Kotlin
Multiplatform target, with `kotlin-stdlib` as its only dependency.

<div class="kite-hero-actions" markdown>
[Get started](#install){ .kite-primary }
[API reference](api/)
[GitHub](https://github.com/yuroyami/Kite3D)
</div>

</div>

Kite3D is the maths, not the engine. There is no `Object3D`, no scene graph, no
geometry and no renderer — so if you are looking for something that draws a
cube, this is not it. If you are writing a renderer, a physics step, a layout
solver or a CAD tool and you need the same linear algebra on Android, iOS,
desktop, the web and native, it is exactly the layer you want.

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

## The two conventions that will trip you up

Everything else follows from these.

### Query methods write into a target you supply

There is no allocating overload. `box.getCenter()` does not compile; you pass
the object that receives the result, and it is also the return value.

```kotlin
val centre = Vector3()
box.getCenter(centre)          // centre now holds the result
val same = box.getCenter(centre) === centre   // true
```

This is three.js's convention, kept deliberately: it lets a hot loop reuse one
scratch vector instead of allocating per frame.

### Methods mutate the receiver and return it

```kotlin
val v = Vector3(1.0, 2.0, 3.0)
v.normalize().multiplyScalar(5.0)   // v itself changed, twice
```

Call `clone()` when you need a copy. `Matrix4().set(…)` takes its sixteen
arguments in row-major order even though the backing array is column-major, so a
literal in source reads the way you would write the matrix on paper.

## A first program

```kotlin
import io.github.yuroyami.kite3d.math.Box3
import io.github.yuroyami.kite3d.math.Frustum
import io.github.yuroyami.kite3d.math.Matrix4
import io.github.yuroyami.kite3d.math.Vector3

// A perspective projection, then the frustum it describes.
val projection = Matrix4().makePerspective(-1.0, 1.0, 1.0, -1.0, 1.0, 100.0)
val frustum = Frustum().setFromProjectionMatrix(projection)

// Is this box inside the view?
val box = Box3(Vector3(0.0, 0.0, 0.0), Vector3(1.0, 1.0, 1.0))
println(frustum.intersectsBox(box))   // false — it sits behind the near plane

box.translate(Vector3(-1.0001, -1.0001, -1.0001))
println(frustum.intersectsBox(box))   // true
```

There is no facade object and no initialisation step. Import the package and
construct what you need.

## Where to go next

The [API reference](api/) is generated from the source and covers every type.
The [README](https://github.com/yuroyami/Kite3D#readme) has task-shaped examples
for transforms, ray casting, orbiting a point, colour blending and keyframe
interpolation, plus the full target list and the current limits.

## Coordinate systems

`makePerspective`, `makeOrthographic` and `Frustum.setFromProjectionMatrix` all
take a `CoordinateSystem`, defaulting to `CoordinateSystem.WebGL` (clip-space
depth from −1 to 1). Pass `CoordinateSystem.WebGPU` for the 0-to-1 convention
that Metal, Vulkan and WebGPU use. Getting this wrong is the usual cause of
geometry that culls at the wrong distance.

## Fidelity

The port is checked against three.js's own unit tests: 452 of them run on JVM,
JS (Node), macOS native, Wasm/JS and Wasm/WASI, plus a JVM-only concurrency test
that runs 32 threads through the shared types to prove no file-level mutable
scratch state survived the translation.
