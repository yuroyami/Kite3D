/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Quaternion.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A quaternion, used in three.js to represent rotations.
 *
 * Iterating through an instance yields its components `(x, y, z, w)` in order.
 *
 * three.js expects quaternions to be normalized.
 *
 * Instances are **mutable** and **not thread-safe**; confine an instance (and any
 * object graph holding it) to a single thread, exactly as in three.js.
 *
 * Reading `x`/`y`/`z`/`w` has no side effect. Writing any of them — or calling a
 * mutating method — fires the registered [onChange] callback (see that method).
 */
public class Quaternion(
    x: Double = 0.0,
    y: Double = 0.0,
    z: Double = 0.0,
    w: Double = 1.0,
) : Iterable<Double> {

    private var _x: Double = x
    private var _y: Double = y
    private var _z: Double = z
    private var _w: Double = w

    private var onChangeCallback: () -> Unit = {}

    /** The x value of this quaternion. Assigning fires the [onChange] callback. */
    public var x: Double
        get() = _x
        set(value) {
            _x = value
            onChangeCallback()
        }

    /** The y value of this quaternion. Assigning fires the [onChange] callback. */
    public var y: Double
        get() = _y
        set(value) {
            _y = value
            onChangeCallback()
        }

    /** The z value of this quaternion. Assigning fires the [onChange] callback. */
    public var z: Double
        get() = _z
        set(value) {
            _z = value
            onChangeCallback()
        }

    /** The w value of this quaternion. Assigning fires the [onChange] callback. */
    public var w: Double
        get() = _w
        set(value) {
            _w = value
            onChangeCallback()
        }

    /**
     * Sets the quaternion components.
     *
     * @param x The x value of this quaternion.
     * @param y The y value of this quaternion.
     * @param z The z value of this quaternion.
     * @param w The w value of this quaternion.
     * @return A reference to this quaternion.
     */
    public fun set(x: Double, y: Double, z: Double, w: Double): Quaternion {
        _x = x
        _y = y
        _z = z
        _w = w

        onChangeCallback()

        return this
    }

    /**
     * Returns a new quaternion with copied values from this instance.
     *
     * @return A clone of this instance.
     */
    public fun clone(): Quaternion = Quaternion(_x, _y, _z, _w)

    /**
     * Copies the values of [quaternion] into this instance.
     *
     * @return A reference to this quaternion.
     */
    public fun copy(quaternion: Quaternion): Quaternion {
        _x = quaternion.x
        _y = quaternion.y
        _z = quaternion.z
        _w = quaternion.w

        onChangeCallback()

        return this
    }

    /**
     * Sets this quaternion from the rotation specified by the given Euler angles.
     *
     * @param euler The Euler angles.
     * @param update Whether the [onChange] callback should be executed.
     * @return A reference to this quaternion.
     */
    public fun setFromEuler(euler: Euler, update: Boolean = true): Quaternion {
        val x = euler.x
        val y = euler.y
        val z = euler.z
        val order = euler.order

        // http://www.mathworks.com/matlabcentral/fileexchange/
        // 	20696-function-to-convert-between-dcm-euler-angles-quaternions-and-euler-vectors/
        //	content/SpinCalc.m

        val c1 = cos(x / 2)
        val c2 = cos(y / 2)
        val c3 = cos(z / 2)

        val s1 = sin(x / 2)
        val s2 = sin(y / 2)
        val s3 = sin(z / 2)

        // Upstream's `default` branch only console.warns on an unknown string order;
        // an exhaustive enum `when` makes it unreachable, so it is dropped.
        when (order) {
            EulerOrder.XYZ -> {
                _x = s1 * c2 * c3 + c1 * s2 * s3
                _y = c1 * s2 * c3 - s1 * c2 * s3
                _z = c1 * c2 * s3 + s1 * s2 * c3
                _w = c1 * c2 * c3 - s1 * s2 * s3
            }

            EulerOrder.YXZ -> {
                _x = s1 * c2 * c3 + c1 * s2 * s3
                _y = c1 * s2 * c3 - s1 * c2 * s3
                _z = c1 * c2 * s3 - s1 * s2 * c3
                _w = c1 * c2 * c3 + s1 * s2 * s3
            }

            EulerOrder.ZXY -> {
                _x = s1 * c2 * c3 - c1 * s2 * s3
                _y = c1 * s2 * c3 + s1 * c2 * s3
                _z = c1 * c2 * s3 + s1 * s2 * c3
                _w = c1 * c2 * c3 - s1 * s2 * s3
            }

            EulerOrder.ZYX -> {
                _x = s1 * c2 * c3 - c1 * s2 * s3
                _y = c1 * s2 * c3 + s1 * c2 * s3
                _z = c1 * c2 * s3 - s1 * s2 * c3
                _w = c1 * c2 * c3 + s1 * s2 * s3
            }

            EulerOrder.YZX -> {
                _x = s1 * c2 * c3 + c1 * s2 * s3
                _y = c1 * s2 * c3 + s1 * c2 * s3
                _z = c1 * c2 * s3 - s1 * s2 * c3
                _w = c1 * c2 * c3 - s1 * s2 * s3
            }

            EulerOrder.XZY -> {
                _x = s1 * c2 * c3 - c1 * s2 * s3
                _y = c1 * s2 * c3 - s1 * c2 * s3
                _z = c1 * c2 * s3 + s1 * s2 * c3
                _w = c1 * c2 * c3 + s1 * s2 * s3
            }
        }

        if (update) onChangeCallback()

        return this
    }

    /**
     * Sets this quaternion from the given [axis] and [angle].
     *
     * @param axis The normalized axis.
     * @param angle The angle in radians.
     * @return A reference to this quaternion.
     */
    public fun setFromAxisAngle(axis: Vector3, angle: Double): Quaternion {
        // http://www.euclideanspace.com/maths/geometry/rotations/conversions/angleToQuaternion/index.htm

        val halfAngle = angle / 2
        val s = sin(halfAngle)

        _x = axis.x * s
        _y = axis.y * s
        _z = axis.z * s
        _w = cos(halfAngle)

        onChangeCallback()

        return this
    }

    /**
     * Sets this quaternion from the given rotation matrix [m] (the upper 3x3 of
     * which must be an unscaled rotation).
     *
     * @param m A 4x4 matrix whose upper 3x3 is a pure rotation matrix.
     * @return A reference to this quaternion.
     */
    public fun setFromRotationMatrix(m: Matrix4): Quaternion {
        // http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/index.htm

        // assumes the upper 3x3 of m is a pure rotation matrix (i.e, unscaled)

        val te = m.elements

        val m11 = te[0]; val m12 = te[4]; val m13 = te[8]
        val m21 = te[1]; val m22 = te[5]; val m23 = te[9]
        val m31 = te[2]; val m32 = te[6]; val m33 = te[10]

        val trace = m11 + m22 + m33

        if (trace > 0) {
            val s = 0.5 / sqrt(trace + 1.0)

            _w = 0.25 / s
            _x = (m32 - m23) * s
            _y = (m13 - m31) * s
            _z = (m21 - m12) * s
        } else if (m11 > m22 && m11 > m33) {
            val s = 2.0 * sqrt(1.0 + m11 - m22 - m33)

            _w = (m32 - m23) / s
            _x = 0.25 * s
            _y = (m12 + m21) / s
            _z = (m13 + m31) / s
        } else if (m22 > m33) {
            val s = 2.0 * sqrt(1.0 + m22 - m11 - m33)

            _w = (m13 - m31) / s
            _x = (m12 + m21) / s
            _y = 0.25 * s
            _z = (m23 + m32) / s
        } else {
            val s = 2.0 * sqrt(1.0 + m33 - m11 - m22)

            _w = (m21 - m12) / s
            _x = (m13 + m31) / s
            _y = (m23 + m32) / s
            _z = 0.25 * s
        }

        onChangeCallback()

        return this
    }

    /**
     * Sets this quaternion to the rotation required to rotate the direction vector
     * [vFrom] to the direction vector [vTo]. Both are assumed normalized.
     *
     * @param vFrom The first (normalized) direction vector.
     * @param vTo The second (normalized) direction vector.
     * @return A reference to this quaternion.
     */
    public fun setFromUnitVectors(vFrom: Vector3, vTo: Vector3): Quaternion {
        // assumes direction vectors vFrom and vTo are normalized

        var r = vFrom.dot(vTo) + 1

        if (r < 1e-8) { // the epsilon value has been discussed in #31286
            // vFrom and vTo point in opposite directions

            r = 0.0

            if (abs(vFrom.x) > abs(vFrom.z)) {
                _x = -vFrom.y
                _y = vFrom.x
                _z = 0.0
                _w = r
            } else {
                _x = 0.0
                _y = -vFrom.z
                _z = vFrom.y
                _w = r
            }
        } else {
            // crossVectors( vFrom, vTo ); // inlined to avoid cyclic dependency on Vector3

            _x = vFrom.y * vTo.z - vFrom.z * vTo.y
            _y = vFrom.z * vTo.x - vFrom.x * vTo.z
            _z = vFrom.x * vTo.y - vFrom.y * vTo.x
            _w = r
        }

        return normalize()
    }

    /**
     * Returns the angle between this quaternion and [q] in radians.
     *
     * @return The angle in radians.
     */
    public fun angleTo(q: Quaternion): Double =
        2 * acos(abs(MathUtils.clamp(dot(q), -1.0, 1.0)))

    /**
     * Rotates this quaternion by an angular [step] (in radians) toward [q],
     * without overshooting it.
     *
     * @return A reference to this quaternion.
     */
    public fun rotateTowards(q: Quaternion, step: Double): Quaternion {
        val angle = angleTo(q)

        if (angle == 0.0) return this

        val t = minOf(1.0, step / angle)

        slerp(q, t)

        return this
    }

    /**
     * Sets this quaternion to the identity quaternion (no rotation).
     *
     * @return A reference to this quaternion.
     */
    public fun identity(): Quaternion = set(0.0, 0.0, 0.0, 1.0)

    /**
     * Inverts this quaternion via [conjugate]. The quaternion is assumed to have
     * unit length.
     *
     * @return A reference to this quaternion.
     */
    public fun invert(): Quaternion = conjugate()

    /**
     * Returns the rotational conjugate of this quaternion — the same rotation in
     * the opposite direction about the rotational axis.
     *
     * @return A reference to this quaternion.
     */
    public fun conjugate(): Quaternion {
        _x *= -1
        _y *= -1
        _z *= -1

        onChangeCallback()

        return this
    }

    /**
     * Returns the dot product of this quaternion and [v].
     *
     * @return The result of the dot product.
     */
    public fun dot(v: Quaternion): Double =
        _x * v._x + _y * v._y + _z * v._z + _w * v._w

    /**
     * Returns the squared Euclidean length of this quaternion (as a 4D vector).
     *
     * @return The squared Euclidean length.
     */
    public fun lengthSq(): Double =
        _x * _x + _y * _y + _z * _z + _w * _w

    /**
     * Returns the Euclidean length of this quaternion (as a 4D vector).
     *
     * @return The Euclidean length.
     */
    public fun length(): Double =
        sqrt(_x * _x + _y * _y + _z * _z + _w * _w)

    /**
     * Normalizes this quaternion — computes the quaternion that performs the same
     * rotation but has length `1`. A zero-length quaternion becomes the identity.
     *
     * @return A reference to this quaternion.
     */
    public fun normalize(): Quaternion {
        var l = length()

        if (l == 0.0) {
            _x = 0.0
            _y = 0.0
            _z = 0.0
            _w = 1.0
        } else {
            l = 1 / l

            _x = _x * l
            _y = _y * l
            _z = _z * l
            _w = _w * l
        }

        onChangeCallback()

        return this
    }

    /**
     * Multiplies this quaternion by [q].
     *
     * @return A reference to this quaternion.
     */
    public fun multiply(q: Quaternion): Quaternion = multiplyQuaternions(this, q)

    /**
     * Pre-multiplies this quaternion by [q].
     *
     * @return A reference to this quaternion.
     */
    public fun premultiply(q: Quaternion): Quaternion = multiplyQuaternions(q, this)

    /**
     * Multiplies [a] and [b] and stores the result in this instance.
     *
     * @return A reference to this quaternion.
     */
    public fun multiplyQuaternions(a: Quaternion, b: Quaternion): Quaternion {
        // from http://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/code/index.htm

        val qax = a._x; val qay = a._y; val qaz = a._z; val qaw = a._w
        val qbx = b._x; val qby = b._y; val qbz = b._z; val qbw = b._w

        _x = qax * qbw + qaw * qbx + qay * qbz - qaz * qby
        _y = qay * qbw + qaw * qby + qaz * qbx - qax * qbz
        _z = qaz * qbw + qaw * qbz + qax * qby - qay * qbx
        _w = qaw * qbw - qax * qbx - qay * qby - qaz * qbz

        onChangeCallback()

        return this
    }

    /**
     * Performs a spherical linear interpolation between this quaternion and [qb]
     * by factor [t]. Values in `[0, 1]` interpolate; values outside extrapolate.
     *
     * @return A reference to this quaternion.
     */
    public fun slerp(qb: Quaternion, t: Double): Quaternion {
        var t = t
        var x = qb._x; var y = qb._y; var z = qb._z; var w = qb._w

        var dot = dot(qb)

        if (dot < 0) {
            x = -x
            y = -y
            z = -z
            w = -w

            dot = -dot
        }

        var s = 1 - t

        if (dot < 0.9995) {
            // slerp

            val theta = acos(dot)
            // Upstream names this local `sin`; renamed to avoid shadowing the
            // imported `sin` function used on the very next lines.
            val sinTheta = sin(theta)

            s = sin(s * theta) / sinTheta
            t = sin(t * theta) / sinTheta

            _x = _x * s + x * t
            _y = _y * s + y * t
            _z = _z * s + z * t
            _w = _w * s + w * t

            onChangeCallback()
        } else {
            // for small angles, lerp then normalize

            _x = _x * s + x * t
            _y = _y * s + y * t
            _z = _z * s + z * t
            _w = _w * s + w * t

            normalize() // normalize calls onChangeCallback()
        }

        return this
    }

    /**
     * Performs a spherical linear interpolation between [qa] and [qb] and stores
     * the result in this quaternion.
     *
     * @param t The interpolation factor in the closed interval `[0, 1]`.
     * @return A reference to this quaternion.
     */
    public fun slerpQuaternions(qa: Quaternion, qb: Quaternion, t: Double): Quaternion =
        copy(qa).slerp(qb, t)

    /**
     * Sets this quaternion to a uniformly random, normalized quaternion.
     *
     * @return A reference to this quaternion.
     */
    public fun random(): Quaternion {
        // Ken Shoemake
        // Uniform random rotations
        // D. Kirk, editor, Graphics Gems III, pages 124-132. Academic Press, New York, 1992.

        val theta1 = 2 * PI * kotlin.random.Random.nextDouble()
        val theta2 = 2 * PI * kotlin.random.Random.nextDouble()

        val x0 = kotlin.random.Random.nextDouble()
        val r1 = sqrt(1 - x0)
        val r2 = sqrt(x0)

        return set(
            r1 * sin(theta1),
            r1 * cos(theta1),
            r2 * sin(theta2),
            r2 * cos(theta2),
        )
    }

    // Upstream's public `equals(quaternion)` is dropped per dialect rule 11 (a bare
    // same-type overload desyncs `==` from `.equals`); use `==` / the `equals`
    // override below, which has identical component-wise semantics.

    /**
     * Sets this quaternion's components from [array] starting at [offset].
     *
     * @return A reference to this quaternion.
     */
    public fun fromArray(array: DoubleArray, offset: Int = 0): Quaternion {
        _x = array[offset]
        _y = array[offset + 1]
        _z = array[offset + 2]
        _w = array[offset + 3]

        onChangeCallback()

        return this
    }

    /**
     * Writes the components of this quaternion into [array] at [offset], growing
     * the list as needed, and returns it.
     *
     * @return The quaternion components.
     */
    public fun toArray(array: MutableList<Double> = mutableListOf(), offset: Int = 0): MutableList<Double> {
        while (array.size < offset + 4) array.add(0.0)
        array[offset] = _x
        array[offset + 1] = _y
        array[offset + 2] = _z
        array[offset + 3] = _w

        return array
    }

    /**
     * Sets the components of this quaternion from [attribute] at [index].
     *
     * @return A reference to this quaternion.
     */
    public fun fromBufferAttribute(attribute: AttributeLike, index: Int): Quaternion {
        _x = attribute.getX(index)
        _y = attribute.getY(index)
        _z = attribute.getZ(index)
        _w = attribute.getW(index)

        onChangeCallback()

        return this
    }

    /**
     * Serialization result: the components of this quaternion as `[x, y, z, w]`.
     *
     * @return The serialized quaternion.
     */
    public fun toJSON(): MutableList<Double> = toArray()

    /**
     * Registers [callback] to run whenever this quaternion's state changes (via a
     * component setter or a mutating method). Reads never fire it.
     *
     * @return A reference to this quaternion.
     */
    public fun onChange(callback: () -> Unit): Quaternion {
        onChangeCallback = callback
        return this
    }

    override fun iterator(): Iterator<Double> = listOf(_x, _y, _z, _w).iterator()

    /**
     * Structural equality: `true` when [other] is a [Quaternion] with equal
     * components (component-wise `==`, so `NaN != NaN`).
     */
    override fun equals(other: Any?): Boolean =
        other is Quaternion && other._x == _x && other._y == _y && other._z == _z && other._w == _w

    override fun hashCode(): Int {
        // (v + 0.0) collapses -0.0 to +0.0, keeping hashCode consistent with
        // equals treating -0.0 == +0.0.
        var result = (_x + 0.0).hashCode()
        result = 31 * result + (_y + 0.0).hashCode()
        result = 31 * result + (_z + 0.0).hashCode()
        result = 31 * result + (_w + 0.0).hashCode()
        return result
    }

    override fun toString(): String = "Quaternion(x=${_x}, y=${_y}, z=${_z}, w=${_w})"

    public companion object {

        /**
         * Interpolates between two quaternions via SLERP, assuming the quaternion
         * data live in flat arrays.
         *
         * @param dst The destination array.
         * @param dstOffset An offset into the destination array.
         * @param src0 The source array of the first quaternion.
         * @param srcOffset0 An offset into the first source array.
         * @param src1 The source array of the second quaternion.
         * @param srcOffset1 An offset into the second source array.
         * @param t The interpolation factor. `[0, 1]` interpolates; outside extrapolates.
         */
        public fun slerpFlat(
            dst: DoubleArray,
            dstOffset: Int,
            src0: DoubleArray,
            srcOffset0: Int,
            src1: DoubleArray,
            srcOffset1: Int,
            t: Double,
        ) {
            var t = t
            var x0 = src0[srcOffset0 + 0]
            var y0 = src0[srcOffset0 + 1]
            var z0 = src0[srcOffset0 + 2]
            var w0 = src0[srcOffset0 + 3]

            var x1 = src1[srcOffset1 + 0]
            var y1 = src1[srcOffset1 + 1]
            var z1 = src1[srcOffset1 + 2]
            var w1 = src1[srcOffset1 + 3]

            if (w0 != w1 || x0 != x1 || y0 != y1 || z0 != z1) {
                var dot = x0 * x1 + y0 * y1 + z0 * z1 + w0 * w1

                if (dot < 0) {
                    x1 = -x1
                    y1 = -y1
                    z1 = -z1
                    w1 = -w1

                    dot = -dot
                }

                var s = 1 - t

                if (dot < 0.9995) {
                    // slerp

                    val theta = acos(dot)
                    // Upstream names this local `sin`; renamed to avoid shadowing the
                    // imported `sin` function used on the very next lines.
                    val sinTheta = sin(theta)

                    s = sin(s * theta) / sinTheta
                    t = sin(t * theta) / sinTheta

                    x0 = x0 * s + x1 * t
                    y0 = y0 * s + y1 * t
                    z0 = z0 * s + z1 * t
                    w0 = w0 * s + w1 * t
                } else {
                    // for small angles, lerp then normalize

                    x0 = x0 * s + x1 * t
                    y0 = y0 * s + y1 * t
                    z0 = z0 * s + z1 * t
                    w0 = w0 * s + w1 * t

                    val f = 1 / sqrt(x0 * x0 + y0 * y0 + z0 * z0 + w0 * w0)

                    x0 *= f
                    y0 *= f
                    z0 *= f
                    w0 *= f
                }
            }

            dst[dstOffset] = x0
            dst[dstOffset + 1] = y0
            dst[dstOffset + 2] = z0
            dst[dstOffset + 3] = w0
        }

        /**
         * Multiplies two quaternions, assuming the quaternion data live in flat
         * arrays.
         *
         * @param dst The destination array.
         * @param dstOffset An offset into the destination array.
         * @param src0 The source array of the first quaternion.
         * @param srcOffset0 An offset into the first source array.
         * @param src1 The source array of the second quaternion.
         * @param srcOffset1 An offset into the second source array.
         * @return The destination array.
         */
        public fun multiplyQuaternionsFlat(
            dst: DoubleArray,
            dstOffset: Int,
            src0: DoubleArray,
            srcOffset0: Int,
            src1: DoubleArray,
            srcOffset1: Int,
        ): DoubleArray {
            val x0 = src0[srcOffset0]
            val y0 = src0[srcOffset0 + 1]
            val z0 = src0[srcOffset0 + 2]
            val w0 = src0[srcOffset0 + 3]

            val x1 = src1[srcOffset1]
            val y1 = src1[srcOffset1 + 1]
            val z1 = src1[srcOffset1 + 2]
            val w1 = src1[srcOffset1 + 3]

            dst[dstOffset] = x0 * w1 + w0 * x1 + y0 * z1 - z0 * y1
            dst[dstOffset + 1] = y0 * w1 + w0 * y1 + z0 * x1 - x0 * z1
            dst[dstOffset + 2] = z0 * w1 + w0 * z1 + x0 * y1 - y0 * x1
            dst[dstOffset + 3] = w0 * w1 - x0 * x1 - y0 * y1 - z0 * z1

            return dst
        }
    }
}
