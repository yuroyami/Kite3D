/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Vector3.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.truncate

/**
 * A 3D vector: an ordered triplet of numbers (`x`, `y`, `z`).
 *
 * A 3D vector can represent a point in 3D space, a direction and length in 3D
 * space (the length being the Euclidean distance from `(0, 0, 0)` to
 * `(x, y, z)`), or any arbitrary ordered triplet of numbers.
 *
 * Vectors are **mutable** and **not thread-safe**; confine an instance (and any
 * object graph holding it) to a single thread, exactly as in three.js. Most
 * methods mutate `this` and return it for chaining.
 *
 * Iterating a vector yields its components `(x, y, z)` in order.
 */
public class Vector3(
    /** The x value of this vector. */
    public var x: Double = 0.0,
    /** The y value of this vector. */
    public var y: Double = 0.0,
    /** The z value of this vector. */
    public var z: Double = 0.0,
) : Iterable<Double> {

    /**
     * Sets the vector components.
     *
     * (Upstream also has a `z === undefined` guard for `sprite.scale.set(x, y)`;
     * in Kotlin `set` takes three required [Double]s, so that guard is dropped.)
     *
     * @param x The value of the x component.
     * @param y The value of the y component.
     * @param z The value of the z component.
     * @return A reference to this vector.
     */
    public fun set(x: Double, y: Double, z: Double): Vector3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    /**
     * Sets all vector components to [scalar].
     *
     * @return A reference to this vector.
     */
    public fun setScalar(scalar: Double): Vector3 {
        x = scalar
        y = scalar
        z = scalar
        return this
    }

    /**
     * Sets the vector's x component.
     *
     * @param x The value to set.
     * @return A reference to this vector.
     */
    public fun setX(x: Double): Vector3 {
        this.x = x
        return this
    }

    /**
     * Sets the vector's y component.
     *
     * @param y The value to set.
     * @return A reference to this vector.
     */
    public fun setY(y: Double): Vector3 {
        this.y = y
        return this
    }

    /**
     * Sets the vector's z component.
     *
     * @param z The value to set.
     * @return A reference to this vector.
     */
    public fun setZ(z: Double): Vector3 {
        this.z = z
        return this
    }

    /**
     * Sets the component at [index] (`0` = x, `1` = y, `2` = z) to [value].
     *
     * @throws IllegalArgumentException if [index] is out of range.
     * @return A reference to this vector.
     */
    public fun setComponent(index: Int, value: Double): Vector3 {
        when (index) {
            0 -> x = value
            1 -> y = value
            2 -> z = value
            else -> throw IllegalArgumentException("index is out of range: $index")
        }
        return this
    }

    /**
     * Returns the component at [index] (`0` = x, `1` = y, `2` = z).
     *
     * @throws IllegalArgumentException if [index] is out of range.
     *
     * @return A vector component value.
     */
    public fun getComponent(index: Int): Double = when (index) {
        0 -> x
        1 -> y
        2 -> z
        else -> throw IllegalArgumentException("index is out of range: $index")
    }

    /**
     * Returns a new vector with the same components.
     *
     * @return A clone of this instance.
     */
    public fun clone(): Vector3 = Vector3(x, y, z)

    /**
     * Copies the components of [v] into this instance.
     *
     * @return A reference to this vector.
     */
    public fun copy(v: Vector3): Vector3 {
        x = v.x
        y = v.y
        z = v.z
        return this
    }

    /**
     * Adds [v] to this instance.
     *
     * @return A reference to this vector.
     */
    public fun add(v: Vector3): Vector3 {
        x += v.x
        y += v.y
        z += v.z
        return this
    }

    /**
     * Adds the scalar [s] to all components.
     *
     * @return A reference to this vector.
     */
    public fun addScalar(s: Double): Vector3 {
        x += s
        y += s
        z += s
        return this
    }

    /**
     * Sets this vector to `a + b`.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return A reference to this vector.
     */
    public fun addVectors(a: Vector3, b: Vector3): Vector3 {
        x = a.x + b.x
        y = a.y + b.y
        z = a.z + b.z
        return this
    }

    /**
     * Adds [v] scaled by [s] to this instance.
     *
     * @return A reference to this vector.
     */
    public fun addScaledVector(v: Vector3, s: Double): Vector3 {
        x += v.x * s
        y += v.y * s
        z += v.z * s
        return this
    }

    /**
     * Subtracts [v] from this instance.
     *
     * @return A reference to this vector.
     */
    public fun sub(v: Vector3): Vector3 {
        x -= v.x
        y -= v.y
        z -= v.z
        return this
    }

    /**
     * Subtracts the scalar [s] from all components.
     *
     * @return A reference to this vector.
     */
    public fun subScalar(s: Double): Vector3 {
        x -= s
        y -= s
        z -= s
        return this
    }

    /**
     * Sets this vector to `a - b`.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return A reference to this vector.
     */
    public fun subVectors(a: Vector3, b: Vector3): Vector3 {
        x = a.x - b.x
        y = a.y - b.y
        z = a.z - b.z
        return this
    }

    /**
     * Multiplies this instance component-wise by [v].
     *
     * @return A reference to this vector.
     */
    public fun multiply(v: Vector3): Vector3 {
        x *= v.x
        y *= v.y
        z *= v.z
        return this
    }

    /**
     * Multiplies all components by [scalar].
     *
     * @return A reference to this vector.
     */
    public fun multiplyScalar(scalar: Double): Vector3 {
        x *= scalar
        y *= scalar
        z *= scalar
        return this
    }

    /**
     * Sets this vector to `a * b` (component-wise).
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return A reference to this vector.
     */
    public fun multiplyVectors(a: Vector3, b: Vector3): Vector3 {
        x = a.x * b.x
        y = a.y * b.y
        z = a.z * b.z
        return this
    }

    /**
     * Applies the Euler rotation [euler] to this vector.
     *
     * @return A reference to this vector.
     */
    public fun applyEuler(euler: Euler): Vector3 =
        applyQuaternion(Quaternion().setFromEuler(euler))

    /**
     * Applies a rotation specified by [axis] (a normalized vector) and [angle] (in
     * radians) to this vector.
     *
     * @return A reference to this vector.
     */
    public fun applyAxisAngle(axis: Vector3, angle: Double): Vector3 =
        applyQuaternion(Quaternion().setFromAxisAngle(axis, angle))

    /**
     * Multiplies this vector by the 3x3 matrix [m].
     *
     * @return A reference to this vector.
     */
    public fun applyMatrix3(m: Matrix3): Vector3 {
        val x = this.x
        val y = this.y
        val z = this.z
        val e = m.elements

        this.x = e[0] * x + e[3] * y + e[6] * z
        this.y = e[1] * x + e[4] * y + e[7] * z
        this.z = e[2] * x + e[5] * y + e[8] * z

        return this
    }

    /**
     * Multiplies this vector by the normal matrix [m] and normalizes the result.
     *
     * @return A reference to this vector.
     */
    public fun applyNormalMatrix(m: Matrix3): Vector3 =
        applyMatrix3(m).normalize()

    /**
     * Multiplies this vector (with an implicit `1` in the 4th dimension) by [m],
     * and divides by perspective.
     *
     * @return A reference to this vector.
     */
    public fun applyMatrix4(m: Matrix4): Vector3 {
        val x = this.x
        val y = this.y
        val z = this.z
        val e = m.elements

        val w = 1 / (e[3] * x + e[7] * y + e[11] * z + e[15])

        this.x = (e[0] * x + e[4] * y + e[8] * z + e[12]) * w
        this.y = (e[1] * x + e[5] * y + e[9] * z + e[13]) * w
        this.z = (e[2] * x + e[6] * y + e[10] * z + e[14]) * w

        return this
    }

    /**
     * Applies the Quaternion [q] (assumed to have unit length) to this vector.
     *
     * @return A reference to this vector.
     */
    public fun applyQuaternion(q: Quaternion): Vector3 {
        // quaternion q is assumed to have unit length

        val vx = x
        val vy = y
        val vz = z
        val qx = q.x
        val qy = q.y
        val qz = q.z
        val qw = q.w

        // t = 2 * cross( q.xyz, v );
        val tx = 2 * (qy * vz - qz * vy)
        val ty = 2 * (qz * vx - qx * vz)
        val tz = 2 * (qx * vy - qy * vx)

        // v + q.w * t + cross( q.xyz, t );
        x = vx + qw * tx + qy * tz - qz * ty
        y = vy + qw * ty + qz * tx - qx * tz
        z = vz + qw * tz + qx * ty - qy * tx

        return this
    }

    // project(camera)/unproject(camera) need core Camera — deferred (see port-ledger.yaml).

    /**
     * Transforms the direction of this vector by the upper-left 3x3 of the 4x4
     * matrix [m], then normalizes the result.
     *
     * @return A reference to this vector.
     */
    public fun transformDirection(m: Matrix4): Vector3 {
        // input: Matrix4 affine matrix
        // vector interpreted as a direction

        val x = this.x
        val y = this.y
        val z = this.z
        val e = m.elements

        this.x = e[0] * x + e[4] * y + e[8] * z
        this.y = e[1] * x + e[5] * y + e[9] * z
        this.z = e[2] * x + e[6] * y + e[10] * z

        return normalize()
    }

    /**
     * Divides this instance component-wise by [v].
     *
     * @return A reference to this vector.
     */
    public fun divide(v: Vector3): Vector3 {
        x /= v.x
        y /= v.y
        z /= v.z
        return this
    }

    /**
     * Divides all components by [scalar].
     *
     * @return A reference to this vector.
     */
    public fun divideScalar(scalar: Double): Vector3 = multiplyScalar(1.0 / scalar)

    /**
     * Sets each component to the minimum of itself and [v]'s corresponding component.
     *
     * @return A reference to this vector.
     */
    public fun min(v: Vector3): Vector3 {
        x = minOf(x, v.x)
        y = minOf(y, v.y)
        z = minOf(z, v.z)
        return this
    }

    /**
     * Sets each component to the maximum of itself and [v]'s corresponding component.
     *
     * @return A reference to this vector.
     */
    public fun max(v: Vector3): Vector3 {
        x = maxOf(x, v.x)
        y = maxOf(y, v.y)
        z = maxOf(z, v.z)
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
    public fun clamp(min: Vector3, max: Vector3): Vector3 {
        // assumes min < max, componentwise
        x = MathUtils.clamp(x, min.x, max.x)
        y = MathUtils.clamp(y, min.y, max.y)
        z = MathUtils.clamp(z, min.z, max.z)
        return this
    }

    /**
     * Clamps each component into the scalar range `[minVal, maxVal]`.
     *
     * @param minVal The minimum value the components will be clamped to.
     * @param maxVal The maximum value the components will be clamped to.
     * @return A reference to this vector.
     */
    public fun clampScalar(minVal: Double, maxVal: Double): Vector3 {
        x = MathUtils.clamp(x, minVal, maxVal)
        y = MathUtils.clamp(y, minVal, maxVal)
        z = MathUtils.clamp(z, minVal, maxVal)
        return this
    }

    /**
     * Clamps this vector's length into `[min, max]`.
     *
     * @param min The minimum value the vector length will be clamped to.
     * @param max The maximum value the vector length will be clamped to.
     * @return A reference to this vector.
     */
    public fun clampLength(min: Double, max: Double): Vector3 {
        val length = length()
        return divideScalar(length.orOne()).multiplyScalar(MathUtils.clamp(length, min, max))
    }

    /**
     * Rounds each component down to the nearest integer.
     *
     * @return A reference to this vector.
     */
    public fun floor(): Vector3 {
        x = floor(x)
        y = floor(y)
        z = floor(z)
        return this
    }

    /**
     * Rounds each component up to the nearest integer.
     *
     * @return A reference to this vector.
     */
    public fun ceil(): Vector3 {
        x = ceil(x)
        y = ceil(y)
        z = ceil(z)
        return this
    }

    /**
     * Rounds each component to the nearest integer (halves toward +infinity, as
     * JS `Math.round`).
     *
     * @return A reference to this vector.
     */
    public fun round(): Vector3 {
        x = MathUtils.jsRound(x)
        y = MathUtils.jsRound(y)
        z = MathUtils.jsRound(z)
        return this
    }

    /**
     * Rounds each component toward zero.
     *
     * @return A reference to this vector.
     */
    public fun roundToZero(): Vector3 {
        x = truncate(x)
        y = truncate(y)
        z = truncate(z)
        return this
    }

    /**
     * Negates each component.
     *
     * @return A reference to this vector.
     */
    public fun negate(): Vector3 {
        x = -x
        y = -y
        z = -z
        return this
    }

    /**
     * Returns the dot product of this vector and [v].
     *
     * @return The result of the dot product.
     */
    public fun dot(v: Vector3): Double = x * v.x + y * v.y + z * v.z

    /**
     * Returns the squared Euclidean length of this vector.
     *
     * @return The square length of this vector.
     */
    public fun lengthSq(): Double = x * x + y * y + z * z

    /**
     * Returns the Euclidean length of this vector.
     *
     * @return The length of this vector.
     */
    public fun length(): Double = sqrt(x * x + y * y + z * z)

    /**
     * Returns the Manhattan length of this vector.
     *
     * @return The length of this vector.
     */
    public fun manhattanLength(): Double = abs(x) + abs(y) + abs(z)

    /**
     * Normalizes this vector to unit length (leaves a zero vector unchanged).
     *
     * @return A reference to this vector.
     */
    public fun normalize(): Vector3 = divideScalar(length().orOne())

    /**
     * Sets this vector's length to [length], preserving direction.
     *
     * @return A reference to this vector.
     */
    public fun setLength(length: Double): Vector3 = normalize().multiplyScalar(length)

    /**
     * Linearly interpolates from this vector toward [v] by [alpha].
     *
     * @return A reference to this vector.
     */
    public fun lerp(v: Vector3, alpha: Double): Vector3 {
        x += (v.x - x) * alpha
        y += (v.y - y) * alpha
        z += (v.z - z) * alpha
        return this
    }

    /**
     * Sets this vector to the linear interpolation of [v1] and [v2] by [alpha].
     *
     * @return A reference to this vector.
     */
    public fun lerpVectors(v1: Vector3, v2: Vector3, alpha: Double): Vector3 {
        x = v1.x + (v2.x - v1.x) * alpha
        y = v1.y + (v2.y - v1.y) * alpha
        z = v1.z + (v2.z - v1.z) * alpha
        return this
    }

    /**
     * Sets this vector to the cross product of this vector and [v].
     *
     * @return A reference to this vector.
     */
    public fun cross(v: Vector3): Vector3 = crossVectors(this, v)

    /**
     * Sets this vector to the cross product of [a] and [b].
     *
     * @return A reference to this vector.
     */
    public fun crossVectors(a: Vector3, b: Vector3): Vector3 {
        val ax = a.x
        val ay = a.y
        val az = a.z
        val bx = b.x
        val by = b.y
        val bz = b.z

        x = ay * bz - az * by
        y = az * bx - ax * bz
        z = ax * by - ay * bx

        return this
    }

    /**
     * Projects this vector onto [v].
     *
     * @return A reference to this vector.
     */
    public fun projectOnVector(v: Vector3): Vector3 {
        val denominator = v.lengthSq()

        if (denominator == 0.0) return set(0.0, 0.0, 0.0)

        val scalar = v.dot(this) / denominator

        return copy(v).multiplyScalar(scalar)
    }

    /**
     * Projects this vector onto the plane with normal [planeNormal] (by subtracting
     * this vector's projection onto the normal). Leaves the vector unchanged when
     * [planeNormal] is a zero vector.
     *
     * @return A reference to this vector.
     */
    public fun projectOnPlane(planeNormal: Vector3): Vector3 {
        // three.js copies `this` into a scratch vector and projectOnVector()s it;
        // inlined here as component math (no module temp). The projection is
        // planeNormal * (planeNormal.dot(this) / planeNormal.lengthSq()); a zero
        // denominator leaves `this` unchanged (the projection is the zero vector).
        val denominator = planeNormal.lengthSq()

        if (denominator == 0.0) return this

        val scalar = planeNormal.dot(this) / denominator

        x -= planeNormal.x * scalar
        y -= planeNormal.y * scalar
        z -= planeNormal.z * scalar

        return this
    }

    /**
     * Reflects this vector off a plane orthogonal to the (normalized) [normal].
     *
     * @return A reference to this vector.
     */
    public fun reflect(normal: Vector3): Vector3 {
        // three.js: this - normal * (2 * this.dot(normal)); inlined as component
        // math (no module temp).
        val scalar = 2 * dot(normal)

        x -= normal.x * scalar
        y -= normal.y * scalar
        z -= normal.z * scalar

        return this
    }

    /**
     * Returns the angle between this vector and [v], in radians.
     *
     * @return The angle in radians.
     */
    public fun angleTo(v: Vector3): Double {
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
    public fun distanceTo(v: Vector3): Double = sqrt(distanceToSquared(v))

    /**
     * Returns the squared distance from this vector to [v].
     *
     * @return The squared distance.
     */
    public fun distanceToSquared(v: Vector3): Double {
        val dx = x - v.x
        val dy = y - v.y
        val dz = z - v.z
        return dx * dx + dy * dy + dz * dz
    }

    /**
     * Returns the Manhattan distance from this vector to [v].
     *
     * @return The Manhattan distance.
     */
    public fun manhattanDistanceTo(v: Vector3): Double =
        abs(x - v.x) + abs(y - v.y) + abs(z - v.z)

    /**
     * Sets the vector components from the spherical coordinates [s].
     *
     * @return A reference to this vector.
     */
    public fun setFromSpherical(s: Spherical): Vector3 =
        setFromSphericalCoords(s.radius, s.phi, s.theta)

    /**
     * Sets the vector components from the spherical coordinates [radius], [phi]
     * (in radians) and [theta] (in radians).
     *
     * @return A reference to this vector.
     */
    public fun setFromSphericalCoords(radius: Double, phi: Double, theta: Double): Vector3 {
        val sinPhiRadius = sin(phi) * radius

        x = sinPhiRadius * sin(theta)
        y = cos(phi) * radius
        z = sinPhiRadius * cos(theta)

        return this
    }

    /**
     * Sets the vector components from the cylindrical coordinates [c].
     *
     * @return A reference to this vector.
     */
    public fun setFromCylindrical(c: Cylindrical): Vector3 =
        setFromCylindricalCoords(c.radius, c.theta, c.y)

    /**
     * Sets the vector components from the cylindrical coordinates [radius], [theta]
     * (in radians) and [y].
     *
     * @return A reference to this vector.
     */
    public fun setFromCylindricalCoords(radius: Double, theta: Double, y: Double): Vector3 {
        x = radius * sin(theta)
        this.y = y
        z = radius * cos(theta)

        return this
    }

    /**
     * Sets the vector components to the position elements of the transformation
     * matrix [m].
     *
     * @return A reference to this vector.
     */
    public fun setFromMatrixPosition(m: Matrix4): Vector3 {
        val e = m.elements

        x = e[12]
        y = e[13]
        z = e[14]

        return this
    }

    /**
     * Sets the vector components to the scale elements of the transformation
     * matrix [m].
     *
     * @return A reference to this vector.
     */
    public fun setFromMatrixScale(m: Matrix4): Vector3 {
        val sx = setFromMatrixColumn(m, 0).length()
        val sy = setFromMatrixColumn(m, 1).length()
        val sz = setFromMatrixColumn(m, 2).length()

        x = sx
        y = sy
        z = sz

        return this
    }

    /**
     * Sets the vector components from column [index] of the 4x4 matrix [m].
     *
     * @return A reference to this vector.
     */
    public fun setFromMatrixColumn(m: Matrix4, index: Int): Vector3 =
        fromArray(m.elements, index * 4)

    /**
     * Sets the vector components from column [index] of the 3x3 matrix [m].
     *
     * @return A reference to this vector.
     */
    public fun setFromMatrix3Column(m: Matrix3, index: Int): Vector3 =
        fromArray(m.elements, index * 3)

    /**
     * Sets the vector components from the Euler angles [e].
     *
     * @return A reference to this vector.
     */
    public fun setFromEuler(e: Euler): Vector3 {
        x = e.x
        y = e.y
        z = e.z

        return this
    }

    /**
     * Sets the vector components from the RGB components of the color [c]
     * (`x = r`, `y = g`, `z = b`).
     *
     * @return A reference to this vector.
     */
    public fun setFromColor(c: Color): Vector3 {
        x = c.r
        y = c.g
        z = c.b

        return this
    }

    /**
     * Sets `x = array[offset]`, `y = array[offset + 1]`, `z = array[offset + 2]`.
     *
     * @param array An array holding the vector component values.
     * @return A reference to this vector.
     */
    public fun fromArray(array: DoubleArray, offset: Int = 0): Vector3 {
        x = array[offset]
        y = array[offset + 1]
        z = array[offset + 2]
        return this
    }

    /**
     * Writes this vector's components into [array] at [offset], growing the list as
     * needed, and returns it.
     *
     * @return The vector components.
     */
    public fun toArray(array: MutableList<Double> = mutableListOf(), offset: Int = 0): MutableList<Double> {
        while (array.size < offset + 3) array.add(0.0)
        array[offset] = x
        array[offset + 1] = y
        array[offset + 2] = z
        return array
    }

    /**
     * Sets this vector's components from [attribute] at [index].
     *
     * @return A reference to this vector.
     */
    public fun fromBufferAttribute(attribute: AttributeLike, index: Int): Vector3 {
        x = attribute.getX(index)
        y = attribute.getY(index)
        z = attribute.getZ(index)
        return this
    }

    /**
     * Sets each component to a pseudo-random value in `[0, 1)`.
     *
     * @return A reference to this vector.
     */
    public fun random(): Vector3 {
        x = kotlin.random.Random.nextDouble()
        y = kotlin.random.Random.nextDouble()
        z = kotlin.random.Random.nextDouble()
        return this
    }

    /**
     * Sets this vector to a uniformly random point on a unit sphere.
     *
     * @return A reference to this vector.
     */
    public fun randomDirection(): Vector3 {
        // https://mathworld.wolfram.com/SpherePointPicking.html

        val theta = kotlin.random.Random.nextDouble() * PI * 2
        val u = kotlin.random.Random.nextDouble() * 2 - 1
        val c = sqrt(1 - u * u)

        x = c * cos(theta)
        y = u
        z = c * sin(theta)

        return this
    }

    override fun iterator(): Iterator<Double> = listOf(x, y, z).iterator()

    /**
     * Structural equality: `true` when [other] is a [Vector3] with equal components
     * (component-wise `==`, so `NaN != NaN`).
     */
    override fun equals(other: Any?): Boolean =
        other is Vector3 && other.x == x && other.y == y && other.z == z

    override fun hashCode(): Int {
        // (v + 0.0) collapses -0.0 to +0.0, keeping hashCode consistent with
        // equals treating -0.0 == +0.0.
        var result = (x + 0.0).hashCode()
        result = 31 * result + (y + 0.0).hashCode()
        result = 31 * result + (z + 0.0).hashCode()
        return result
    }

    override fun toString(): String = "Vector3(x=$x, y=$y, z=$z)"
}
