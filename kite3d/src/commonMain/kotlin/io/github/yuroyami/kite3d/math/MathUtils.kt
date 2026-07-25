/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/MathUtils.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * The storage type of a buffer component, used by [MathUtils.normalize] and
 * [MathUtils.denormalize]. In three.js the corresponding information is carried
 * by the JavaScript `TypedArray` constructor; common Kotlin has no equivalent, so
 * the type is passed explicitly.
 */
public enum class ComponentType {
    Float32,
    Uint32,
    Uint16,
    Uint8,
    Int32,
    Int16,
    Int8,
}

/**
 * The axis order for [MathUtils.setQuaternionFromProperEuler].
 *
 * These are *proper* (a.k.a. classic) Euler orders, where the first and third
 * axes repeat (`XYX`, `ZXZ`, …). They are distinct from [EulerOrder], whose
 * orders use three different axes (`XYZ`, `ZXY`, …).
 */
public enum class ProperEulerOrder { XYX, XZX, YXY, YZY, ZXZ, ZYZ }

/**
 * A collection of math utility functions.
 *
 * Ported from three.js `MathUtils`. Modelled as a Kotlin `object` so that ported
 * three.js code keeps calling `MathUtils.clamp(...)` etc. verbatim.
 */
public object MathUtils {

    /** Multiply by this to convert degrees to radians. */
    public const val DEG2RAD: Double = PI / 180.0

    /** Multiply by this to convert radians to degrees. */
    public const val RAD2DEG: Double = 180.0 / PI

    // 256 two-digit lowercase hex strings: the UUID nibble lookup table.
    private val lut: Array<String> = Array(256) { it.toString(16).padStart(2, '0') }

    private var seed: Int = 1234567

    /**
     * Replicates JS `Math.round` (half toward +infinity), which differs from
     * [kotlin.math.round] (half to even). Shared by vector `round()` methods.
     */
    internal fun jsRound(x: Double): Double = floor(x + 0.5)

    /**
     * Generates a [UUID](https://en.wikipedia.org/wiki/Universally_unique_identifier).
     *
     * @return The UUID.
     */
    public fun generateUUID(): String {

        // http://stackoverflow.com/questions/105034/how-to-create-a-guid-uuid-in-javascript/21963136#21963136
        // The `.toLong().toInt()` narrowing reproduces JS `x | 0` (ToInt32): take
        // the low 32 bits and reinterpret as a signed Int. `shr` matches JS `>>`.

        val d0 = (Random.nextDouble() * 0xffffffff).toLong().toInt()
        val d1 = (Random.nextDouble() * 0xffffffff).toLong().toInt()
        val d2 = (Random.nextDouble() * 0xffffffff).toLong().toInt()
        val d3 = (Random.nextDouble() * 0xffffffff).toLong().toInt()

        return lut[d0 and 0xff] + lut[(d0 shr 8) and 0xff] + lut[(d0 shr 16) and 0xff] + lut[(d0 shr 24) and 0xff] + "-" +
            lut[d1 and 0xff] + lut[(d1 shr 8) and 0xff] + "-" + lut[(d1 shr 16) and 0x0f or 0x40] + lut[(d1 shr 24) and 0xff] + "-" +
            lut[d2 and 0x3f or 0x80] + lut[(d2 shr 8) and 0xff] + "-" + lut[(d2 shr 16) and 0xff] + lut[(d2 shr 24) and 0xff] +
            lut[d3 and 0xff] + lut[(d3 shr 8) and 0xff] + lut[(d3 shr 16) and 0xff] + lut[(d3 shr 24) and 0xff]

    }

    /**
     * Clamps [value] between [minVal] and [maxVal].
     *
     * Uses `max(min, min(max, value))`. It never uses [Double.coerceIn], which throws
     * when `min > max` (a normal state for empty boxes: min=+inf, max=-inf).
     *
     * @return The clamped value.
     */
    public fun clamp(value: Double, minVal: Double, maxVal: Double): Double =
        max(minVal, min(maxVal, value))

    /**
     * Computes the Euclidean modulo `((n % m) + m) % m`.
     *
     * @param n The dividend.
     * @param m The divisor.
     * @return The Euclidean modulo.
     */
    public fun euclideanModulo(n: Double, m: Double): Double =
        ((n % m) + m) % m

    /**
     * Linearly maps [x] from range `[a1, a2]` to range `[b1, b2]`.
     *
     * @param a1 Minimum value for range A.
     * @param a2 Maximum value for range A.
     * @param b1 Minimum value for range B.
     * @param b2 Maximum value for range B.
     * @return The mapped value.
     */
    public fun mapLinear(x: Double, a1: Double, a2: Double, b1: Double, b2: Double): Double =
        b1 + (x - a1) * (b2 - b1) / (a2 - a1)

