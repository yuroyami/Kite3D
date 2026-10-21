/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/FrustumArray.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

/**
 * Holds a clip-space [coordinateSystem]. In three.js this class tests objects against
 * several cameras at once.
 *
 * This class has no intersection tests: three.js's versions need camera arrays and
 * scene objects, which Kite3D does not have. For each camera, build a [Frustum] with
 * [Frustum.setFromProjectionMatrix] and test against it instead.
 *
 * Instances are **mutable** and **not thread-safe**. Use each instance from one
 * thread only.
 */
public class FrustumArray {

    /**
     * The coordinate system to use. Defaults to [CoordinateSystem.WebGL] (three.js
     * `WebGLCoordinateSystem`).
     */
    public var coordinateSystem: CoordinateSystem = CoordinateSystem.WebGL

    /**
     * Returns a new [FrustumArray] with default values. Like three.js, it does
     * **not** copy [coordinateSystem].
     *
     * @return A new instance.
     */
    public fun clone(): FrustumArray = FrustumArray()
}
