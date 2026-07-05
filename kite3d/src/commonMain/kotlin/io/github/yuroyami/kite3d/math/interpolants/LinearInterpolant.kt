/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/interpolants/LinearInterpolant.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math.interpolants

import io.github.yuroyami.kite3d.math.Interpolant

/**
 * A basic linear interpolant.
 *
 * @param parameterPositions The parameter positions holding the interpolation factors.
 * @param sampleValues The sample values.
 * @param sampleSize The sample size (a.k.a. the value size / stride).
 * @param resultBuffer The result buffer; defaults to a fresh `DoubleArray(sampleSize)`.
 */
public class LinearInterpolant(
    parameterPositions: DoubleArray,
    sampleValues: DoubleArray,
    sampleSize: Int,
    resultBuffer: DoubleArray = DoubleArray(sampleSize),
) : Interpolant(parameterPositions, sampleValues, sampleSize, resultBuffer) {

    override fun interpolate_(i1: Int, t0: Double, t: Double, t1: Double): DoubleArray {
        val result = resultBuffer
        val values = sampleValues
        val stride = valueSize

        val offset1 = i1 * stride
        val offset0 = offset1 - stride

        val weight1 = (t - t0) / (t1 - t0)
        val weight0 = 1 - weight1

        var i = 0
        while (i != stride) {
            result[i] =
                values[offset0 + i] * weight0 +
                values[offset1 + i] * weight1
            ++i
        }

        return result
    }
}
