/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Vector4.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.math.truncate

/**
 * A 4D vector: an ordered quadruplet of numbers (`x`, `y`, `z`, `w`).
 *
 * A 4D vector can represent a point in 4D space, a direction and length in 4D
 * space (the length being the Euclidean distance from `(0, 0, 0, 0)` to
 * `(x, y, z, w)`), or any arbitrary ordered quadruplet of numbers.
 *
 * Vectors are **mutable** and **not thread-safe**; confine an instance (and any
 * object graph holding it) to a single thread, exactly as in three.js. Most
 * methods mutate `this` and return it for chaining.
 *
 * Iterating a vector yields its components `(x, y, z, w)` in order.
 *
 * Note: the [w] component defaults to `1.0` (matching three.js), unlike the
 * other three components which default to `0.0`.
 */
public class Vector4(
    /** The x value of this vector. */
    public var x: Double = 0.0,
    /** The y value of this vector. */
    public var y: Double = 0.0,
    /** The z value of this vector. */
    public var z: Double = 0.0,
    /** The w value of this vector. */
    public var w: Double = 1.0,
) : Iterable<Double> {

    /** Alias for [z]. */
    public var width: Double
        get() = z
        set(value) { z = value }

    /** Alias for [w]. */
    public var height: Double
        get() = w
        set(value) { w = value }

    /**
     * Sets the vector components.
     *
     * @param x The value of the x component.
     * @param y The value of the y component.
     * @param z The value of the z component.
     * @param w The value of the w component.
     * @return A reference to this vector.
     */
    public fun set(x: Double, y: Double, z: Double, w: Double): Vector4 {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    /**
     * Sets all vector components to [scalar].
     *
     * @return A reference to this vector.
     */
    public fun setScalar(scalar: Double): Vector4 {
        x = scalar
        y = scalar
        z = scalar
        w = scalar
        return this
    }

    /**
     * Sets the vector's x component.
     *
     * @param x The value to set.
     * @return A reference to this vector.
     */
    public fun setX(x: Double): Vector4 {
        this.x = x
        return this
    }

    /**
     * Sets the vector's y component.
     *
     * @param y The value to set.
     * @return A reference to this vector.
     */
    public fun setY(y: Double): Vector4 {
        this.y = y
        return this
    }

    /**
     * Sets the vector's z component.
     *
     * @param z The value to set.
     * @return A reference to this vector.
     */
    public fun setZ(z: Double): Vector4 {
        this.z = z
        return this
    }

    /**
     * Sets the vector's w component.
     *
     * @param w The value to set.
     * @return A reference to this vector.
     */
    public fun setW(w: Double): Vector4 {
        this.w = w
        return this
    }

    /**
     * Sets the component at [index] (`0` = x, `1` = y, `2` = z, `3` = w) to [value].
     *
     * @throws IllegalArgumentException if [index] is out of range.
     * @return A reference to this vector.
     */
    public fun setComponent(index: Int, value: Double): Vector4 {
        when (index) {
            0 -> x = value
            1 -> y = value
            2 -> z = value
            3 -> w = value
            else -> throw IllegalArgumentException("index is out of range: $index")
        }
        return this
    }

    /**
     * Returns the component at [index] (`0` = x, `1` = y, `2` = z, `3` = w).
     *
     * @throws IllegalArgumentException if [index] is out of range.
     *
     * @return A vector component value.
     */
    public fun getComponent(index: Int): Double = when (index) {
        0 -> x
        1 -> y
        2 -> z
        3 -> w
        else -> throw IllegalArgumentException("index is out of range: $index")
    }

    /**
     * Returns a new vector with the same components.
     *
     * @return A clone of this instance.
     */
    public fun clone(): Vector4 = Vector4(x, y, z, w)

    /**
     * Copies the components of [v] into this instance.
     *
     * @return A reference to this vector.
     */
    public fun copy(v: Vector4): Vector4 {
        x = v.x
        y = v.y
        z = v.z
        // Upstream guards `v.w` with `(v.w !== undefined) ? v.w : 1` to accept a
        // Vector3; in Kotlin the parameter is a Vector4 whose w is always defined.
        w = v.w
        return this
    }

    /**
     * Adds [v] to this instance.
     *
     * @return A reference to this vector.
     */
    public fun add(v: Vector4): Vector4 {
        x += v.x
        y += v.y
        z += v.z
        w += v.w
        return this
    }

    /**
     * Adds the scalar [s] to all components.
     *
     * @return A reference to this vector.
     */
    public fun addScalar(s: Double): Vector4 {
        x += s
        y += s
        z += s
        w += s
        return this
    }

    /**
     * Sets this vector to `a + b`.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return A reference to this vector.
     */
    public fun addVectors(a: Vector4, b: Vector4): Vector4 {
        x = a.x + b.x
        y = a.y + b.y
        z = a.z + b.z
        w = a.w + b.w
        return this
    }

    /**
     * Adds [v] scaled by [s] to this instance.
     *
     * @return A reference to this vector.
     */
    public fun addScaledVector(v: Vector4, s: Double): Vector4 {
        x += v.x * s
        y += v.y * s
        z += v.z * s
        w += v.w * s
        return this
    }

    /**
     * Subtracts [v] from this instance.
     *
     * @return A reference to this vector.
     */
    public fun sub(v: Vector4): Vector4 {
        x -= v.x
        y -= v.y
        z -= v.z
        w -= v.w
        return this
    }

    /**
     * Subtracts the scalar [s] from all components.
     *
     * @return A reference to this vector.
     */
    public fun subScalar(s: Double): Vector4 {
        x -= s
        y -= s
        z -= s
        w -= s
        return this
    }

    /**
     * Sets this vector to `a - b`.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return A reference to this vector.
     */
    public fun subVectors(a: Vector4, b: Vector4): Vector4 {
        x = a.x - b.x
        y = a.y - b.y
        z = a.z - b.z
        w = a.w - b.w
        return this
    }

    /**
     * Multiplies this instance component-wise by [v].
     *
     * @return A reference to this vector.
     */
    public fun multiply(v: Vector4): Vector4 {
        x *= v.x
        y *= v.y
        z *= v.z
        w *= v.w
        return this
    }

    /**
     * Multiplies all components by [scalar].
     *
     * @return A reference to this vector.
     */
    public fun multiplyScalar(scalar: Double): Vector4 {
        x *= scalar
        y *= scalar
        z *= scalar
        w *= scalar
        return this
    }

    /**
     * Multiplies this vector by the 4x4 matrix [m].
     *
     * @return A reference to this vector.
     */
    public fun applyMatrix4(m: Matrix4): Vector4 {
        val x = this.x
        val y = this.y
        val z = this.z
        val w = this.w
        val e = m.elements

        this.x = e[0] * x + e[4] * y + e[8] * z + e[12] * w
        this.y = e[1] * x + e[5] * y + e[9] * z + e[13] * w
        this.z = e[2] * x + e[6] * y + e[10] * z + e[14] * w
        this.w = e[3] * x + e[7] * y + e[11] * z + e[15] * w

        return this
    }

    /**
     * Divides this instance component-wise by [v].
     *
     * @return A reference to this vector.
     */
    public fun divide(v: Vector4): Vector4 {
        x /= v.x
        y /= v.y
        z /= v.z
        w /= v.w
        return this
    }

    /**
     * Divides all components by [scalar].
     *
     * @return A reference to this vector.
     */
    public fun divideScalar(scalar: Double): Vector4 = multiplyScalar(1.0 / scalar)

    /**
     * Sets the `x`, `y` and `z` components of this vector to the quaternion [q]'s
     * axis and `w` to the angle.
     *
     * @return A reference to this vector.
     */
    public fun setAxisAngleFromQuaternion(q: Quaternion): Vector4 {
        // http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToAngle/index.htm

        // q is assumed to be normalized

        w = 2 * acos(q.w)

        val s = sqrt(1 - q.w * q.w)

        if (s < 0.0001) {
            x = 1.0
            y = 0.0
            z = 0.0
        } else {
            x = q.x / s
            y = q.y / s
            z = q.z / s
        }

        return this
    }

    /**
     * Sets the `x`, `y` and `z` components of this vector to the axis of rotation
     * and `w` to the angle, from the 4x4 matrix [m] (whose upper-left 3x3 is
     * assumed to be a pure rotation matrix).
     *
     * @return A reference to this vector.
     */
    public fun setAxisAngleFromRotationMatrix(m: Matrix4): Vector4 {
        // http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToAngle/index.htm

        // assumes the upper 3x3 of m is a pure rotation matrix (i.e, unscaled)

        val angle: Double
        var x: Double
        var y: Double
        var z: Double // variables for result
        val epsilon = 0.01 // margin to allow for rounding errors
        val epsilon2 = 0.1 // margin to distinguish between 0 and 180 degrees

        val te = m.elements

        val m11 = te[0]
        val m12 = te[4]
        val m13 = te[8]
        val m21 = te[1]
        val m22 = te[5]
        val m23 = te[9]
        val m31 = te[2]
        val m32 = te[6]
        val m33 = te[10]

        if (abs(m12 - m21) < epsilon &&
            abs(m13 - m31) < epsilon &&
            abs(m23 - m32) < epsilon
        ) {

            // singularity found
            // first check for identity matrix which must have +1 for all terms
            // in leading diagonal and zero in other terms

            if (abs(m12 + m21) < epsilon2 &&
                abs(m13 + m31) < epsilon2 &&
                abs(m23 + m32) < epsilon2 &&
                abs(m11 + m22 + m33 - 3) < epsilon2
            ) {

                // this singularity is identity matrix so angle = 0

                set(1.0, 0.0, 0.0, 0.0)

                return this // zero angle, arbitrary axis
            }

            // otherwise this singularity is angle = 180

            angle = PI

            val xx = (m11 + 1) / 2
            val yy = (m22 + 1) / 2
            val zz = (m33 + 1) / 2
            val xy = (m12 + m21) / 4
            val xz = (m13 + m31) / 4
            val yz = (m23 + m32) / 4

            if (xx > yy && xx > zz) {

                // m11 is the largest diagonal term

                if (xx < epsilon) {
                    x = 0.0
                    y = 0.707106781
                    z = 0.707106781
                } else {
                    x = sqrt(xx)
                    y = xy / x
                    z = xz / x
                }
            } else if (yy > zz) {

                // m22 is the largest diagonal term

                if (yy < epsilon) {
                    x = 0.707106781
                    y = 0.0
                    z = 0.707106781
                } else {
                    y = sqrt(yy)
                    x = xy / y
                    z = yz / y
                }
            } else {

                // m33 is the largest diagonal term so base result on this

                if (zz < epsilon) {
                    x = 0.707106781
                    y = 0.707106781
                    z = 0.0
                } else {
                    z = sqrt(zz)
                    x = xz / z
                    y = yz / z
                }
            }

            set(x, y, z, angle)

            return this // return 180 deg rotation
        }

        // as we have reached here there are no singularities so we can handle normally

        var s = sqrt(
            (m32 - m23) * (m32 - m23) +
                (m13 - m31) * (m13 - m31) +
                (m21 - m12) * (m21 - m12),
        ) // used to normalize

        if (abs(s) < 0.001) s = 1.0

        // prevent divide by zero, should not happen if matrix is orthogonal and should be
        // caught by singularity test above, but I've left it in just in case

        this.x = (m32 - m23) / s
        this.y = (m13 - m31) / s
        this.z = (m21 - m12) / s
        this.w = acos((m11 + m22 + m33 - 1) / 2)

        return this
    }

    /**
     * Sets the vector components to the position elements of the transformation
     * matrix [m].
     *
     * @return A reference to this vector.
     */
    public fun setFromMatrixPosition(m: Matrix4): Vector4 {
        val e = m.elements

        x = e[12]
        y = e[13]
        z = e[14]
        w = e[15]

        return this
    }

    /**
     * Sets each component to the minimum of itself and [v]'s corresponding component.
     *
     * @return A reference to this vector.
     */
    public fun min(v: Vector4): Vector4 {
        x = minOf(x, v.x)
        y = minOf(y, v.y)
        z = minOf(z, v.z)
        w = minOf(w, v.w)
        return this
    }

    /**
     * Sets each component to the maximum of itself and [v]'s corresponding component.
     *
     * @return A reference to this vector.
     */
    public fun max(v: Vector4): Vector4 {
        x = maxOf(x, v.x)
        y = maxOf(y, v.y)
        z = maxOf(z, v.z)
        w = maxOf(w, v.w)
        return this
    }

    /**
     * Clamps each component into the range `[min, max]` (assumed component-wise
     * `min <= max`).
     *
     * @param min The minimum x, y and z values.
     * @param max The maximum x, y and z values in the desired range.
     * @return A reference to this vector.
     */
    public fun clamp(min: Vector4, max: Vector4): Vector4 {
        // assumes min < max, componentwise
        x = MathUtils.clamp(x, min.x, max.x)
        y = MathUtils.clamp(y, min.y, max.y)
        z = MathUtils.clamp(z, min.z, max.z)
        w = MathUtils.clamp(w, min.w, max.w)
        return this
    }

    /**
     * Clamps each component into the scalar range `[minVal, maxVal]`.
     *
     * @param minVal The minimum value the components will be clamped to.
     * @param maxVal The maximum value the components will be clamped to.
     * @return A reference to this vector.
     */
    public fun clampScalar(minVal: Double, maxVal: Double): Vector4 {
        x = MathUtils.clamp(x, minVal, maxVal)
        y = MathUtils.clamp(y, minVal, maxVal)
        z = MathUtils.clamp(z, minVal, maxVal)
        w = MathUtils.clamp(w, minVal, maxVal)
        return this
    }

    /**
     * Clamps this vector's length into `[min, max]`.
     *
     * @param min The minimum value the vector length will be clamped to.
     * @param max The maximum value the vector length will be clamped to.
     * @return A reference to this vector.
     */
    public fun clampLength(min: Double, max: Double): Vector4 {
        val length = length()
        return divideScalar(length.orOne()).multiplyScalar(MathUtils.clamp(length, min, max))
    }

    /**
     * Rounds each component down to the nearest integer.
     *
     * @return A reference to this vector.
     */
    public fun floor(): Vector4 {
        x = floor(x)
        y = floor(y)
        z = floor(z)
        w = floor(w)
        return this
    }

    /**
     * Rounds each component up to the nearest integer.
     *
     * @return A reference to this vector.
     */
    public fun ceil(): Vector4 {
        x = ceil(x)
        y = ceil(y)
        z = ceil(z)
        w = ceil(w)
        return this
    }

    /**
     * Rounds each component to the nearest integer (halves toward +infinity, as
     * JS `Math.round`).
     *
     * @return A reference to this vector.
     */
    public fun round(): Vector4 {
        x = MathUtils.jsRound(x)
        y = MathUtils.jsRound(y)
        z = MathUtils.jsRound(z)
        w = MathUtils.jsRound(w)
        return this
    }

    /**
     * Rounds each component toward zero.
     *
     * @return A reference to this vector.
     */
    public fun roundToZero(): Vector4 {
        x = truncate(x)
        y = truncate(y)
        z = truncate(z)
        w = truncate(w)
        return this
    }

    /**
     * Negates each component.
     *
     * @return A reference to this vector.
     */
    public fun negate(): Vector4 {
        x = -x
        y = -y
        z = -z
        w = -w
        return this
    }

    /**
     * Returns the dot product of this vector and [v].
     *
     * @return The result of the dot product.
     */
    public fun dot(v: Vector4): Double = x * v.x + y * v.y + z * v.z + w * v.w

    /**
     * Returns the squared Euclidean length of this vector.
     *
     * @return The square length of this vector.
     */
    public fun lengthSq(): Double = x * x + y * y + z * z + w * w

    /**
     * Returns the Euclidean length of this vector.
     *
     * @return The length of this vector.
     */
    public fun length(): Double = sqrt(x * x + y * y + z * z + w * w)

    /**
     * Returns the Manhattan length of this vector.
     *
     * @return The length of this vector.
     */
    public fun manhattanLength(): Double = abs(x) + abs(y) + abs(z) + abs(w)

    /**
     * Normalizes this vector to unit length (leaves a zero vector unchanged).
     *
     * @return A reference to this vector.
     */
    public fun normalize(): Vector4 = divideScalar(length().orOne())

    /**
     * Sets this vector's length to [length], preserving direction.
     *
     * @return A reference to this vector.
     */
    public fun setLength(length: Double): Vector4 = normalize().multiplyScalar(length)

    /**
     * Linearly interpolates from this vector toward [v] by [alpha].
     *
     * @return A reference to this vector.
     */
    public fun lerp(v: Vector4, alpha: Double): Vector4 {
        x += (v.x - x) * alpha
        y += (v.y - y) * alpha
        z += (v.z - z) * alpha
        w += (v.w - w) * alpha
        return this
    }

    /**
     * Sets this vector to the linear interpolation of [v1] and [v2] by [alpha].
     *
     * @return A reference to this vector.
     */
    public fun lerpVectors(v1: Vector4, v2: Vector4, alpha: Double): Vector4 {
        x = v1.x + (v2.x - v1.x) * alpha
        y = v1.y + (v2.y - v1.y) * alpha
        z = v1.z + (v2.z - v1.z) * alpha
        w = v1.w + (v2.w - v1.w) * alpha
        return this
    }

    /**
     * Sets `x = array[offset]`, `y = array[offset + 1]`, `z = array[offset + 2]`,
     * `w = array[offset + 3]`.
     *
     * @param array An array holding the vector component values.
     * @return A reference to this vector.
     */
    public fun fromArray(array: DoubleArray, offset: Int = 0): Vector4 {
        x = array[offset]
        y = array[offset + 1]
        z = array[offset + 2]
        w = array[offset + 3]
        return this
    }

    /**
     * Writes this vector's components into [array] at [offset], growing the list as
     * needed, and returns it.
     *
     * @return The vector components.
     */
    public fun toArray(array: MutableList<Double> = mutableListOf(), offset: Int = 0): MutableList<Double> {
        while (array.size < offset + 4) array.add(0.0)
        array[offset] = x
        array[offset + 1] = y
        array[offset + 2] = z
        array[offset + 3] = w
        return array
    }

    /**
     * Sets this vector's components from [attribute] at [index].
     *
     * @return A reference to this vector.
     */
    public fun fromBufferAttribute(attribute: AttributeLike, index: Int): Vector4 {
        x = attribute.getX(index)
        y = attribute.getY(index)
        z = attribute.getZ(index)
        w = attribute.getW(index)
        return this
    }

    /**
     * Sets each component to a pseudo-random value in `[0, 1)`.
     *
     * @return A reference to this vector.
     */
    public fun random(): Vector4 {
        x = kotlin.random.Random.nextDouble()
        y = kotlin.random.Random.nextDouble()
        z = kotlin.random.Random.nextDouble()
        w = kotlin.random.Random.nextDouble()
        return this
    }

    override fun iterator(): Iterator<Double> = listOf(x, y, z, w).iterator()

    /**
     * Structural equality: `true` when [other] is a [Vector4] with equal components
     * (component-wise `==`, so `NaN != NaN`).
     */
    override fun equals(other: Any?): Boolean =
        other is Vector4 && other.x == x && other.y == y && other.z == z && other.w == w

    override fun hashCode(): Int {
        // (v + 0.0) collapses -0.0 to +0.0, keeping hashCode consistent with
        // equals treating -0.0 == +0.0.
        var result = (x + 0.0).hashCode()
        result = 31 * result + (y + 0.0).hashCode()
        result = 31 * result + (z + 0.0).hashCode()
        result = 31 * result + (w + 0.0).hashCode()
        return result
    }

    override fun toString(): String = "Vector4(x=$x, y=$y, z=$z, w=$w)"
}
