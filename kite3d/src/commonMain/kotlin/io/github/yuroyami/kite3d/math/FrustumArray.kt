/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/FrustumArray.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

/**
 * FrustumArray is used to determine if an object is visible in at least one camera
 * from an array of cameras. This is particularly useful for multi-view renderers.
 *
 * Instances are **mutable** and **not thread-safe**; confine an instance to a single
 * thread, exactly as in three.js.
 *
 * Note: every intersection method of this class drives a [Frustum] from an
 * `ArrayCamera` (reading each camera's `projectionMatrix`, `matrixWorldInverse`,
 * `coordinateSystem` and `reversedDepth`), and further delegates to
 * `Frustum.intersectsObject` / `intersectsSprite`. Those core camera/object types
 * are not part of the math layer, so the intersection methods are DEFERRED (see
 * port-ledger.yaml) and will be reintroduced in the owning layer once core lands.
 * FrustumArray has no upstream test file, so nothing is skipped test-side. Only the
 * [coordinateSystem] field and [clone] are portable today.
 */
public class FrustumArray {

    /**
     * The coordinate system to use. Defaults to [CoordinateSystem.WebGL] (three.js
     * `WebGLCoordinateSystem`).
     */
    public var coordinateSystem: CoordinateSystem = CoordinateSystem.WebGL

    // DEFERRED (need core ArrayCamera + Object3D/Sprite/Sphere/Box3 traversal):
    //   intersectsObject(object, cameraArray)
    //   intersectsSprite(sprite, cameraArray)
    //   intersectsSphere(sphere, cameraArray)
    //   intersectsBox(box, cameraArray)
    //   containsPoint(point, cameraArray)
    // Each iterates cameraArray.cameras, builds a projection matrix from
    // camera.projectionMatrix * camera.matrixWorldInverse, calls
    // Frustum.setFromProjectionMatrix, then a Frustum intersection test. Reintroduce
    // once the core camera layer exists. See port-ledger.yaml.

    /**
     * Returns a new frustum array with copied values from this instance.
     *
     * Faithful to three.js, which returns a freshly constructed [FrustumArray]
     * (it does not copy [coordinateSystem]).
     *
     * @return A clone of this instance.
     */
    public fun clone(): FrustumArray = FrustumArray()
}
