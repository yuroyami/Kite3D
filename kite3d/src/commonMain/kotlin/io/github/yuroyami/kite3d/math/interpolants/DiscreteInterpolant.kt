/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/interpolants/DiscreteInterpolant.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math.interpolants

import io.github.yuroyami.kite3d.math.Interpolant

/**
 * Interpolant that evaluates to the sample value at the position preceding the
 * parameter.
 *
 * @param parameterPositions The parameter positions holding the interpolation factors.
 * @param sampleValues The sample values.
 * @param sampleSize The sample size (a.k.a. the value size / stride).
 * @param resultBuffer The result buffer; defaults to a fresh `DoubleArray(sampleSize)`.
 */
public class DiscreteInterpolant(
    parameterPositions: DoubleArray,
    sampleValues: DoubleArray,
    sampleSize: Int,
    resultBuffer: DoubleArray = DoubleArray(sampleSize),
) : Interpolant(parameterPositions, sampleValues, sampleSize, resultBuffer) {

    override fun interpolate_(i1: Int, t0: Double, t: Double, t1: Double): DoubleArray {
        return copySampleValue_(i1 - 1)
    }
}
