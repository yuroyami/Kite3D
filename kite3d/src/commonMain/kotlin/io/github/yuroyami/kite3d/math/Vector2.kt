/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Vector2.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.truncate

/**
 * A 2D vector: an ordered pair of numbers (`x`, `y`).
 *
 * Vectors are **mutable** and **not thread-safe**; confine an instance (and any
 * object graph holding it) to a single thread, exactly as in three.js. Most
 * methods mutate `this` and return it for chaining.
 *
 * Iterating a vector yields its components `(x, y)` in order.
 */
public class Vector2(
    /** The x value of this vector. */
    public var x: Double = 0.0,
    /** The y value of this vector. */
    public var y: Double = 0.0,
) : Iterable<Double> {

    /** Alias for [x]. */
    public var width: Double
        get() = x
        set(value) { x = value }

    /** Alias for [y]. */
    public var height: Double
        get() = y
        set(value) { y = value }

    /**
     * Sets the vector components.
     *
     * @param x The value of the x component.
     * @param y The value of the y component.
     * @return A reference to this vector.
     */
    public fun set(x: Double, y: Double): Vector2 {
        this.x = x
        this.y = y
        return this
    }

    /**
     * Sets all vector components to [scalar].
     *
     * @return A reference to this vector.
     */
    public fun setScalar(scalar: Double): Vector2 {
        x = scalar
        y = scalar
        return this
    }

    /**
     * Sets the vector's x component.
     *
     * @param x The value to set.
     * @return A reference to this vector.
     */
    public fun setX(x: Double): Vector2 {
        this.x = x
        return this
    }

    /**
     * Sets the vector's y component.
     *
     * @param y The value to set.
     * @return A reference to this vector.
     */
    public fun setY(y: Double): Vector2 {
        this.y = y
        return this
    }

    /**
     * Sets the component at [index] (`0` = x, `1` = y) to [value].
     *
     * @throws IllegalArgumentException if [index] is out of range.
     * @return A reference to this vector.
     */
    public fun setComponent(index: Int, value: Double): Vector2 {
        when (index) {
            0 -> x = value
            1 -> y = value
            else -> throw IllegalArgumentException("index is out of range: $index")
        }
        return this
    }

    /**
     * Returns the component at [index] (`0` = x, `1` = y).
     *
     * @throws IllegalArgumentException if [index] is out of range.
     *
     * @return A vector component value.
     */
    public fun getComponent(index: Int): Double = when (index) {
        0 -> x
        1 -> y
        else -> throw IllegalArgumentException("index is out of range: $index")
    }

    /**
     * Returns a new vector with the same components.
     *
     * @return A clone of this instance.
     */
    public fun clone(): Vector2 = Vector2(x, y)

    /**
     * Copies the components of [v] into this instance.
     *
     * @return A reference to this vector.
     */
    public fun copy(v: Vector2): Vector2 {
        x = v.x
        y = v.y
        return this
    }

    /**
     * Adds [v] to this instance.
     *
     * @return A reference to this vector.
     */
    public fun add(v: Vector2): Vector2 {
        x += v.x
        y += v.y
        return this
    }

    /**
     * Adds the scalar [s] to all components.
     *
     * @return A reference to this vector.
     */
    public fun addScalar(s: Double): Vector2 {
        x += s
        y += s
        return this
    }

    /**
     * Sets this vector to `a + b`.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return A reference to this vector.
     */
    public fun addVectors(a: Vector2, b: Vector2): Vector2 {
        x = a.x + b.x
        y = a.y + b.y
        return this
    }

    /**
     * Adds [v] scaled by [s] to this instance.
     *
     * @return A reference to this vector.
     */
    public fun addScaledVector(v: Vector2, s: Double): Vector2 {
        x += v.x * s
        y += v.y * s
        return this
    }

    /**
     * Subtracts [v] from this instance.
     *
     * @return A reference to this vector.
     */
    public fun sub(v: Vector2): Vector2 {
        x -= v.x
        y -= v.y
        return this
    }

    /**
     * Subtracts the scalar [s] from all components.
     *
     * @return A reference to this vector.
     */
    public fun subScalar(s: Double): Vector2 {
        x -= s
        y -= s
        return this
    }

    /**
     * Sets this vector to `a - b`.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return A reference to this vector.
     */
    public fun subVectors(a: Vector2, b: Vector2): Vector2 {
        x = a.x - b.x
        y = a.y - b.y
        return this
    }

    /**
     * Multiplies this instance component-wise by [v].
     *
     * @return A reference to this vector.
     */
    public fun multiply(v: Vector2): Vector2 {
        x *= v.x
        y *= v.y
        return this
    }

    /**
     * Multiplies all components by [scalar].
     *
     * @return A reference to this vector.
     */
    public fun multiplyScalar(scalar: Double): Vector2 {
        x *= scalar
        y *= scalar
        return this
    }

    /**
     * Divides this instance component-wise by [v].
     *
     * @return A reference to this vector.
     */
    public fun divide(v: Vector2): Vector2 {
        x /= v.x
        y /= v.y
        return this
    }

    /**
     * Divides all components by [scalar].
     *
     * @return A reference to this vector.
     */
    public fun divideScalar(scalar: Double): Vector2 = multiplyScalar(1.0 / scalar)

    /**
     * Multiplies this vector by the 3x3 matrix [m].
     *
     * @return A reference to this vector.
     */
    public fun applyMatrix3(m: Matrix3): Vector2 {
        val x = this.x
        val y = this.y
        val e = m.elements

        this.x = e[0] * x + e[3] * y + e[6]
        this.y = e[1] * x + e[4] * y + e[7]

        return this
    }

    /**
     * Sets each component to the minimum of itself and [v]'s corresponding component.
     *
     * @return A reference to this vector.
     */
    public fun min(v: Vector2): Vector2 {
        x = minOf(x, v.x)
        y = minOf(y, v.y)
        return this
    }

    /**
     * Sets each component to the maximum of itself and [v]'s corresponding component.
     *
     * @return A reference to this vector.
     */
    public fun max(v: Vector2): Vector2 {
        x = maxOf(x, v.x)
        y = maxOf(y, v.y)
        return this
    }

    /**
     * Clamps each component into the range `[min, max]` (assumed component-wise
     * `min <= max`).
     *
     * @param min The minimum x and y values.
     * @param max The maximum x and y values in the desired range.
     * @return A reference to this vector.
     */
    public fun clamp(min: Vector2, max: Vector2): Vector2 {
        x = MathUtils.clamp(x, min.x, max.x)
        y = MathUtils.clamp(y, min.y, max.y)
        return this
    }

    /**
     * Clamps each component into the scalar range `[minVal, maxVal]`.
     *
     * @param minVal The minimum value the components will be clamped to.
     * @param maxVal The maximum value the components will be clamped to.
     * @return A reference to this vector.
     */
    public fun clampScalar(minVal: Double, maxVal: Double): Vector2 {
        x = MathUtils.clamp(x, minVal, maxVal)
        y = MathUtils.clamp(y, minVal, maxVal)
        return this
    }

    /**
     * Clamps this vector's length into `[min, max]`.
     *
     * @param min The minimum value the vector length will be clamped to.
     * @param max The maximum value the vector length will be clamped to.
     * @return A reference to this vector.
     */
    public fun clampLength(min: Double, max: Double): Vector2 {
        val length = length()
        return divideScalar(length.orOne()).multiplyScalar(MathUtils.clamp(length, min, max))
    }

    /**
     * Rounds each component down to the nearest integer.
     *
     * @return A reference to this vector.
     */
    public fun floor(): Vector2 {
        x = floor(x)
        y = floor(y)
        return this
    }

    /**
     * Rounds each component up to the nearest integer.
     *
     * @return A reference to this vector.
     */
    public fun ceil(): Vector2 {
        x = ceil(x)
        y = ceil(y)
        return this
    }

    /**
     * Rounds each component to the nearest integer (halves toward +infinity, as
     * JS `Math.round`).
     *
     * @return A reference to this vector.
     */
    public fun round(): Vector2 {
        x = MathUtils.jsRound(x)
        y = MathUtils.jsRound(y)
        return this
    }

    /**
     * Rounds each component toward zero.
     *
     * @return A reference to this vector.
     */
    public fun roundToZero(): Vector2 {
        x = truncate(x)
        y = truncate(y)
        return this
    }

    /**
     * Negates each component.
     *
     * @return A reference to this vector.
     */
    public fun negate(): Vector2 {
        x = -x
        y = -y
        return this
    }

    /**
     * Returns the dot product of this vector and [v].
     *
     * @return The result of the dot product.
     */
    public fun dot(v: Vector2): Double = x * v.x + y * v.y

    /**
     * Returns the (scalar) cross product of this vector and [v].
     *
     * @return The result of the cross product.
     */
    public fun cross(v: Vector2): Double = x * v.y - y * v.x

    /**
     * Returns the squared Euclidean length of this vector.
     *
     * @return The square length of this vector.
     */
    public fun lengthSq(): Double = x * x + y * y

    /**
     * Returns the Euclidean length of this vector.
     *
     * @return The length of this vector.
     */
    public fun length(): Double = sqrt(x * x + y * y)

    /**
     * Returns the Manhattan length of this vector.
     *
     * @return The length of this vector.
     */
    public fun manhattanLength(): Double = abs(x) + abs(y)

    /**
     * Normalizes this vector to unit length (leaves a zero vector unchanged).
     *
     * @return A reference to this vector.
     */
    public fun normalize(): Vector2 = divideScalar(length().orOne())

    /**
     * Returns the angle of this vector relative to the positive x-axis, in radians,
     * in `[0, 2*PI)`.
     *
     * @return The angle in radians.
     */
    public fun angle(): Double = atan2(-y, -x) + PI

    /**
     * Returns the angle between this vector and [v], in radians.
     *
     * @return The angle in radians.
     */
    public fun angleTo(v: Vector2): Double {
        val denominator = sqrt(lengthSq() * v.lengthSq())
        if (denominator == 0.0) return PI / 2
        val theta = dot(v) / denominator
        // clamp, to handle numerical problems
        return acos(MathUtils.clamp(theta, -1.0, 1.0))
    }

    /**
     * Returns the distance from this vector to [v].
     *
     * @return The distance.
     */
    public fun distanceTo(v: Vector2): Double = sqrt(distanceToSquared(v))

    /**
     * Returns the squared distance from this vector to [v].
     *
     * @return The squared distance.
     */
    public fun distanceToSquared(v: Vector2): Double {
        val dx = x - v.x
        val dy = y - v.y
        return dx * dx + dy * dy
    }

    /**
     * Returns the Manhattan distance from this vector to [v].
     *
     * @return The Manhattan distance.
     */
    public fun manhattanDistanceTo(v: Vector2): Double = abs(x - v.x) + abs(y - v.y)

    /**
     * Sets this vector's length to [length], preserving direction.
     *
     * @return A reference to this vector.
     */
    public fun setLength(length: Double): Vector2 = normalize().multiplyScalar(length)

    /**
     * Linearly interpolates from this vector toward [v] by [alpha].
     *
     * @return A reference to this vector.
     */
    public fun lerp(v: Vector2, alpha: Double): Vector2 {
        x += (v.x - x) * alpha
        y += (v.y - y) * alpha
        return this
    }

    /**
     * Sets this vector to the linear interpolation of [v1] and [v2] by [alpha].
     *
     * @return A reference to this vector.
     */
    public fun lerpVectors(v1: Vector2, v2: Vector2, alpha: Double): Vector2 {
        x = v1.x + (v2.x - v1.x) * alpha
        y = v1.y + (v2.y - v1.y) * alpha
        return this
    }

    /**
     * Sets `x = array[offset]`, `y = array[offset + 1]`.
     *
     * @param array An array holding the vector component values.
     * @return A reference to this vector.
     */
    public fun fromArray(array: DoubleArray, offset: Int = 0): Vector2 {
        x = array[offset]
        y = array[offset + 1]
        return this
    }

    /**
     * Writes this vector's components into [array] at [offset], growing the list as
     * needed, and returns it.
     *
     * @return The vector components.
     */
    public fun toArray(array: MutableList<Double> = mutableListOf(), offset: Int = 0): MutableList<Double> {
        while (array.size < offset + 2) array.add(0.0)
        array[offset] = x
        array[offset + 1] = y
        return array
    }

    /**
     * Sets this vector's components from [attribute] at [index].
     *
     * @return A reference to this vector.
     */
    public fun fromBufferAttribute(attribute: AttributeLike, index: Int): Vector2 {
        x = attribute.getX(index)
        y = attribute.getY(index)
        return this
    }

    /**
     * Rotates this vector around [center] by [angle] radians.
     *
     * @return A reference to this vector.
     */
    public fun rotateAround(center: Vector2, angle: Double): Vector2 {
        val c = cos(angle)
        val s = sin(angle)
        val dx = x - center.x
        val dy = y - center.y
        x = dx * c - dy * s + center.x
        y = dx * s + dy * c + center.y
        return this
    }

    /**
     * Sets each component to a pseudo-random value in `[0, 1)`.
     *
     * @return A reference to this vector.
     */
    public fun random(): Vector2 {
        x = kotlin.random.Random.nextDouble()
        y = kotlin.random.Random.nextDouble()
        return this
    }

    override fun iterator(): Iterator<Double> = listOf(x, y).iterator()

    /**
     * Structural equality: `true` when [other] is a [Vector2] with equal components
     * (component-wise `==`, so `NaN != NaN`).
     */
    override fun equals(other: Any?): Boolean =
        other is Vector2 && other.x == x && other.y == y

    override fun hashCode(): Int {
        // (v + 0.0) collapses -0.0 to +0.0, keeping hashCode consistent with
        // equals treating -0.0 == +0.0.
        var result = (x + 0.0).hashCode()
        result = 31 * result + (y + 0.0).hashCode()
        return result
    }

    override fun toString(): String = "Vector2(x=$x, y=$y)"
}
