/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/interpolants/CubicInterpolant.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math.interpolants

import io.github.yuroyami.kite3d.math.Interpolant
import io.github.yuroyami.kite3d.math.InterpolantEnding
import io.github.yuroyami.kite3d.math.InterpolantSettings

/**
 * Fast and simple cubic spline interpolant.
 *
 * It was derived from a Hermitian construction setting the first derivative at
 * each sample position to the linear slope between neighboring positions over
 * their parameter interval.
 *
 * @param parameterPositions The parameter positions holding the interpolation factors.
 * @param sampleValues The sample values.
 * @param sampleSize The sample size (a.k.a. the value size / stride).
 * @param resultBuffer The result buffer; defaults to a fresh `DoubleArray(sampleSize)`.
 */
public class CubicInterpolant(
    parameterPositions: DoubleArray,
    sampleValues: DoubleArray,
    sampleSize: Int,
    resultBuffer: DoubleArray = DoubleArray(sampleSize),
) : Interpolant(parameterPositions, sampleValues, sampleSize, resultBuffer) {

    private var _weightPrev: Double = -0.0
    private var _offsetPrev: Int = 0
    private var _weightNext: Double = -0.0
    private var _offsetNext: Int = 0

    override val DefaultSettings_: InterpolantSettings = InterpolantSettings(
        endingStart = InterpolantEnding.ZeroCurvature,
        endingEnd = InterpolantEnding.ZeroCurvature,
    )

    /**
     * Reads `parameterPositions[i]`, returning `null` for an out-of-bounds index
     * (mirrors JS `pp[i] === undefined`).
     */
    private fun ppGet(i: Int): Double? {
        val pp = parameterPositions
        return if (i < 0 || i >= pp.size) null else pp[i]
    }

    override fun intervalChanged_(i1: Int, t0: Double, t1: Double) {
        val pp = parameterPositions
        var iPrev = i1 - 2
        var iNext = i1 + 1

        var tPrev = ppGet(iPrev)
        var tNext = ppGet(iNext)

        if (tPrev == null) {
            when (getSettings_().endingStart) {
                InterpolantEnding.ZeroSlope -> {
                    // f'(t0) = 0
                    iPrev = i1
                    tPrev = 2 * t0 - t1
                }

                InterpolantEnding.WrapAround -> {
                    // use the other end of the curve
                    iPrev = pp.size - 2
                    tPrev = t0 + pp[iPrev] - pp[iPrev + 1]
                }

                InterpolantEnding.ZeroCurvature -> {
                    // f''(t0) = 0 a.k.a. Natural Spline
                    iPrev = i1
                    tPrev = t1
                }
            }
        }

        if (tNext == null) {
            when (getSettings_().endingEnd) {
                InterpolantEnding.ZeroSlope -> {
                    // f'(tN) = 0
                    iNext = i1
                    tNext = 2 * t1 - t0
                }

                InterpolantEnding.WrapAround -> {
                    // use the other end of the curve
                    iNext = 1
                    tNext = t1 + pp[1] - pp[0]
                }

                InterpolantEnding.ZeroCurvature -> {
                    // f''(tN) = 0, a.k.a. Natural Spline
                    iNext = i1 - 1
                    tNext = t0
                }
            }
        }

        val halfDt = (t1 - t0) * 0.5
        val stride = valueSize

        _weightPrev = halfDt / (t0 - tPrev!!)
        _weightNext = halfDt / (tNext!! - t1)
        _offsetPrev = iPrev * stride
        _offsetNext = iNext * stride
    }

    override fun interpolate_(i1: Int, t0: Double, t: Double, t1: Double): DoubleArray {
        val result = resultBuffer
        val values = sampleValues
        val stride = valueSize

        val o1 = i1 * stride
        val o0 = o1 - stride
        val oP = _offsetPrev
        val oN = _offsetNext
        val wP = _weightPrev
        val wN = _weightNext

        val p = (t - t0) / (t1 - t0)
        val pp = p * p
        val ppp = pp * p

        // evaluate polynomials

        val sP = -wP * ppp + 2 * wP * pp - wP * p
        val s0 = (1 + wP) * ppp + (-1.5 - 2 * wP) * pp + (-0.5 + wP) * p + 1
        val s1 = (-1 - wN) * ppp + (1.5 + wN) * pp + 0.5 * p
        val sN = wN * ppp - wN * pp

        // combine data linearly

        var i = 0
        while (i != stride) {
            result[i] =
                sP * values[oP + i] +
                s0 * values[o0 + i] +
                s1 * values[o1 + i] +
                sN * values[oN + i]
            ++i
        }

        return result
    }
}