    /**
     * Returns the fraction in `[0, 1]` that [value] represents between [x] and [y].
     * Returns `0` when `x == y` (rather than `NaN`).
     *
     * @return The interpolation factor.
     */
    public fun inverseLerp(x: Double, y: Double, value: Double): Double =
        if (x != y) (value - x) / (y - x) else 0.0

    /**
     * Linearly interpolates between [x] and [y] by [t]; `t = 0` returns [x],
     * `t = 1` returns [y].
     *
     * @return The interpolated value.
     */
    public fun lerp(x: Double, y: Double, t: Double): Double =
        (1 - t) * x + t * y

    /**
     * Frame-rate-independent damping: smoothly moves [x] toward [y] using [lambda]
     * and delta time [dt] in seconds.
     *
     * @return The interpolated value.
     */
    public fun damp(x: Double, y: Double, lambda: Double, dt: Double): Double =
        lerp(x, y, 1 - exp(-lambda * dt))

    /**
     * Returns a value that alternates ("ping-pongs") between `0` and [length].
     *
     * @param x The value to pingpong.
     * @return The alternated value.
     */
    public fun pingpong(x: Double, length: Double = 1.0): Double =
        length - abs(euclideanModulo(x, length * 2) - length)

    /**
     * Smoothstep: a value in `[0, 1]` for [x] between [minVal] and [maxVal],
     * eased at both ends.
     *
     * @return The smoothed value.
     */
    public fun smoothstep(x: Double, minVal: Double, maxVal: Double): Double {

        if (x <= minVal) return 0.0
        if (x >= maxVal) return 1.0

        val t = (x - minVal) / (maxVal - minVal)

        return t * t * (3 - 2 * t)

    }

    /**
     * Smootherstep: a variation on [smoothstep] with zero 1st and 2nd derivatives
     * at both ends.
     *
     * @param x The value to evaluate based on its position between min and max.
     * @param minVal Any x value below this is `0`.
     * @param maxVal Any x value above this is `1`.
     * @return The smoothed value.
     */
    public fun smootherstep(x: Double, minVal: Double, maxVal: Double): Double {

        if (x <= minVal) return 0.0
        if (x >= maxVal) return 1.0

        val t = (x - minVal) / (maxVal - minVal)

        return t * t * t * (t * (t * 6 - 15) + 10)

    }

    /**
     * Returns a random integer in the closed interval `[low, high]`.
     *
     * @param low The lower value boundary.
     * @param high The upper value boundary.
     * @return A random integer.
     */
    public fun randInt(low: Int, high: Int): Int =
        low + floor(Random.nextDouble() * (high - low + 1)).toInt()

    /**
     * Returns a random float in the interval `[low, high)`.
     *
     * @param low The lower value boundary.
     * @param high The upper value boundary.
     * @return A random float.
     */
    public fun randFloat(low: Double, high: Double): Double =
        low + Random.nextDouble() * (high - low)

    /**
     * Returns a random float in the interval `(-range/2, range/2)`.
     *
     * @param range Defines the value range.
     * @return A random float.
     */
    public fun randFloatSpread(range: Double): Double =
        range * (0.5 - Random.nextDouble())

    /**
     * Returns a deterministic pseudo-random float in `[0, 1)` (Mulberry32).
     *
     * @param s Optional integer seed. When given, (re)seeds the generator.
     * @return A random float.
     */
    public fun seededRandom(s: Int? = null): Double {

        if (s != null) seed = s

        // Mulberry32. Kotlin Int arithmetic wraps mod 2^32 (two's complement), so
        // `+`/`*` match JS `Math.imul` and int32 accumulation; `ushr` is JS `>>>`.
        seed += 0x6D2B79F5
        var t = seed

        t = (t xor (t ushr 15)) * (t or 1)
        t = t xor (t + (t xor (t ushr 7)) * (t or 61))

        return ((t xor (t ushr 14)).toLong() and 0xFFFFFFFFL).toDouble() / 4294967296.0

    }

    /**
     * Converts [degrees] to radians.
     *
     * @return The value in radians.
     */
    public fun degToRad(degrees: Double): Double = degrees * DEG2RAD

    /**
     * Converts [radians] to degrees.
     *
     * @return The value in degrees.
     */
    public fun radToDeg(radians: Double): Double = radians * RAD2DEG

    /**
     * Returns `true` if [value] is a power of two.
     *
     * @return Whether the given number is a power of two or not.
     */
    public fun isPowerOfTwo(value: Int): Boolean =
        (value and (value - 1)) == 0 && value != 0

