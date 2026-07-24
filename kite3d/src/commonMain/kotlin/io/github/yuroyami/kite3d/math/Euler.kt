/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Euler.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2

/**
 * The rotation order applied by an [Euler] instance.
 *
 * three.js uses string literals (`'XYZ'`, `'ZXY'`, …); this port uses this enum
 * everywhere an order is passed. Members are listed in the same order as the
 * cases in three.js's `switch` statements.
 */
public enum class EulerOrder { XYZ, YXZ, ZXY, XZY, YZX, ZYX }

/**
 * The default [Euler] angle order (`XYZ`). Also exposed as [Euler.DEFAULT_ORDER];
 * both refer to the same value.
 */
public val DEFAULT_ORDER: EulerOrder = EulerOrder.XYZ

/**
 * A class representing Euler angles.
 *
 * Euler angles describe a rotational transformation by rotating an object on its
 * various axes in specified amounts per axis, and a specified axis [order].
 *
 * Iterating through an instance yields its components `(x, y, z, order)` in that
 * order — three [Double]s followed by an [EulerOrder].
 *
 * Instances are **mutable** and **not thread-safe**; confine an instance (and any
 * object graph holding it) to a single thread, exactly as in three.js.
 *
 * Reading `x`/`y`/`z`/`order` has no side effect. Writing any of them — or calling
 * a mutating method — fires the registered [onChange] callback (see that method).
 */
