/*
 * Copyright © 2026 yuroyami — MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/MathUtils.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MathUtilsTest {

    @Test
    fun generateUUID() {
        val a = MathUtils.generateUUID()
        val regex = Regex("[A-Za-z0-9]{8}-[A-Za-z0-9]{4}-4[A-Za-z0-9]{3}-[A-Za-z0-9]{4}-[A-Za-z0-9]{12}")
        assertTrue(regex.matches(a), "Generated UUID matches the expected pattern: $a")
    }

    @Test
    fun clamp() {
        assertEquals(0.5, MathUtils.clamp(0.5, 0.0, 1.0), 0.0, "Value already within limits")
        assertEquals(0.0, MathUtils.clamp(0.0, 0.0, 1.0), 0.0, "Value equal to one limit")
        assertEquals(0.0, MathUtils.clamp(-0.1, 0.0, 1.0), 0.0, "Value too low")
        assertEquals(1.0, MathUtils.clamp(1.1, 0.0, 1.0), 0.0, "Value too high")
    }

    @Test
    fun euclideanModulo() {
        assertTrue(MathUtils.euclideanModulo(6.0, 0.0).isNaN(), "Division by zero returns NaN")
        assertEquals(0.0, MathUtils.euclideanModulo(6.0, 1.0), 0.0, "Division by trivial divisor")
        assertEquals(0.0, MathUtils.euclideanModulo(6.0, 2.0), 0.0, "Division by non-trivial divisor")
        assertEquals(1.0, MathUtils.euclideanModulo(6.0, 5.0), 0.0, "Division by itself - 1")
        assertEquals(0.0, MathUtils.euclideanModulo(6.0, 6.0), 0.0, "Division by itself")
        assertEquals(6.0, MathUtils.euclideanModulo(6.0, 7.0), 0.0, "Division by itself + 1")
    }

    @Test
    fun mapLinear() {
        assertEquals(5.0, MathUtils.mapLinear(0.5, 0.0, 1.0, 0.0, 10.0), 0.0, "Value within range")
        assertEquals(0.0, MathUtils.mapLinear(0.0, 0.0, 1.0, 0.0, 10.0), 0.0, "Value at lower boundary")
        assertEquals(10.0, MathUtils.mapLinear(1.0, 0.0, 1.0, 0.0, 10.0), 0.0, "Value at upper boundary")
    }

    @Test
    fun inverseLerp() {
        assertEquals(0.5, MathUtils.inverseLerp(1.0, 2.0, 1.5), 0.0, "50%")
        assertEquals(1.0, MathUtils.inverseLerp(1.0, 2.0, 2.0), 0.0, "100%")
        assertEquals(0.0, MathUtils.inverseLerp(1.0, 2.0, 1.0), 0.0, "0%")
        assertEquals(0.0, MathUtils.inverseLerp(1.0, 1.0, 1.0), 0.0, "0%, no NaN")
    }

    @Test
    fun lerp() {
        assertEquals(1.0, MathUtils.lerp(1.0, 2.0, 0.0), 0.0, "Lower boundary")
        assertEquals(2.0, MathUtils.lerp(1.0, 2.0, 1.0), 0.0, "Upper boundary")
        assertEquals(1.4, MathUtils.lerp(1.0, 2.0, 0.4), 0.0, "Within range")
    }

    @Test
    fun damp() {
        assertEquals(1.0, MathUtils.damp(1.0, 2.0, 0.0, 0.016), 0.0, "Lower boundary")
        // exp() is not bit-identical across V8/JVM/Native libm — assert with tolerance.
        assertEquals(1.1478562110337887, MathUtils.damp(1.0, 2.0, 10.0, 0.016), 1e-12, "Within range")
    }

    @Test
    fun pingpong() {
        assertEquals(0.5, MathUtils.pingpong(2.5), 0.0, "2.5 -> 0.5")
        assertEquals(1.5, MathUtils.pingpong(2.5, 2.0), 0.0, "2.5 with length 2 -> 1.5")
        assertEquals(0.5, MathUtils.pingpong(-1.5), 0.0, "-1.5 -> 0.5")
    }

    @Test
    fun smoothstep() {
        assertEquals(0.0, MathUtils.smoothstep(-1.0, 0.0, 2.0), 0.0, "Below min")
        assertEquals(0.0, MathUtils.smoothstep(0.0, 0.0, 2.0), 0.0, "At min")
        assertEquals(0.15625, MathUtils.smoothstep(0.5, 0.0, 2.0), 0.0, "Within limits")
        assertEquals(0.5, MathUtils.smoothstep(1.0, 0.0, 2.0), 0.0, "Within limits")
        assertEquals(0.84375, MathUtils.smoothstep(1.5, 0.0, 2.0), 0.0, "Within limits")
        assertEquals(1.0, MathUtils.smoothstep(2.0, 0.0, 2.0), 0.0, "At max")
        assertEquals(1.0, MathUtils.smoothstep(3.0, 0.0, 2.0), 0.0, "Above max")
    }

    @Test
    fun smootherstep() {
        assertEquals(0.0, MathUtils.smootherstep(-1.0, 0.0, 2.0), 0.0, "Below min")
        assertEquals(0.0, MathUtils.smootherstep(0.0, 0.0, 2.0), 0.0, "At min")
        assertEquals(0.103515625, MathUtils.smootherstep(0.5, 0.0, 2.0), 0.0, "Within limits")
        assertEquals(0.5, MathUtils.smootherstep(1.0, 0.0, 2.0), 0.0, "Within limits")
        assertEquals(0.896484375, MathUtils.smootherstep(1.5, 0.0, 2.0), 0.0, "Within limits")
        assertEquals(1.0, MathUtils.smootherstep(2.0, 0.0, 2.0), 0.0, "At max")
        assertEquals(1.0, MathUtils.smootherstep(3.0, 0.0, 2.0), 0.0, "Above max")
    }

    @Test
    fun randInt() {
        val low = 1
        val high = 3
        val a = MathUtils.randInt(low, high)
        assertTrue(a >= low, "At or above lower limit")
        assertTrue(a <= high, "At or below upper limit")
    }

    @Test
    fun randFloat() {
        val low = 1.0
        val high = 3.0
        val a = MathUtils.randFloat(low, high)
        assertTrue(a >= low, "At or above lower limit")
        assertTrue(a <= high, "At or below upper limit")
    }

    @Test
    fun randFloatSpread() {
        val a = MathUtils.randFloatSpread(3.0)
        assertTrue(a > -3.0 / 2.0, "Above lower limit")
        assertTrue(a < 3.0 / 2.0, "Below upper limit")
    }

    @Test
    fun degToRad() {
        assertEquals(0.0, MathUtils.degToRad(0.0), 0.0, "0 degrees")
        assertEquals(PI / 2, MathUtils.degToRad(90.0), 0.0, "90 degrees")
        assertEquals(PI, MathUtils.degToRad(180.0), 0.0, "180 degrees")
        assertEquals(PI * 2, MathUtils.degToRad(360.0), 0.0, "360 degrees")
    }

    @Test
    fun radToDeg() {
        assertEquals(0.0, MathUtils.radToDeg(0.0), 0.0, "0 radians")
        assertEquals(90.0, MathUtils.radToDeg(PI / 2), 0.0, "PI/2 radians")
        assertEquals(180.0, MathUtils.radToDeg(PI), 0.0, "PI radians")
        assertEquals(360.0, MathUtils.radToDeg(PI * 2), 0.0, "PI*2 radians")
    }

    @Test
    fun isPowerOfTwo() {
        assertFalse(MathUtils.isPowerOfTwo(0), "0 is not a PoT")
        assertTrue(MathUtils.isPowerOfTwo(1), "1 is a PoT")
        assertTrue(MathUtils.isPowerOfTwo(2), "2 is a PoT")
        assertFalse(MathUtils.isPowerOfTwo(3), "3 is not a PoT")
        assertTrue(MathUtils.isPowerOfTwo(4), "4 is a PoT")
    }

    @Test
    fun ceilPowerOfTwo() {
        assertEquals(1.0, MathUtils.ceilPowerOfTwo(1.0), 0.0, "ceil PoT of 1 is 1")
        assertEquals(4.0, MathUtils.ceilPowerOfTwo(3.0), 0.0, "ceil PoT of 3 is 4")
        assertEquals(4.0, MathUtils.ceilPowerOfTwo(4.0), 0.0, "ceil PoT of 4 is 4")
    }

    @Test
    fun floorPowerOfTwo() {
        assertEquals(1.0, MathUtils.floorPowerOfTwo(1.0), 0.0, "floor PoT of 1 is 1")
        assertEquals(2.0, MathUtils.floorPowerOfTwo(3.0), 0.0, "floor PoT of 3 is 2")
        assertEquals(4.0, MathUtils.floorPowerOfTwo(4.0), 0.0, "floor PoT of 4 is 4")
    }
}
