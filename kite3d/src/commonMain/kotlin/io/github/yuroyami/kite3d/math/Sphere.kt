/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Sphere.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * An analytical 3D sphere defined by a center and radius. This class is mainly
 * used as a Bounding Sphere for 3D objects.
 *
 * The sphere is **mutable** and **not thread-safe**; confine an instance (and any
 * object graph holding it) to a single thread, exactly as in three.js.
 *
 * The constructor stores the given [center] **by reference** (it does not copy
 * it). Sharing that vector with another object means later mutations are visible
 * through every alias; use [set] or [clone] for independent storage.
 *
 * A sphere with a negative [radius] is considered empty (see [isEmpty]); a radius
 * of `0` contains only the center point and is **not** empty.
 */
public class Sphere(
    /** The center of the sphere. */
    public val center: Vector3 = Vector3(),
    /** The radius of the sphere. */
    public var radius: Double = -1.0,
) {

    /**
     * Sets the sphere's components by copying the given values.
     *
     * @param center The center.
     * @param radius The radius.
     * @return A reference to this sphere.
     */
    public fun set(center: Vector3, radius: Double): Sphere {
        this.center.copy(center)
        this.radius = radius

        return this
    }

    /**
     * Computes the minimum bounding sphere for a list of [points]. If the optional
     * [optionalCenter] is given, it is used as the sphere's center. Otherwise, the
     * center of the axis-aligned bounding box encompassing the points is calculated.
     *
     * @param points A list of points in 3D space.
     * @param optionalCenter The center of the sphere.
     * @return A reference to this sphere.
     */
    public fun setFromPoints(points: List<Vector3>, optionalCenter: Vector3? = null): Sphere {
        val center = this.center

        if (optionalCenter != null) {
            center.copy(optionalCenter)
        } else {
            // three.js uses a module-level _box scratch Box3; allocated locally.
            Box3().setFromPoints(points).getCenter(center)
        }

        var maxRadiusSq = 0.0

        for (i in 0 until points.size) {
            maxRadiusSq = maxOf(maxRadiusSq, center.distanceToSquared(points[i]))
        }

        radius = sqrt(maxRadiusSq)

        return this
    }

    /**
     * Copies the values of the given sphere [sphere] to this instance.
     *
     * @return A reference to this sphere.
     */
    public fun copy(sphere: Sphere): Sphere {
        center.copy(sphere.center)
        radius = sphere.radius

        return this
    }

    /**
     * Returns `true` if the sphere is empty (the radius set to a negative number).
     *
     * Spheres with a radius of `0` contain only their center point and are not
     * considered to be empty.
     *
     * @return Whether this sphere is empty or not.
     */
    public fun isEmpty(): Boolean =
        radius < 0

    /**
     * Makes this sphere empty which means it encloses a zero space in 3D.
     *
     * @return A reference to this sphere.
     */
    public fun makeEmpty(): Sphere {
        center.set(0.0, 0.0, 0.0)
        radius = -1.0

        return this
    }

    /**
     * Returns `true` if this sphere contains the given [point] inclusive of the
     * surface of the sphere.
     *
     * @return Whether this sphere contains the given point or not.
     */
    public fun containsPoint(point: Vector3): Boolean =
        point.distanceToSquared(center) <= (radius * radius)

    /**
     * Returns the closest distance from the boundary of the sphere to the given
     * [point]. If the sphere contains the point, the distance will be negative.
     *
     * @return The distance to the point.
     */
    public fun distanceToPoint(point: Vector3): Double =
        point.distanceTo(center) - radius

    /**
     * Returns `true` if this sphere intersects with the given one [sphere].
     *
     * @return Whether this sphere intersects with the given one or not.
     */
    public fun intersectsSphere(sphere: Sphere): Boolean {
        val radiusSum = radius + sphere.radius

        return sphere.center.distanceToSquared(center) <= (radiusSum * radiusSum)
    }

    /**
     * Returns `true` if this sphere intersects with the given [box].
     *
     * @return Whether this sphere intersects with the given box or not.
     */
    public fun intersectsBox(box: Box3): Boolean =
        box.intersectsSphere(this)

    /**
     * Returns `true` if this sphere intersects with the given [plane].
     *
     * @return Whether this sphere intersects with the given plane or not.
     */
    public fun intersectsPlane(plane: Plane): Boolean =
        abs(plane.distanceToPoint(center)) <= radius

    /**
     * Clamps a [point] within the sphere, writing the result into [target]. If the
     * point is outside the sphere, it is clamped to the closest point on the edge
     * of the sphere. Points already inside the sphere are not affected.
     *
     * @return [target].
     */
    public fun clampPoint(point: Vector3, target: Vector3): Vector3 {
        val deltaLengthSq = center.distanceToSquared(point)

        target.copy(point)

        if (deltaLengthSq > (radius * radius)) {
            target.sub(center).normalize()
            target.multiplyScalar(radius).add(center)
        }

        return target
    }

    /**
     * Writes a bounding box that encloses this sphere into [target].
     *
     * @return [target].
     */
    public fun getBoundingBox(target: Box3): Box3 {
        if (isEmpty()) {
            // Empty sphere produces empty bounding box
            target.makeEmpty()
            return target
        }

        target.set(center, center)
        target.expandByScalar(radius)

        return target
    }

    /**
     * Transforms this sphere with the given 4x4 transformation matrix [matrix].
     *
     * @return A reference to this sphere.
     */
    public fun applyMatrix4(matrix: Matrix4): Sphere {
        center.applyMatrix4(matrix)
        radius = radius * matrix.getMaxScaleOnAxis()

        return this
    }

    /**
     * Translates the sphere's center by the given [offset].
     *
     * @return A reference to this sphere.
     */
    public fun translate(offset: Vector3): Sphere {
        center.add(offset)

        return this
    }

    /**
     * Expands the boundaries of this sphere to include the given [point].
     *
     * @return A reference to this sphere.
     */
    public fun expandByPoint(point: Vector3): Sphere {
        if (isEmpty()) {
            center.copy(point)

            radius = 0.0

            return this
        }

        // three.js uses a module-level _v1 scratch vector; allocated locally.
        val v1 = Vector3().subVectors(point, center)

        val lengthSq = v1.lengthSq()

        if (lengthSq > (radius * radius)) {
            // calculate the minimal sphere

            val length = sqrt(lengthSq)

            val delta = (length - radius) * 0.5

            center.addScaledVector(v1, delta / length)

            radius += delta
        }

        return this
    }

    /**
     * Expands this sphere to enclose both the original sphere and the given [sphere].
     *
     * @return A reference to this sphere.
     */
    public fun union(sphere: Sphere): Sphere {
        if (sphere.isEmpty()) {
            return this
        }

        if (isEmpty()) {
            copy(sphere)

            return this
        }

        if (center.equals(sphere.center)) {
            radius = maxOf(radius, sphere.radius)
        } else {
            // three.js uses module-level _v1/_v2 scratch vectors; allocated locally.
            val v2 = Vector3().subVectors(sphere.center, center).setLength(sphere.radius)

            expandByPoint(Vector3().copy(sphere.center).add(v2))

            expandByPoint(Vector3().copy(sphere.center).sub(v2))
        }

        return this
    }

    /**
     * Returns a new sphere with copied values from this instance.
     *
     * @return A clone of this instance.
     */
    public fun clone(): Sphere = Sphere().copy(this)

    // --- JSON round-trip ------------------------------------------------------
    // three.js returns/consumes a plain JS object; common Kotlin has no such JSON
    // value type at this layer, so these are exposed as array round-trips. They use
    // the same shape as Box3.toJSON/fromJSON.

    /**
     * Writes this sphere as `[centerX, centerY, centerZ, radius]`.
     *
     * Mirrors three.js `toJSON()` (which produces `{ radius, center:
     * center.toArray() }`); flattened here since the math layer has no JS object
     * type. Center first, so the leading three components are exactly what
     * [Vector3.toArray] would write.
     *
     * @return Serialized structure with fields representing the object state.
     */
    public fun toJSON(): DoubleArray = doubleArrayOf(center.x, center.y, center.z, radius)

    /**
     * Sets this sphere from [json] laid out as `[centerX, centerY, centerZ, radius]`.
     *
     * @return A reference to this sphere.
     */
    public fun fromJSON(json: DoubleArray): Sphere {
        center.fromArray(json, 0)
        radius = json[3]
        return this
    }

    /**
     * Structural equality: `true` when [other] is a [Sphere] with an equal [center]
     * (via [Vector3.equals]) and an equal [radius] (primitive `==`, so `NaN != NaN`).
     */
    override fun equals(other: Any?): Boolean =
        other is Sphere && other.center == center && other.radius == radius

    override fun hashCode(): Int {
        // (radius + 0.0) collapses -0.0 to +0.0, keeping hashCode consistent with
        // equals treating -0.0 == +0.0.
        var result = center.hashCode()
        result = 31 * result + (radius + 0.0).hashCode()
        return result
    }

    override fun toString(): String = "Sphere(center=$center, radius=$radius)"
}