public class Euler(
    x: Double = 0.0,
    y: Double = 0.0,
    z: Double = 0.0,
    order: EulerOrder = EulerOrder.XYZ,
) : Iterable<Any> {

    private var _x: Double = x
    private var _y: Double = y
    private var _z: Double = z
    private var _order: EulerOrder = order

    private var onChangeCallback: () -> Unit = {}

    /** The angle of the x axis in radians. Assigning fires the [onChange] callback. */
    public var x: Double
        get() = _x
        set(value) {
            _x = value
            onChangeCallback()
        }

    /** The angle of the y axis in radians. Assigning fires the [onChange] callback. */
    public var y: Double
        get() = _y
        set(value) {
            _y = value
            onChangeCallback()
        }

    /** The angle of the z axis in radians. Assigning fires the [onChange] callback. */
    public var z: Double
        get() = _z
        set(value) {
            _z = value
            onChangeCallback()
        }

    /** The order in which the axis rotations are applied. Assigning fires the [onChange] callback. */
    public var order: EulerOrder
        get() = _order
        set(value) {
            _order = value
            onChangeCallback()
        }

    /**
     * Sets the Euler components.
     *
     * @param x The angle of the x axis in radians.
     * @param y The angle of the y axis in radians.
     * @param z The angle of the z axis in radians.
     * @param order The order in which the rotations are applied.
     * @return A reference to this Euler instance.
     */
    public fun set(x: Double, y: Double, z: Double, order: EulerOrder = _order): Euler {
        _x = x
        _y = y
        _z = z
        _order = order

        onChangeCallback()

        return this
    }

    /**
     * Returns a new Euler instance with copied values from this instance.
     *
     * @return A clone of this instance.
     */
    public fun clone(): Euler = Euler(_x, _y, _z, _order)

    /**
     * Copies the values of [euler] into this instance.
     *
     * @return A reference to this Euler instance.
     */
    public fun copy(euler: Euler): Euler {
        _x = euler._x
        _y = euler._y
        _z = euler._z
        _order = euler._order

        onChangeCallback()

        return this
    }

    /**
     * Sets the angles of this Euler instance from a pure rotation matrix [m] (the
     * upper 3x3 of which must be an unscaled rotation).
     *
     * @param m A 4x4 matrix whose upper 3x3 is a pure rotation matrix.
     * @param order The order in which the axis rotations are applied.
     * @param update Whether the [onChange] callback should be executed.
     * @return A reference to this Euler instance.
     */
    public fun setFromRotationMatrix(m: Matrix4, order: EulerOrder = _order, update: Boolean = true): Euler {
        val te = m.elements
        val m11 = te[0]; val m12 = te[4]; val m13 = te[8]
        val m21 = te[1]; val m22 = te[5]; val m23 = te[9]
        val m31 = te[2]; val m32 = te[6]; val m33 = te[10]

        // Upstream's `default` branch only console.warns on an unknown string order;
        // an exhaustive enum `when` makes it unreachable, so it is dropped.
        when (order) {
            EulerOrder.XYZ -> {
                _y = asin(MathUtils.clamp(m13, -1.0, 1.0))

                if (abs(m13) < 0.9999999) {
                    _x = atan2(-m23, m33)
                    _z = atan2(-m12, m11)
                } else {
                    _x = atan2(m32, m22)
                    _z = 0.0
                }
            }

            EulerOrder.YXZ -> {
                _x = asin(-MathUtils.clamp(m23, -1.0, 1.0))

                if (abs(m23) < 0.9999999) {
                    _y = atan2(m13, m33)
                    _z = atan2(m21, m22)
                } else {
                    _y = atan2(-m31, m11)
                    _z = 0.0
                }
            }

            EulerOrder.ZXY -> {
                _x = asin(MathUtils.clamp(m32, -1.0, 1.0))

                if (abs(m32) < 0.9999999) {
                    _y = atan2(-m31, m33)
                    _z = atan2(-m12, m22)
                } else {
                    _y = 0.0
                    _z = atan2(m21, m11)
                }
            }

            EulerOrder.ZYX -> {
                _y = asin(-MathUtils.clamp(m31, -1.0, 1.0))

                if (abs(m31) < 0.9999999) {
                    _x = atan2(m32, m33)
                    _z = atan2(m21, m11)
                } else {
                    _x = 0.0
                    _z = atan2(-m12, m22)
                }
            }

            EulerOrder.YZX -> {
                _z = asin(MathUtils.clamp(m21, -1.0, 1.0))

                if (abs(m21) < 0.9999999) {
                    _x = atan2(-m23, m22)
                    _y = atan2(-m31, m11)
                } else {
                    _x = 0.0
                    _y = atan2(m13, m33)
                }
            }

            EulerOrder.XZY -> {
                _z = asin(-MathUtils.clamp(m12, -1.0, 1.0))

                if (abs(m12) < 0.9999999) {
                    _x = atan2(m32, m22)
                    _y = atan2(m13, m11)
                } else {
                    _x = atan2(-m23, m33)
                    _y = 0.0
                }
            }
        }

        _order = order

        if (update) onChangeCallback()

        return this
    }

    /**
     * Sets the angles of this Euler instance from a normalized quaternion [q].
     *
     * @param q A normalized Quaternion.
     * @param order The order in which the axis rotations are applied.
     * @param update Whether the [onChange] callback should be executed.
     * @return A reference to this Euler instance.
     */
    public fun setFromQuaternion(q: Quaternion, order: EulerOrder = _order, update: Boolean = true): Euler {
        // three.js uses a module-level _matrix scratch; a local avoids the data race.
        val matrix = Matrix4()
        matrix.makeRotationFromQuaternion(q)

        return setFromRotationMatrix(matrix, order, update)
    }

    /**
     * Sets the angles of this Euler instance from the given vector [v].
     *
     * @param v The vector.
     * @param order The order in which the axis rotations are applied.
     * @return A reference to this Euler instance.
     */
    public fun setFromVector3(v: Vector3, order: EulerOrder = _order): Euler =
        set(v.x, v.y, v.z, order)

    /**
     * Resets the euler angle with a new order by creating a quaternion from this
     * euler angle and then setting this euler angle with the quaternion and the
     * new order.
     *
     * Warning: This discards revolution information.
     *
     * @param newOrder The new order in which the axis rotations are applied.
     * @return A reference to this Euler instance.
     */
    public fun reorder(newOrder: EulerOrder): Euler {
        // three.js uses a module-level _quaternion scratch; a local avoids the data race.
        val quaternion = Quaternion()
        quaternion.setFromEuler(this)

        return setFromQuaternion(quaternion, newOrder)
    }

    // Upstream's public `equals(euler)` is dropped per dialect rule 11 (a bare
    // same-type overload desyncs `==` from `.equals`); use `==` / the `equals`
    // override below, which has identical component-wise semantics.

    /**
     * Sets this Euler instance's components from [array]. The first three entries
     * are assigned to the x, y and z components; an optional fourth entry (an
     * [EulerOrder]) defines the order.
     *
     * @return A reference to this Euler instance.
     */
    public fun fromArray(array: List<Any>): Euler {
        _x = array[0] as Double
        _y = array[1] as Double
        _z = array[2] as Double
        if (array.size > 3) _order = array[3] as EulerOrder

        onChangeCallback()

        return this
    }

    /**
     * Writes the components of this Euler instance into [array] at [offset],
     * growing the list as needed, and returns it. The order is written as the
     * fourth element (an [EulerOrder]).
     *
     * @return The Euler components.
     */
    public fun toArray(array: MutableList<Any> = mutableListOf(), offset: Int = 0): MutableList<Any> {
        while (array.size < offset + 4) array.add(0.0)
        array[offset] = _x
        array[offset + 1] = _y
        array[offset + 2] = _z
        array[offset + 3] = _order

        return array
    }

    /**
     * Registers [callback] to run whenever this Euler's state changes (via a
     * component setter or a mutating method). Reads never fire it.
     *
     * @return A reference to this Euler instance.
     */
    public fun onChange(callback: () -> Unit): Euler {
        onChangeCallback = callback
        return this
    }

    override fun iterator(): Iterator<Any> = listOf(_x, _y, _z, _order).iterator()

    /**
     * Structural equality: `true` when [other] is an [Euler] with equal components
     * and order (components compared with primitive `==`, so `NaN != NaN`).
     */
    override fun equals(other: Any?): Boolean =
        other is Euler && other._x == _x && other._y == _y && other._z == _z && other._order == _order

    override fun hashCode(): Int {
        // (v + 0.0) collapses -0.0 to +0.0, keeping hashCode consistent with
        // equals treating -0.0 == +0.0.
        var result = (_x + 0.0).hashCode()
        result = 31 * result + (_y + 0.0).hashCode()
        result = 31 * result + (_z + 0.0).hashCode()
        result = 31 * result + _order.hashCode()
        return result
    }

    override fun toString(): String = "Euler(x=${_x}, y=${_y}, z=${_z}, order=${_order})"

    public companion object {
        /** The default Euler angle order (`XYZ`). Same value as the top-level [DEFAULT_ORDER]. */
        public val DEFAULT_ORDER: EulerOrder get() = io.github.yuroyami.kite3d.math.DEFAULT_ORDER
    }
}
