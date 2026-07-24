/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Plane.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

/**
 * A two dimensional surface that extends infinitely in 3D space, represented
 * in [Hessian normal form](http://mathworld.wolfram.com/HessianNormalForm.html)
 * by a unit length normal vector and a constant.
 *
 * The plane is **mutable** and **not thread-safe**; confine an instance (and any
 * object graph holding it) to a single thread, exactly as in three.js.
 *
 * The constructor stores the given [normal] **by reference** (it does not copy
 * it). Sharing that vector with another object means later mutations are visible
 * through every alias; use [set]/[setComponents] or [clone] for independent storage.
 */
public class Plane(
    /** A unit length vector defining the normal of the plane. */
    public val normal: Vector3 = Vector3(1.0, 0.0, 0.0),
    /** The signed distance from the origin to the plane. */
    public var constant: Double = 0.0,
) {

    /**
     * Sets the plane components by copying the given values.
     *
     * @param normal The normal.
     * @param constant The constant.
     * @return A reference to this plane.
     */
    public fun set(normal: Vector3, constant: Double): Plane {
        this.normal.copy(normal)
        this.constant = constant

        return this
    }

    /**
     * Sets the plane components by defining `x`, `y`, `z` as the plane normal and
     * `w` as the constant.
     *
     * @param x The value for the normal's x component.
     * @param y The value for the normal's y component.
     * @param z The value for the normal's z component.
     * @param w The constant value.
     * @return A reference to this plane.
     */
    public fun setComponents(x: Double, y: Double, z: Double, w: Double): Plane {
        normal.set(x, y, z)
        constant = w

        return this
    }

    /**
     * Sets the plane from the given [normal] and coplanar [point] (a point that
     * lies on the plane).
     *
     * @return A reference to this plane.
     */
    public fun setFromNormalAndCoplanarPoint(normal: Vector3, point: Vector3): Plane {
        this.normal.copy(normal)
        constant = -point.dot(this.normal)

        return this
    }

    /**
     * Sets the plane from three coplanar points [a], [b], [c]. The winding order is
     * assumed to be counter-clockwise, and determines the direction of the plane
     * normal.
     *
     * @return A reference to this plane.
     */
    public fun setFromCoplanarPoints(a: Vector3, b: Vector3, c: Vector3): Plane {
        // three.js uses module-level _vector1/_vector2 scratch vectors; allocated
        // locally here (no file-level mutable state).
        val normal = Vector3().subVectors(c, b).cross(Vector3().subVectors(a, b)).normalize()

        // Q: should an error be thrown if normal is zero (e.g. degenerate plane)?

        setFromNormalAndCoplanarPoint(normal, a)

        return this
    }

    /**
     * Copies the values of the given plane [plane] to this instance.
     *
     * @return A reference to this plane.
     */
    public fun copy(plane: Plane): Plane {
        normal.copy(plane.normal)
        constant = plane.constant

        return this
    }

    /**
     * Normalizes the plane normal and adjusts the constant accordingly.
     *
     * Note: will lead to a divide by zero if the plane is invalid (zero normal).
     *
     * @return A reference to this plane.
     */
    public fun normalize(): Plane {
        val inverseNormalLength = 1.0 / normal.length()
        normal.multiplyScalar(inverseNormalLength)
        constant *= inverseNormalLength

        return this
    }

    /**
     * Negates both the plane normal and the constant.
     *
     * @return A reference to this plane.
     */
    public fun negate(): Plane {
        constant *= -1
        normal.negate()

        return this
    }

    /**
     * Returns the signed distance from the given [point] to this plane.
     *
     * @return The signed distance.
     */
    public fun distanceToPoint(point: Vector3): Double =
        normal.dot(point) + constant

    /**
     * Returns the signed distance from the given [sphere] to this plane.
     *
     * @return The signed distance.
     */
    public fun distanceToSphere(sphere: Sphere): Double =
        distanceToPoint(sphere.center) - sphere.radius

    /**
     * Projects the given [point] onto the plane, writing the result into [target].
     *
     * @return [target].
     */
    public fun projectPoint(point: Vector3, target: Vector3): Vector3 =
        target.copy(point).addScaledVector(normal, -distanceToPoint(point))

    /**
     * Writes the intersection point of the passed [line] and the plane into
     * [target]. Returns `null` if the line does not intersect. Returns the line's
     * starting point if the line is coplanar with the plane.
     *
     * @param line The line to compute the intersection for.
     * @param target The target vector that is used to store the method's result.
     * @param clampToLine Whether to clamp the intersection to the line segment.
     * @return The intersection point, or `null` if no intersection is detected.
     */
    public fun intersectLine(line: Line3, target: Vector3, clampToLine: Boolean = true): Vector3? {
        // three.js uses module-level _vector1 scratch; allocated locally.
        val direction = line.delta(Vector3())

        val denominator = normal.dot(direction)

        if (denominator == 0.0) {
            // line is coplanar, return origin
            if (distanceToPoint(line.start) == 0.0) {
                return target.copy(line.start)
            }

            // Unsure if this is the correct method to handle this case.
            return null
        }

        val t = -(line.start.dot(normal) + constant) / denominator

        if (clampToLine && (t < 0 || t > 1)) {
            return null
        }

        return target.copy(line.start).addScaledVector(direction, t)
    }

    /**
     * Returns `true` if the given line segment [line] intersects with (passes
     * through) the plane.
     *
     * @return Whether the given line segment intersects with the plane or not.
     */
    public fun intersectsLine(line: Line3): Boolean {
        // Note: this tests if a line intersects the plane, not whether it (or its
        // end-points) are coplanar with it.

        val startSign = distanceToPoint(line.start)
        val endSign = distanceToPoint(line.end)

        return (startSign < 0 && endSign > 0) || (endSign < 0 && startSign > 0)
    }

    /**
     * Returns `true` if the given bounding box [box] intersects with the plane.
     *
     * @return Whether the given bounding box intersects with the plane or not.
     */
    public fun intersectsBox(box: Box3): Boolean =
        box.intersectsPlane(this)

    /**
     * Returns `true` if the given bounding sphere [sphere] intersects with the plane.
     *
     * @return Whether the given bounding sphere intersects with the plane or not.
     */
    public fun intersectsSphere(sphere: Sphere): Boolean =
        sphere.intersectsPlane(this)

    /**
     * Writes a coplanar vector to the plane into [target], by calculating the
     * projection of the normal at the origin onto the plane.
     *
     * @return [target].
     */
    public fun coplanarPoint(target: Vector3): Vector3 =
        target.copy(normal).multiplyScalar(-constant)

    /**
     * Applies a 4x4 matrix [matrix] to the plane. The matrix must be an affine,
     * homogeneous transform.
     *
     * The optional normal matrix can be pre-computed like so:
     * ```
     * val optionalNormalMatrix = Matrix3().getNormalMatrix(matrix)
     * ```
     *
     * @param matrix The transformation matrix.
     * @param optionalNormalMatrix A pre-computed normal matrix.
     * @return A reference to this plane.
     */
    public fun applyMatrix4(matrix: Matrix4, optionalNormalMatrix: Matrix3? = null): Plane {
        // three.js uses module-level _normalMatrix/_vector1 scratch; allocated locally.
        val normalMatrix = optionalNormalMatrix ?: Matrix3().getNormalMatrix(matrix)

        val referencePoint = coplanarPoint(Vector3()).applyMatrix4(matrix)

        val normal = normal.applyMatrix3(normalMatrix).normalize()

        constant = -referencePoint.dot(normal)

        return this
    }

    /**
     * Translates the plane by the distance defined by the given [offset] vector.
     * Note that this only affects the plane constant and will not affect the normal
     * vector.
     *
     * @return A reference to this plane.
     */
    public fun translate(offset: Vector3): Plane {
        constant -= offset.dot(normal)

        return this
    }

    /**
     * Returns a new plane with copied values from this instance.
     *
     * @return A clone of this instance.
     */
    public fun clone(): Plane = Plane().copy(this)

    /**
     * Structural equality: `true` when [other] is a [Plane] with an equal [normal]
     * (via [Vector3.equals]) and an equal [constant] (primitive `==`, so
     * `NaN != NaN`).
     */
    override fun equals(other: Any?): Boolean =
        other is Plane && other.normal == normal && other.constant == constant

    override fun hashCode(): Int {
        // (constant + 0.0) collapses -0.0 to +0.0, keeping hashCode consistent with
        // equals treating -0.0 == +0.0.
        var result = normal.hashCode()
        result = 31 * result + (constant + 0.0).hashCode()
        return result
    }

    override fun toString(): String = "Plane(normal=$normal, constant=$constant)"
}
