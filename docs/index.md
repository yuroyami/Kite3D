<div class="kite-hero" markdown>

# Kite3D

3D maths for Kotlin Multiplatform: vectors, matrices, quaternions, bounding
volumes and colour, with the same API as three.js. It runs on every Kotlin
Multiplatform target and depends only on `kotlin-stdlib`.

<div class="kite-hero-actions" markdown>
[Get started](#install){ .kite-primary }
[API reference](api/)
[GitHub](https://github.com/yuroyami/Kite3D)
</div>

</div>

Kite3D gives you the maths, not the engine. It has no `Object3D`, no scene
graph, no geometry and no renderer, so it cannot draw a cube.

Use it when you write a renderer, a physics step, a layout solver or a CAD tool.
You get the same linear algebra on Android, iOS, desktop, the web and native
targets.

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

## The two conventions to learn first

Everything else follows from these.

### Query methods write into a target you supply

There is no allocating overload. `box.getCenter()` does not compile. You pass
the object that receives the result, and it is also the return value.

```kotlin
val centre = Vector3()
box.getCenter(centre)          // centre now holds the result
val same = box.getCenter(centre) === centre   // true
```

This convention lets a loop that runs every frame reuse one scratch vector
instead of allocating a new one each time.

### Methods mutate the receiver and return it

```kotlin
val v = Vector3(1.0, 2.0, 3.0)
v.normalize().multiplyScalar(5.0)   // v itself changed, twice
```

Call `clone()` when you need a copy. `Matrix4().set(…)` takes its sixteen
arguments in row-major order, even though the backing array is column-major. A
literal in your source therefore reads in the same order that you would write
the matrix on paper.

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
println(frustum.intersectsBox(box))   // false: the box is behind the near plane

box.translate(Vector3(-1.0001, -1.0001, -1.0001))
println(frustum.intersectsBox(box))   // true
```

There is no facade object and no initialisation step. Import the package and
construct what you need.

## Where to go next

The [API reference](api/) is generated from the source and covers every type.
The [README](https://github.com/yuroyami/Kite3D#readme) has task-shaped examples
for transforms, ray casting, orbiting a point, colour blending and keyframe
interpolation. It also lists every target and every current limit.

## Coordinate systems

`makePerspective`, `makeOrthographic` and `Frustum.setFromProjectionMatrix` all
take a `CoordinateSystem`. The default is `CoordinateSystem.WebGL`, which puts
clip-space depth in the range -1 to 1. Pass `CoordinateSystem.WebGPU` for the 0
to 1 range that Metal, Vulkan and WebGPU use. The wrong value here is the usual
cause of geometry that culls at the wrong distance.

## Testing

452 tests cover the library in common code. They run on JVM, JS (Node), macOS
native, Wasm/JS and Wasm/WASI. One more test runs on the JVM only. It puts 32
threads through the shared types and checks that no file-level mutable scratch
state exists.
