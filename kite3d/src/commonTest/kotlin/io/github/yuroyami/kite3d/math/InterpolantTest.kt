/*
 * Copyright © 2026 yuroyami — MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Interpolant.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InterpolantTest {

    // Since this is an abstract base class, we have to make it concrete in order
    // to test its functionality...
    //
    // The JS test tacks `intervalChanged_`/`interpolate_` onto Mock.prototype and
    // uses a static `Mock.calls` array as a capture facility. Here Mock captures
    // into an instance list; a `null` list means "don't record" (mirrors the JS
    // `Mock.calls !== null` guard).
    private class Call(val func: String, val args: List<Double>)

    private class Mock(
        parameterPositions: DoubleArray,
        sampleValues: DoubleArray,
        sampleSize: Int,
        resultBuffer: DoubleArray = DoubleArray(sampleSize),
    ) : Interpolant(parameterPositions, sampleValues, sampleSize, resultBuffer) {

        var calls: MutableList<Call>? = null

        override fun intervalChanged_(i1: Int, t0: Double, t1: Double) {
            calls?.add(Call("intervalChanged", listOf(i1.toDouble(), t0, t1)))
        }

        override fun interpolate_(i1: Int, t0: Double, t: Double, t1: Double): DoubleArray {
            calls?.add(Call("interpolate", listOf(i1.toDouble(), t0, t, t1)))
            return copySampleValue_(i1 - 1)
        }
    }

    // INSTANCING
    @Test
    fun instancing() {
        // JS passes `null` parameterPositions; the pinned Kotlin ctor is
        // non-nullable, so pass an empty array (construction never reads it here).
        val interpolant = Mock(DoubleArray(0), doubleArrayOf(1.0, 11.0, 2.0, 22.0, 3.0, 33.0), 2, DoubleArray(0))
        // JS: assert interpolant instanceof Interpolant === true
        assertTrue(interpolant is Interpolant, "Mock extends from Interpolant")
    }

    // PRIVATE
    @Test
    fun copySampleValue_() {
        val interpolant = Mock(DoubleArray(0), doubleArrayOf(1.0, 11.0, 2.0, 22.0, 3.0, 33.0), 2, DoubleArray(2))

        assertContentEquals2(doubleArrayOf(1.0, 11.0), interpolant.copySampleValue_(0), "sample fetch (0)")
        assertContentEquals2(doubleArrayOf(2.0, 22.0), interpolant.copySampleValue_(1), "sample fetch (1)")
        assertContentEquals2(doubleArrayOf(3.0, 33.0), interpolant.copySampleValue_(2), "first sample (2)")
    }

    @Test
    fun evaluateIntervalChangedInterpolate() {
        // parameterPositions = [11..99], sampleValues = null (never read: sampleSize 0)
        val interpolant = Mock(doubleArrayOf(11.0, 22.0, 33.0, 44.0, 55.0, 66.0, 77.0, 88.0, 99.0), DoubleArray(0), 0, DoubleArray(0))

        interpolant.calls = mutableListOf()
        interpolant.evaluate(11.0)

        assertCall(interpolant, 0, "intervalChanged", listOf(1.0, 11.0, 22.0))
        assertCall(interpolant, 1, "interpolate", listOf(1.0, 11.0, 11.0, 22.0))
        assertTrue(interpolant.calls!!.size == 2, "no further calls")

        interpolant.calls = mutableListOf()
        interpolant.evaluate(12.0) // same interval
        assertCall(interpolant, 0, "interpolate", listOf(1.0, 11.0, 12.0, 22.0))
        assertTrue(interpolant.calls!!.size == 1, "no further calls")

        interpolant.calls = mutableListOf()
        interpolant.evaluate(22.0) // step forward
        assertCall(interpolant, 0, "intervalChanged", listOf(2.0, 22.0, 33.0))
        assertCall(interpolant, 1, "interpolate", listOf(2.0, 22.0, 22.0, 33.0))
        assertTrue(interpolant.calls!!.size == 2)

        interpolant.calls = mutableListOf()
        interpolant.evaluate(21.0) // step back
        assertCall(interpolant, 0, "intervalChanged", listOf(1.0, 11.0, 22.0))
        assertCall(interpolant, 1, "interpolate", listOf(1.0, 11.0, 21.0, 22.0))
        assertTrue(interpolant.calls!!.size == 2, "no further calls")

        interpolant.calls = mutableListOf()
        interpolant.evaluate(20.0) // same interval
        assertCall(interpolant, 0, "interpolate", listOf(1.0, 11.0, 20.0, 22.0))
        assertTrue(interpolant.calls!!.size == 1, "no further calls")

        interpolant.calls = mutableListOf()
        interpolant.evaluate(43.0) // two steps forward
        assertCall(interpolant, 0, "intervalChanged", listOf(3.0, 33.0, 44.0))
        assertCall(interpolant, 1, "interpolate", listOf(3.0, 33.0, 43.0, 44.0))
        assertTrue(interpolant.calls!!.size == 2, "no further calls")

        interpolant.calls = mutableListOf()
        interpolant.evaluate(12.0) // two steps back
        assertCall(interpolant, 0, "intervalChanged", listOf(1.0, 11.0, 22.0))
        assertCall(interpolant, 1, "interpolate", listOf(1.0, 11.0, 12.0, 22.0))
        assertTrue(interpolant.calls!!.size == 2, "no further calls")

        interpolant.calls = mutableListOf()
        interpolant.evaluate(77.0) // random access
        assertCall(interpolant, 0, "intervalChanged", listOf(7.0, 77.0, 88.0))
        assertCall(interpolant, 1, "interpolate", listOf(7.0, 77.0, 77.0, 88.0))
        assertTrue(interpolant.calls!!.size == 2, "no further calls")

        interpolant.calls = mutableListOf()
        interpolant.evaluate(80.0) // same interval
        assertCall(interpolant, 0, "interpolate", listOf(7.0, 77.0, 80.0, 88.0))
        assertTrue(interpolant.calls!!.size == 1, "no further calls")

        interpolant.calls = mutableListOf()
        interpolant.evaluate(36.0) // random access
        assertCall(interpolant, 0, "intervalChanged", listOf(3.0, 33.0, 44.0))
        assertCall(interpolant, 1, "interpolate", listOf(3.0, 33.0, 36.0, 44.0))
        assertTrue(interpolant.calls!!.size == 2, "no further calls")

        interpolant.calls = mutableListOf()
        interpolant.evaluate(24.0) // fast reset / loop (2nd)
        assertCall(interpolant, 0, "intervalChanged", listOf(2.0, 22.0, 33.0))
        assertCall(interpolant, 1, "interpolate", listOf(2.0, 22.0, 24.0, 33.0))
        assertTrue(interpolant.calls!!.size == 2, "no further calls")

        interpolant.calls = mutableListOf()
        interpolant.evaluate(16.0) // fast reset / loop (2nd)
        assertCall(interpolant, 0, "intervalChanged", listOf(1.0, 11.0, 22.0))
        assertCall(interpolant, 1, "interpolate", listOf(1.0, 11.0, 16.0, 22.0))
        assertTrue(interpolant.calls!!.size == 2, "no further calls")
    }

    // --- helpers ---

    private fun assertContentEquals2(expected: DoubleArray, actual: DoubleArray, message: String) {
        assertEquals(expected.size, actual.size, "$message: size")
        for (i in expected.indices) {
            assertEquals(expected[i], actual[i], 0.0, "$message: [$i]")
        }
    }

    private fun assertCall(mock: Mock, index: Int, func: String, args: List<Double>) {
        val call = mock.calls!![index]
        assertEquals(func, call.func, "call[$index].func")
        assertEquals(args.size, call.args.size, "call[$index].args.size")
        for (i in args.indices) {
            assertEquals(args[i], call.args[i], 0.0, "call[$index].args[$i]")
        }
    }
}
