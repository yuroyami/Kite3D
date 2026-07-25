/*
 * Copyright © 2026 yuroyami. MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/interpolants/CustomInterpolant.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math.interpolants

import io.github.yuroyami.kite3d.math.Interpolant
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

/**
 * A custom cubic spline interpolant mimicking `GLTFCubicSplineInterpolant` from
 * `GLTFLoader`. The keyframe layout for CUBICSPLINE animations is:
 * `[ inTangent_1, splineVertex_1, outTangent_1, inTangent_2, splineVertex_2, … ]`
 *
 * This exists to prove the [Interpolant] extension seam: a derived class must be
 * able to override [Interpolant.copySampleValue_] as well as `interpolate_`.
 */
private class CubicSplineInterpolant(
    parameterPositions: DoubleArray,
    sampleValues: DoubleArray,
    sampleSize: Int,
    resultBuffer: DoubleArray,
) : Interpolant(parameterPositions, sampleValues, sampleSize, resultBuffer) {

    override fun copySampleValue_(index: Int): DoubleArray {
        val result = resultBuffer
        val values = sampleValues
        val stride = valueSize
        val offset = index * stride * 3 + stride

        var i = 0
        while (i != stride) {
            result[i] = values[offset + i]
            ++i
        }

        return result
    }

    override fun interpolate_(i1: Int, t0: Double, t: Double, t1: Double): DoubleArray {
        val result = resultBuffer
        val values = sampleValues
        val stride = valueSize

        val stride2 = stride * 2
        val stride3 = stride * 3

        val td = t1 - t0

        val p = (t - t0) / td
        val pp = p * p
        val ppp = pp * p

        val offset1 = i1 * stride3
        val offset0 = offset1 - stride3

        val s2 = -2.0 * ppp + 3.0 * pp
        val s3 = ppp - pp
        val s0 = 1.0 - s2
        val s1 = s3 - pp + p

        var i = 0
        while (i != stride) {
            val p0 = values[offset0 + i + stride]
            val m0 = values[offset0 + i + stride2] * td
            val p1 = values[offset1 + i + stride]
            val m1 = values[offset1 + i] * td

            result[i] = s0 * p0 + s1 * m0 + s2 * p1 + s3 * m1
            ++i
        }

        return result
    }
}

class CustomInterpolantTest {

    // INHERITANCE
    @Test
    fun extending() {
        // parameterPositions, sampleValues, sampleSize, resultBuffer
        val obj = CubicSplineInterpolant(
            doubleArrayOf(0.0, 1.0),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            1,
            DoubleArray(1),
        )
        assertTrue(obj is Interpolant, "CubicSplineInterpolant extends from Interpolant")
    }

    // PUBLIC
    @Test
    fun evaluate() {
        // Two keyframes at t = 0 and t = 1, valueSize = 1.
        // Layout: [ in_0, v_0, out_0, in_1, v_1, out_1 ]
        // Vertex values 0 -> 1 with non-zero tangents to exercise all spline terms.
        val positions = doubleArrayOf(0.0, 1.0)
        val values = doubleArrayOf(0.0, 0.0, 1.0, -1.0, 1.0, 0.0)
        val interpolant = CubicSplineInterpolant(positions, values, 1, DoubleArray(1))

        assertContentEquals(doubleArrayOf(0.0), interpolant.evaluate(0.0), "evaluate at first keyframe")
        assertContentEquals(doubleArrayOf(1.0), interpolant.evaluate(1.0), "evaluate at last keyframe")

        // At t = 0.5 with td = 1, p = 0.5 → s0 = 0.5, s1 = 0.125, s2 = 0.5, s3 = -0.125
        // result = 0.5 * 0 + 0.125 * 1 + 0.5 * 1 + ( -0.125 ) * ( -1 ) = 0.75
        assertContentEquals(doubleArrayOf(0.75), interpolant.evaluate(0.5), "evaluate inside interval")

        // Out-of-range queries clamp to the boundary spline vertex.
        assertContentEquals(doubleArrayOf(0.0), interpolant.evaluate(-1.0), "evaluate before first keyframe")
        assertContentEquals(doubleArrayOf(1.0), interpolant.evaluate(2.0), "evaluate after last keyframe")
    }
}
