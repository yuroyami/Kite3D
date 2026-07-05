/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/interpolants/BezierInterpolant.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math.interpolants

import io.github.yuroyami.kite3d.math.Interpolant
import kotlin.math.abs

/**
 * A Bezier interpolant using cubic Bezier curves with 2D control points.
 *
 * This interpolant supports the COLLADA/Maya style of Bezier animation where
 * each keyframe has explicit in/out tangent control points specified as 2D
 * coordinates (time, value).
 *
 * The tangent data must be provided via the [settings] object:
 * - `settings.inTangents`: packed `[time, value]` pairs per keyframe per component.
 * - `settings.outTangents`: packed `[time, value]` pairs per keyframe per component.
 *
 * For a track with `N` keyframes and stride `S`:
 * - Each tangent array has `N * S * 2` values.
 * - Layout: `[k0_c0_time, k0_c0_value, k0_c1_time, k0_c1_value, ..., k0_cS_time,
 *   k0_cS_value, k1_c0_time, k1_c0_value, ...]`.
 *
 * When no tangent data is present, this falls back to linear interpolation.
 *
 * @param parameterPositions The parameter positions holding the interpolation factors.
 * @param sampleValues The sample values.
 * @param sampleSize The sample size (a.k.a. the value size / stride).
 * @param resultBuffer The result buffer; defaults to a fresh `DoubleArray(sampleSize)`.
 */
public class BezierInterpolant(
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

        val settings = getSettings_()
        val inTangents = settings.inTangents
        val outTangents = settings.outTangents

        // If no tangent data, fall back to linear interpolation
        if (inTangents == null || outTangents == null) {
            val weight1 = (t - t0) / (t1 - t0)
            val weight0 = 1 - weight1

            var i = 0
            while (i != stride) {
                result[i] = values[offset0 + i] * weight0 + values[offset1 + i] * weight1
                ++i
            }

            return result
        }

        val tangentStride = stride * 2
        val i0 = i1 - 1

        var i = 0
        while (i != stride) {
            val v0 = values[offset0 + i]
            val v1 = values[offset1 + i]

            // outTangent of previous keyframe (C0)
            val outTangentOffset = i0 * tangentStride + i * 2
            val c0x = outTangents[outTangentOffset]
            val c0y = outTangents[outTangentOffset + 1]

            // inTangent of current keyframe (C1)
            val inTangentOffset = i1 * tangentStride + i * 2
            val c1x = inTangents[inTangentOffset]
            val c1y = inTangents[inTangentOffset + 1]

            // Solve for Bezier parameter s where Bx(s) = t using Newton-Raphson
            var s = (t - t0) / (t1 - t0)
            var s2 = s * s
            var s3 = s2 * s
            var oneMinusS = 1 - s
            var oneMinusS2 = oneMinusS * oneMinusS
            var oneMinusS3 = oneMinusS2 * oneMinusS

            var iter = 0
            while (iter < 8) {
                s2 = s * s
                s3 = s2 * s
                oneMinusS = 1 - s
                oneMinusS2 = oneMinusS * oneMinusS
                oneMinusS3 = oneMinusS2 * oneMinusS

                // Bezier X(s) = (1-s)³·t0 + 3(1-s)²s·c0x + 3(1-s)s²·c1x + s³·t1
                val bx = oneMinusS3 * t0 + 3 * oneMinusS2 * s * c0x + 3 * oneMinusS * s2 * c1x + s3 * t1

                val error = bx - t
                if (abs(error) < 1e-10) break

                // Derivative dX/ds
                val dbx = 3 * oneMinusS2 * (c0x - t0) + 6 * oneMinusS * s * (c1x - c0x) + 3 * s2 * (t1 - c1x)
                if (abs(dbx) < 1e-10) break

                s -= error / dbx
                s = maxOf(0.0, minOf(1.0, s))
                ++iter
            }

            // Evaluate Bezier Y(s)
            result[i] = oneMinusS3 * v0 + 3 * oneMinusS2 * s * c0y + 3 * oneMinusS * s2 * c1y + s3 * v1
            ++i
        }

        return result
    }
}
