/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Interpolant.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

/**
 * The ending mode of a [CubicInterpolant] at a curve boundary.
 *
 * Mirrors the `ZeroCurvatureEnding` / `ZeroSlopeEnding` / `WrapAroundEnding`
 * integer constants from three.js `src/constants.js` (2400 / 2401 / 2402). The
 * numeric values are irrelevant to the port — only the identity matters — so
 * they are modeled as enum members rather than magic ints.
 */
public enum class InterpolantEnding {
    /** `f''(t) = 0`, a.k.a. Natural Spline. The three.js default (`ZeroCurvatureEnding`). */
    ZeroCurvature,

    /** `f'(t) = 0` (`ZeroSlopeEnding`). */
    ZeroSlope,

    /** Use the other end of the curve (`WrapAroundEnding`). */
    WrapAround,
}

/**
 * The interpolation settings that a derived interpolant may read.
 *
 * three.js stashes these on a plain `settings` object; different subclasses read
 * different properties off it:
 * - [CubicInterpolant] reads [endingStart]/[endingEnd].
 * - [BezierInterpolant] reads [inTangents]/[outTangents] (packed `[time, value]`
 *   pairs per keyframe per component; `null` ⇒ linear fallback).
 *
 * Modeled as a small holder so the base [Interpolant.getSettings_] contract
 * (`settings || DefaultSettings_`) ports faithfully. The tangent arrays are
 * `DoubleArray?` here (three.js uses `Float32Array`) to keep the seam entirely
 * within `math` — no core type is pulled in.
 *
 * @param endingStart The ending mode applied at the start of the curve.
 * @param endingEnd The ending mode applied at the end of the curve.
 * @param inTangents Packed in-tangent control points for [BezierInterpolant], or `null`.
 * @param outTangents Packed out-tangent control points for [BezierInterpolant], or `null`.
 */
public class InterpolantSettings(
    public var endingStart: InterpolantEnding = InterpolantEnding.ZeroCurvature,
    public var endingEnd: InterpolantEnding = InterpolantEnding.ZeroCurvature,
    public var inTangents: DoubleArray? = null,
    public var outTangents: DoubleArray? = null,
)

/**
 * Abstract base class of interpolants over parametric samples.
 *
 * The parameter domain is one dimensional, typically the time or a path along a
 * curve defined by the data. The sample values can have any dimensionality and
 * derived classes may apply special interpretations to the data.
 *
 * This class provides the interval seek in a Template Method, deferring the
 * actual interpolation to derived classes via [interpolate_].
 *
 * Time complexity is `O(1)` for linear access crossing at most two points and
 * `O(log N)` for random access, where `N` is the number of positions.
 *
 * Instances are **mutable** and **not thread-safe**; confine an instance (and
 * the buffers it wraps) to a single thread, exactly as in three.js. The
 * [resultBuffer] is reused across calls and its contents are overwritten by each
 * [evaluate].
 *
 * See [the Template Method pattern](http://www.oodesign.com/template-method-pattern.html).
 *
 * @param parameterPositions The parameter positions holding the interpolation factors.
 * @param sampleValues The sample values.
 * @param sampleSize The sample size (a.k.a. the value size / stride).
 * @param resultBuffer The result buffer; defaults to a fresh `DoubleArray(sampleSize)`.
 */
