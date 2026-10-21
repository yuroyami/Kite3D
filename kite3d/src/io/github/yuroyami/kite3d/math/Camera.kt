/*
 * Copyright (c) 2026 yuroyami - MIT.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.math.tan

/**
 * A view matrix and a projection matrix, used to project world points to the screen.
 *
 * Set the projection with one of the `set` functions, place the camera with [lookAt], then call
 * [updateMatrices]. Call it again after any change. [project] allocates nothing, because it only
 * reads the combined matrix.
 * Results are normalised device coordinates, minus one to one on each axis, with y up. The
 * camera looks down its own negative z, the three.js convention, so a point in front of a camera
 * at z = 10 has a smaller z than 10.
 */
public class Camera {
    public val position: Vector3 = Vector3(0.0, 0.0, 1.0)
    public val target: Vector3 = Vector3()
    public val up: Vector3 = Vector3(0.0, 1.0, 0.0)
    public val projectionMatrix: Matrix4 = Matrix4()
    public val viewMatrix: Matrix4 = Matrix4()
    public val viewProjectionMatrix: Matrix4 = Matrix4()
    private val world: Matrix4 = Matrix4()

    public fun setPerspective(
        left: Double,
        right: Double,
        top: Double,
        bottom: Double,
        near: Double,
        far: Double,
    ): Camera {
        projectionMatrix.makePerspective(left, right, top, bottom, near, far)
        return this
    }

    /** Vertical field of view in degrees; [aspect] is width over height. */
    public fun setPerspectiveFov(fovDegrees: Double, aspect: Double, near: Double, far: Double): Camera {
        val top = near * tan(fovDegrees * 0.5 * PI / 180.0)
        val right = top * aspect
        return setPerspective(-right, right, top, -top, near, far)
    }

    public fun setOrthographic(
        left: Double,
        right: Double,
        top: Double,
        bottom: Double,
        near: Double,
        far: Double,
    ): Camera {
        projectionMatrix.makeOrthographic(left, right, top, bottom, near, far)
        return this
    }

    public fun lookAt(eye: Vector3, target: Vector3, up: Vector3): Camera {
        this.position.copy(eye)
        this.target.copy(target)
        this.up.copy(up)
        return this
    }

    /** Rebuilds the view and the combined matrix from [position], [target], [up], and the projection. */
    public fun updateMatrices(): Camera {
        world.identity()
        world.lookAt(position, target, up)
        world.setPosition(position)
        viewMatrix.copy(world).invert()
        viewProjectionMatrix.multiplyMatrices(projectionMatrix, viewMatrix)
        return this
    }

    /** [point] in world space to normalised device coordinates, written into [target]. */
    public fun project(point: Vector3, target: Vector3): Vector3 =
        target.copy(point).applyMatrix4(viewProjectionMatrix)

    /**
     * The same projection for callers that draw in floats. Writes x, y, z into [target] from
     * [offset]. Nothing is allocated, so it can run once per vertex per frame.
     */
    public fun project(x: Float, y: Float, z: Float, target: FloatArray, offset: Int = 0) {
        val e = viewProjectionMatrix.elements
        val px = x.toDouble()
        val py = y.toDouble()
        val pz = z.toDouble()
        val w = 1.0 / (e[3] * px + e[7] * py + e[11] * pz + e[15])
        target[offset] = ((e[0] * px + e[4] * py + e[8] * pz + e[12]) * w).toFloat()
        target[offset + 1] = ((e[1] * px + e[5] * py + e[9] * pz + e[13]) * w).toFloat()
        target[offset + 2] = ((e[2] * px + e[6] * py + e[10] * pz + e[14]) * w).toFloat()
    }
}