    /**
     * Returns the smallest power of two greater than or equal to [value]
     * (which must be greater than `0`).
     *
     * @return The smallest power of two that is greater than or equal to the given number.
     */
    public fun ceilPowerOfTwo(value: Double): Double =
        2.0.pow(ceil(ln(value) / ln(2.0)))

    /**
     * Returns the largest power of two less than or equal to [value]
     * (which must be greater than `0`).
     *
     * @return The largest power of two that is less than or equal to the given number.
     */
    public fun floorPowerOfTwo(value: Double): Double =
        2.0.pow(floor(ln(value) / ln(2.0)))

    /**
     * Denormalizes [value] according to the component storage [type].
     *
     * @return The denormalized (float) value.
     */
    public fun denormalize(value: Double, type: ComponentType): Double = when (type) {
        ComponentType.Float32 -> value
        ComponentType.Uint32 -> value / 4294967295.0
        ComponentType.Uint16 -> value / 65535.0
        ComponentType.Uint8 -> value / 255.0
        ComponentType.Int32 -> max(value / 2147483647.0, -1.0)
        ComponentType.Int16 -> max(value / 32767.0, -1.0)
        ComponentType.Int8 -> max(value / 127.0, -1.0)
    }

    /**
     * Normalizes [value] (in `[0, 1]` for unsigned, `[-1, 1]` for signed)
     * according to the component storage [type].
     *
     * @return The normalized integer value (as a [Double]).
     */
    public fun normalize(value: Double, type: ComponentType): Double = when (type) {
        ComponentType.Float32 -> value
        ComponentType.Uint32 -> jsRound(value * 4294967295.0)
        ComponentType.Uint16 -> jsRound(value * 65535.0)
        ComponentType.Uint8 -> jsRound(value * 255.0)
        ComponentType.Int32 -> jsRound(value * 2147483647.0)
        ComponentType.Int16 -> jsRound(value * 32767.0)
        ComponentType.Int8 -> jsRound(value * 127.0)
    }

    /**
     * Sets the given quaternion [q] from the
     * [Intrinsic Proper Euler Angles](https://en.wikipedia.org/wiki/Euler_angles)
     * defined by [a], [b], [c] and [order].
     *
     * Rotations are applied to the axes in the order specified by [order]:
     * rotation by angle [a] is applied first, then by angle [b], then by angle [c].
     *
     * @param q The quaternion to set.
     * @param a The rotation applied to the first axis, in radians.
     * @param b The rotation applied to the second axis, in radians.
     * @param c The rotation applied to the third axis, in radians.
     * @param order A [ProperEulerOrder] specifying the axes order.
     */
    public fun setQuaternionFromProperEuler(q: Quaternion, a: Double, b: Double, c: Double, order: ProperEulerOrder) {

        val c2 = cos(b / 2)
        val s2 = sin(b / 2)

        val c13 = cos((a + c) / 2)
        val s13 = sin((a + c) / 2)

        val c1_3 = cos((a - c) / 2)
        val s1_3 = sin((a - c) / 2)

        val c3_1 = cos((c - a) / 2)
        val s3_1 = sin((c - a) / 2)

        // Upstream's default branch only console.warns on an unknown string order;
        // with an exhaustive enum `when` that branch is unreachable, so it is dropped.
        when (order) {
            ProperEulerOrder.XYX -> q.set(c2 * s13, s2 * c1_3, s2 * s1_3, c2 * c13)
            ProperEulerOrder.YZY -> q.set(s2 * s1_3, c2 * s13, s2 * c1_3, c2 * c13)
            ProperEulerOrder.ZXZ -> q.set(s2 * c1_3, s2 * s1_3, c2 * s13, c2 * c13)
            ProperEulerOrder.XZX -> q.set(c2 * s13, s2 * s3_1, s2 * c3_1, c2 * c13)
            ProperEulerOrder.YXY -> q.set(s2 * c3_1, c2 * s13, s2 * s3_1, c2 * c13)
            ProperEulerOrder.ZYZ -> q.set(s2 * s3_1, s2 * c3_1, c2 * s13, c2 * c13)
        }

    }

    // -- shared numeric constants used across the math layer --------------------

    /**
     * The difference between `1.0` and the next representable [Double]
     * (`2^-52`). This is the Kotlin equivalent of JS `Number.EPSILON`. Shared by the
     * math layer (e.g. `Quaternion.slerp`, `Matrix4.decompose`).
     */
    internal const val EPSILON: Double = 2.220446049250313E-16
}

/**
 * Replicates the JS idiom `x || 1`: returns `1.0` when the receiver is falsy as a
 * JS number (`0.0`, `-0.0` or `NaN`), otherwise the receiver. Used by vector
 * `normalize`/`clampLength`/`setLength` to avoid division by a zero length.
 */
internal fun Double.orOne(): Double = if (this == 0.0 || this.isNaN()) 1.0 else this