public abstract class Interpolant(
    /** The parameter positions. */
    public val parameterPositions: DoubleArray,
    /** The sample values. */
    public val sampleValues: DoubleArray,
    sampleSize: Int,
    /** The result buffer. Reused across calls; overwritten by every [evaluate]. */
    public val resultBuffer: DoubleArray = DoubleArray(sampleSize),
) {

    /** A cache index. */
    private var _cachedIndex: Int = 0

    /** The value size (stride). */
    public val valueSize: Int = sampleSize

    /**
     * The interpolation settings. `null` until assigned; [getSettings_] then
     * falls back to [DefaultSettings_].
     */
    public var settings: InterpolantSettings? = null

    /** The default settings object. Overridden by [CubicInterpolant]. */
    public open val DefaultSettings_: InterpolantSettings = InterpolantSettings()

    /**
     * Reads `parameterPositions[i]`, returning `null` for an out-of-bounds
     * index. Models the JS `pp[i] === undefined` boundary tests faithfully: in
     * JS an out-of-range typed-array access yields `undefined`, whereas a Kotlin
     * `DoubleArray` access throws — so every boundary read in [evaluate] goes
     * through this helper.
     */
    private fun ppGet(i: Int): Double? {
        val pp = parameterPositions
        return if (i < 0 || i >= pp.size) null else pp[i]
    }

    /**
     * Evaluates the interpolant at position [t].
     *
     * @param t The interpolation factor.
     * @return The result buffer.
     */
    public fun evaluate(t: Double): DoubleArray {
        val pp = parameterPositions
        var i1 = _cachedIndex
        var t1 = ppGet(i1)
        var t0 = ppGet(i1 - 1)

        // Emulates the JS labeled-block control flow (validate_interval / seek /
        // linear_scan / forward_scan). Kotlin has no labeled blocks with `break
        // label`, so each labeled region is expressed with a small flag/loop.
        var seekDone = false // set true once the sought interval is found (JS `break seek`)
        var intervalValid = false // set true when the cached interval is still valid (JS `break validate_interval`)
        var right = 0

        run linearScan@{
            // forward_scan: if ( ! ( t < t1 ) )
            var forwardFellThrough = false
            if (!(t1 != null && t < t1)) {
                val giveUpAt = i1 + 2
                var brokeForwardScan = false
                while (true) {
                    if (t1 == null) {
                        if (t0 != null && t < t0) {
                            brokeForwardScan = true
                            break // break forward_scan
                        }

                        // after end
                        i1 = pp.size
                        _cachedIndex = i1
                        return copySampleValue_(i1 - 1)
                    }

                    if (i1 == giveUpAt) break // this loop

                    t0 = t1
                    t1 = ppGet(++i1)

                    if (t1 != null && t < t1) {
                        // we have arrived at the sought interval
                        seekDone = true
                        return@linearScan
                    }
                }

                if (!brokeForwardScan) {
                    // Loop exited via plain `break` (JS: `i1 === giveUpAt`): prepare
                    // binary search on the right side of the index (JS `break linear_scan`).
                    right = pp.size
                    return@linearScan // break linear_scan
                }
                // Loop exited via JS `break forward_scan`, which fires only when
                // `t1 == null && t < t0`. Control then falls through to the reverse
                // check `if (!(t >= t0))`; since `t < t0` holds there, that check is
                // always true, so forcing entry below is equivalent.
                forwardFellThrough = true
            }

            // if ( ! ( t >= t0 ) )   (forwardFellThrough forces entry — see note above)
            if (forwardFellThrough || !(t0 != null && t >= t0)) {
                // looping?
                val t1global = ppGet(1)

                if (t1global != null && t < t1global) {
                    i1 = 2 // + 1, using the scan for the details
                    t0 = t1global
                }

                // linear reverse scan
                val giveUpAt = i1 - 2
                while (true) {
                    if (t0 == null) {
                        // before start
                        _cachedIndex = 0
                        return copySampleValue_(0)
                    }

                    if (i1 == giveUpAt) break // this loop

                    t1 = t0
                    t0 = ppGet(--i1 - 1)

                    if (t0 != null && t >= t0) {
                        // we have arrived at the sought interval
                        seekDone = true
                        return@linearScan
                    }
                }

                // prepare binary search on the left side of the index
                right = i1
                i1 = 0
                return@linearScan // break linear_scan
            }

            // the interval is valid
            intervalValid = true // break validate_interval
        } // linear scan

        if (!intervalValid) {
            if (!seekDone) {
                // binary search
                while (i1 < right) {
                    val mid = (i1 + right) ushr 1

                    val ppMid = ppGet(mid)
                    if (ppMid != null && t < ppMid) {
                        right = mid
                    } else {
                        i1 = mid + 1
                    }
                }

                t1 = ppGet(i1)
                t0 = ppGet(i1 - 1)

                // check boundary cases, again
                if (t0 == null) {
                    _cachedIndex = 0
                    return copySampleValue_(0)
                }

                if (t1 == null) {
                    i1 = pp.size
                    _cachedIndex = i1
                    return copySampleValue_(i1 - 1)
                }
            } // seek

            _cachedIndex = i1

            intervalChanged_(i1, t0!!, t1!!)
        } // validate_interval

        return interpolate_(i1, t0!!, t, t1!!)
    }

    /**
     * Returns the interpolation settings.
     *
     * @return The interpolation settings.
     */
    public fun getSettings_(): InterpolantSettings = settings ?: DefaultSettings_

    /**
     * Copies a sample value to the result buffer.
     *
     * @param index An index into the sample value buffer.
     * @return The result buffer.
     */
    public fun copySampleValue_(index: Int): DoubleArray {
        // copies a sample value to the result buffer
        val result = resultBuffer
        val values = sampleValues
        val stride = valueSize
        val offset = index * stride

        var i = 0
        while (i != stride) {
            result[i] = values[offset + i]
            ++i
        }

        return result
    }

    /**
     * Interpolates within the interval `[t0, t1]` at position [t] and writes the
     * result into [resultBuffer].
     *
     * @param i1 An index into the sample value buffer.
     * @param t0 The previous interpolation factor.
     * @param t The current interpolation factor.
     * @param t1 The next interpolation factor.
     * @return The result buffer.
     */
    protected abstract fun interpolate_(i1: Int, t0: Double, t: Double, t1: Double): DoubleArray

    /**
     * Optional hook executed when the interval has changed. Defaults to a no-op;
     * [CubicInterpolant] overrides it to recompute Hermite weights.
     *
     * @param i1 An index into the sample value buffer.
     * @param t0 The previous interpolation factor.
     * @param t1 The next interpolation factor.
     */
    protected open fun intervalChanged_(i1: Int, t0: Double, t1: Double) {
        // empty
    }
}
