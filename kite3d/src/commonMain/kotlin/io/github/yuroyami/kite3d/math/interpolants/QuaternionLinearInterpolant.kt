/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/interpolants/QuaternionLinearInterpolant.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math.interpolants

import io.github.yuroyami.kite3d.math.Interpolant
import io.github.yuroyami.kite3d.math.Quaternion

/**
 * Spherical linear unit quaternion interpolant.
 *
 * @param parameterPositions The parameter positions holding the interpolation factors.
 * @param sampleValues The sample values.
 * @param sampleSize The sample size (a.k.a. the value size / stride).
 * @param resultBuffer The result buffer; defaults to a fresh `DoubleArray(sampleSize)`.
 */
public class QuaternionLinearInterpolant(
    parameterPositions: DoubleArray,
    sampleValues: DoubleArray,
    sampleSize: Int,
    resultBuffer: DoubleArray = DoubleArray(sampleSize),
) : Interpolant(parameterPositions, sampleValues, sampleSize, resultBuffer) {

    override fun interpolate_(i1: Int, t0: Double, t: Double, t1: Double): DoubleArray {
        val result = resultBuffer
        val values = sampleValues
        val stride = valueSize

        val alpha = (t - t0) / (t1 - t0)

        var offset = i1 * stride

        val end = offset + stride
        while (offset != end) {
            Quaternion.slerpFlat(result, 0, values, offset - stride, values, offset, alpha)
            offset += 4
        }

        return result
    }
}
