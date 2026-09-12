/*
 * Copyright © 2026 yuroyami. MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Color.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.abs
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ColorTest {

    // ColorManagement is global. Each test sets `enabled` itself, and this sets it back
    // to the default (true) after each test, so tests cannot affect each other.
    @AfterTest
    fun restore() {
        ColorManagement.enabled = true
    }

    // INSTANCING
    @Test
    fun instancing() {
        ColorManagement.enabled = false // three.js turns it off here too

        // default ctor
        var c = Color()
        assertTrue(c.r != 0.0, "Red: ${c.r}")
        assertTrue(c.g != 0.0, "Green: ${c.g}")
        assertTrue(c.b != 0.0, "Blue: ${c.b}")

        // rgb ctor
        c = Color(1.0, 1.0, 1.0)
        assertTrue(c.r == 1.0, "Passed")
        assertTrue(c.g == 1.0, "Passed")
        assertTrue(c.b == 1.0, "Passed")
    }

    // No Color.NAMES test: Kite3D has no named-color table.

    // No isColor test: Kotlin checks the type with `is Color`.

    @Test
    fun set() {
        ColorManagement.enabled = false

        val a = Color()
        val b = Color(0.5, 0.0, 0.0)
        val c = Color(0xFF0000)
        val e = Color(0.5, 0.5, 0.5)

        a.set(b)
        assertTrue(a == b, "Set with Color instance")

        a.set(0xFF0000)
        assertTrue(a == c, "Set with number")

        // three.js also checks `a.set('rgb(0,255,0)')`. Kite3D does not parse CSS strings.

        a.set(0.5, 0.5, 0.5)
        assertTrue(a == e, "Set with r,g,b components")
    }

    @Test
    fun setScalar() {
        ColorManagement.enabled = false

        val c = Color()
        c.setScalar(0.5)
        assertTrue(c.r == 0.5, "Red: ${c.r}")
        assertTrue(c.g == 0.5, "Green: ${c.g}")
        assertTrue(c.b == 0.5, "Blue: ${c.b}")
    }

    @Test
    fun setHex() {
        ColorManagement.enabled = false

        val c = Color()
        c.setHex(0xFA8072)
        assertTrue(c.getHex() == 0xFA8072, "Hex: ${c.getHex()}")
        assertTrue(c.r == 0xFA / 0xFF.toDouble(), "Red: ${c.r}")
        assertTrue(c.g == 0x80 / 0xFF.toDouble(), "Green: ${c.g}")
        assertTrue(c.b == 0x72 / 0xFF.toDouble(), "Blue: ${c.b}")
    }

    @Test
    fun setRGB() {
        ColorManagement.enabled = true

        val c = Color()

        c.setRGB(0.3, 0.5, 0.7)

        assertEquals(0.3, c.r, 0.0, "Red (srgb-linear)")
        assertEquals(0.5, c.g, 0.0, "Green (srgb-linear)")
        assertEquals(0.7, c.b, 0.0, "Blue (srgb-linear)")

        c.setRGB(0.3, 0.5, 0.7, ColorSpace.SRGB)

        // srgb → working (linear) conversion (transcendental → tolerance).
        assertTrue(abs(c.r - 0.073) <= 1e-3, "Red (srgb): ${c.r}")
        assertTrue(abs(c.g - 0.214) <= 1e-3, "Green (srgb): ${c.g}")
        assertTrue(abs(c.b - 0.448) <= 1e-3, "Blue (srgb): ${c.b}")
    }

    @Test
    fun setHSL() {
        ColorManagement.enabled = false

        val c = Color()
        val hsl = HSL()
        c.setHSL(0.75, 1.0, 0.25)
        c.getHSL(hsl)

        assertTrue(hsl.h == 0.75, "hue: ${hsl.h}")
        assertTrue(hsl.s == 1.00, "saturation: ${hsl.s}")
        assertTrue(hsl.l == 0.25, "lightness: ${hsl.l}")
    }

    // No setStyle or setColorName tests: Kite3D does not parse CSS strings or color names.

    @Test
    fun clone() {
        ColorManagement.enabled = false
        // three.js uses Color('teal'). Kite3D has no color names, so this uses 0x008080.
        val c = Color(0x008080)
        val c2 = c.clone()
        assertTrue(c2.getHex() == 0x008080, "Hex c2: ${c2.getHex()}")
    }

    @Test
    fun copy() {
        ColorManagement.enabled = false

        // three.js uses Color('teal'); this uses its hex value, 0x008080.
        val a = Color(0x008080)
        val b = Color()
        b.copy(a)
        assertTrue(b.r == 0x00 / 255.0, "Red: ${b.r}")
        assertTrue(b.g == 0x80 / 255.0, "Green: ${b.g}")
        assertTrue(b.b == 0x80 / 255.0, "Blue: ${b.b}")
    }

    @Test
    fun copySRGBToLinear() {
        ColorManagement.enabled = false

        val c = Color()
        val c2 = Color()
        c2.setRGB(0.3, 0.5, 0.9)
        c.copySRGBToLinear(c2)
        // three.js checks rough x^2 values with a tolerance of 0.1. The exact sRGB curve
        // differs from x^2 by up to about 0.03, so the same tolerance works here.
        assertTrue(abs(c.r - 0.09) < 0.1, "Red c: ${c.r} Red c2: ${c2.r}")
        assertTrue(abs(c.g - 0.25) < 0.1, "Green c: ${c.g} Green c2: ${c2.g}")
        assertTrue(abs(c.b - 0.81) < 0.1, "Blue c: ${c.b} Blue c2: ${c2.b}")
    }

    @Test
    fun copyLinearToSRGB() {
        ColorManagement.enabled = false

        val c = Color()
        val c2 = Color()
        c2.setRGB(0.09, 0.25, 0.81)
        c.copyLinearToSRGB(c2)
        // Tolerance 0.1, as in three.js. The exact curve differs from these rough values
        // by up to about 0.03.
        assertTrue(abs(c.r - 0.3) < 0.1, "Red c: ${c.r} Red c2: ${c2.r}")
        assertTrue(abs(c.g - 0.5) < 0.1, "Green c: ${c.g} Green c2: ${c2.g}")
        assertTrue(abs(c.b - 0.9) < 0.1, "Blue c: ${c.b} Blue c2: ${c2.b}")
    }

    @Test
    fun convertSRGBToLinear() {
        ColorManagement.enabled = false

        val c = Color()
        c.setRGB(0.3, 0.5, 0.9)
        c.convertSRGBToLinear()
        // Tolerance 0.1, as in three.js.
        assertTrue(abs(c.r - 0.09) < 0.1, "Red: ${c.r}")
        assertTrue(abs(c.g - 0.25) < 0.1, "Green: ${c.g}")
        assertTrue(abs(c.b - 0.81) < 0.1, "Blue: ${c.b}")
    }

    @Test
    fun convertLinearToSRGB() {
        ColorManagement.enabled = false

        val c = Color()
        c.setRGB(4.0, 9.0, 16.0)
        c.convertLinearToSRGB()
        // Tolerance 0.1, as in three.js.
        assertTrue(abs(c.r - 1.82) < 0.1, "Red: ${c.r}")
        assertTrue(abs(c.g - 2.58) < 0.1, "Green: ${c.g}")
        assertTrue(abs(c.b - 3.29) < 0.1, "Blue: ${c.b}")
    }

    @Test
    fun getHex() {
        ColorManagement.enabled = false

        // three.js uses Color('red'); this uses its hex value, 0xFF0000.
        val c = Color(0xFF0000)
        val res = c.getHex()
        assertTrue(res == 0xFF0000, "Hex: $res")
    }

    @Test
    fun getHexString() {
        ColorManagement.enabled = false

        // three.js uses Color('tomato'); this uses its hex value, 0xFF6347.
        val c = Color(0xFF6347)
        val res = c.getHexString()
        assertTrue(res == "ff6347", "Hex: $res")
    }

    @Test
    fun getHSL() {
        ColorManagement.enabled = false

        val c = Color(0x80ffff)
        val hsl = HSL()
        c.getHSL(hsl)

        assertTrue(hsl.h == 0.5, "hue: ${hsl.h}")
        assertTrue(hsl.s == 1.0, "saturation: ${hsl.s}")
        assertTrue((MathUtils.jsRound(hsl.l * 100) / 100) == 0.75, "lightness: ${hsl.l}")
    }

    @Test
    fun getRGB() {
        ColorManagement.enabled = true

        // three.js uses Color('plum'); this uses its hex value, 0xDDA0DD.
        val c = Color(0xDDA0DD)
        val t = Color(0.0, 0.0, 0.0)

        c.getRGB(t)

        assertTrue(abs(t.r - 0.723) <= 1e-3, "r (srgb-linear): ${t.r}")
        assertTrue(abs(t.g - 0.352) <= 1e-3, "g (srgb-linear): ${t.g}")
        assertTrue(abs(t.b - 0.723) <= 1e-3, "b (srgb-linear): ${t.b}")

        c.getRGB(t, ColorSpace.SRGB)

        assertTrue(abs(t.r - 221.0 / 255.0) <= 1e-3, "r (srgb): ${t.r}")
        assertTrue(abs(t.g - 160.0 / 255.0) <= 1e-3, "g (srgb): ${t.g}")
        assertTrue(abs(t.b - 221.0 / 255.0) <= 1e-3, "b (srgb): ${t.b}")
    }

    // No getStyle test: Kite3D does not output CSS strings.

    @Test
    fun offsetHSL() {
        ColorManagement.enabled = false

        // three.js uses Color('hsl(120,50%,50%)'). Kite3D has no CSS parsing, so this uses setHSL.
        val a = Color().setHSL(120.0 / 360.0, 0.5, 0.5)
        val b = Color(0.36, 0.84, 0.648)

        a.offsetHSL(0.1, 0.1, 0.1)

        assertTrue(abs(a.r - b.r) <= eps, "Check r")
        assertTrue(abs(a.g - b.g) <= eps, "Check g")
        assertTrue(abs(a.b - b.b) <= eps, "Check b")
    }

    @Test
    fun add() {
        ColorManagement.enabled = false

        val a = Color(0x0000FF)
        val b = Color(0xFF0000)
        val c = Color(0xFF00FF)

        a.add(b)

        assertTrue(a == c, "Check new value")
    }

    @Test
    fun addColors() {
        ColorManagement.enabled = false

        val a = Color(0x0000FF)
        val b = Color(0xFF0000)
        val c = Color(0xFF00FF)
        val d = Color()

        d.addColors(a, b)

        assertTrue(d == c, "Passed")
    }

    @Test
    fun addScalar() {
        ColorManagement.enabled = false

        val a = Color(0.1, 0.0, 0.0)
        val b = Color(0.6, 0.5, 0.5)

        a.addScalar(0.5)

        assertTrue(a == b, "Check new value")
    }

    @Test
    fun sub() {
        ColorManagement.enabled = false

        val a = Color(0x0000CC)
        val b = Color(0xFF0000)
        val c = Color(0x0000AA)

        a.sub(b)
        assertEquals(0xCC, a.getHex(), "Difference too large")

        a.sub(c)
        assertEquals(0x22, a.getHex(), "Difference fine")
    }

    @Test
    fun multiply() {
        ColorManagement.enabled = false

        val a = Color(1.0, 0.0, 0.5)
        val b = Color(0.5, 1.0, 0.5)
        val c = Color(0.5, 0.0, 0.25)

        a.multiply(b)
        assertTrue(a == c, "Check new value")
    }

    @Test
    fun multiplyScalar() {
        ColorManagement.enabled = false

        val a = Color(0.25, 0.0, 0.5)
        val b = Color(0.5, 0.0, 1.0)

        a.multiplyScalar(2.0)
        assertTrue(a == b, "Check new value")
    }

    @Test
    fun lerp() {
        ColorManagement.enabled = false

        val c = Color()
        val c2 = Color()
        c.setRGB(0.0, 0.0, 0.0)
        c.lerp(c2, 0.2)
        assertTrue(c.r == 0.2, "Red: ${c.r}")
        assertTrue(c.g == 0.2, "Green: ${c.g}")
        assertTrue(c.b == 0.2, "Blue: ${c.b}")
    }

    @Test
    fun equalsTest() {
        ColorManagement.enabled = false

        val a = Color(0.5, 0.0, 1.0)
        val b = Color(0.5, 1.0, 0.0)

        assertEquals(a.r, b.r, 0.0, "Components: r is equal")
        assertTrue(a.g != b.g, "Components: g is not equal")
        assertTrue(a.b != b.b, "Components: b is not equal")

        assertFalse(a == b, "equals(): a not equal b")
        assertFalse(b == a, "equals(): b not equal a")

        a.copy(b)
        assertEquals(a.r, b.r, 0.0, "Components after copy(): r is equal")
        assertEquals(a.g, b.g, 0.0, "Components after copy(): g is equal")
        assertEquals(a.b, b.b, 0.0, "Components after copy(): b is equal")

        assertTrue(a == b, "equals() after copy(): a equals b")
        assertTrue(b == a, "equals() after copy(): b equals a")
    }

    @Test
    fun fromArray() {
        ColorManagement.enabled = false

        val a = Color()
        val array = doubleArrayOf(0.5, 0.6, 0.7, 0.0, 1.0, 0.0)

        a.fromArray(array)
        assertEquals(0.5, a.r, 0.0, "No offset: check r")
        assertEquals(0.6, a.g, 0.0, "No offset: check g")
        assertEquals(0.7, a.b, 0.0, "No offset: check b")

        a.fromArray(array, 3)
        assertEquals(0.0, a.r, 0.0, "With offset: check r")
        assertEquals(1.0, a.g, 0.0, "With offset: check g")
        assertEquals(0.0, a.b, 0.0, "With offset: check b")
    }

    @Test
    fun toArray() {
        ColorManagement.enabled = false

        val r = 0.5
        val g = 1.0
        val b = 0.0
        val a = Color(r, g, b)

        val array1 = a.toArray()
        assertEquals(r, array1[0], 0.0, "No array, no offset: check r")
        assertEquals(g, array1[1], 0.0, "No array, no offset: check g")
        assertEquals(b, array1[2], 0.0, "No array, no offset: check b")

        val array2 = mutableListOf<Double>()
        a.toArray(array2)
        assertEquals(r, array2[0], 0.0, "With array, no offset: check r")
        assertEquals(g, array2[1], 0.0, "With array, no offset: check g")
        assertEquals(b, array2[2], 0.0, "With array, no offset: check b")

        val array3 = mutableListOf<Double>()
        a.toArray(array3, 1)
        // Kotlin lists have no holes: the gap at [0] is filled with 0.0 (three.js
        // leaves it undefined).
        assertEquals(0.0, array3[0], 0.0, "With array and offset: gap filled with 0")
        assertEquals(r, array3[1], 0.0, "With array and offset: check r")
        assertEquals(g, array3[2], 0.0, "With array and offset: check g")
        assertEquals(b, array3[3], 0.0, "With array and offset: check b")
    }

    @Test
    fun toJSON() {
        ColorManagement.enabled = false

        val a = Color(0.0, 0.0, 0.0)
        val b = Color(0.0, 0.5, 0.0)
        val c = Color(1.0, 0.0, 0.0)
        val d = Color(1.0, 1.0, 1.0)

        assertEquals(0x000000, a.toJSON(), "Check black")
        assertEquals(0x008000, b.toJSON(), "Check half-blue")
        assertEquals(0xFF0000, c.toJSON(), "Check red")
        assertEquals(0xFFFFFF, d.toJSON(), "Check white")
    }

    // OTHERS - FUNCTIONAL
    @Test
    fun copyHex() {
        ColorManagement.enabled = false

        val c = Color()
        val c2 = Color(0xF5FFFA)
        c.copy(c2)
        assertTrue(c.getHex() == c2.getHex(), "Hex c: ${c.getHex()} Hex c2: ${c2.getHex()}")
    }

    // No copyColorString test: it needs Color('ivory'), and Kite3D has no color names.

    @Test
    fun setWithNum() {
        ColorManagement.enabled = false

        val c = Color()
        c.set(0xFF0000)
        assertTrue(c.r == 1.0, "Red: ${c.r}")
        assertTrue(c.g == 0.0, "Green: ${c.g}")
        assertTrue(c.b == 0.0, "Blue: ${c.b}")
    }

    // No setWithString or setStyle tests: Kite3D does not parse CSS strings or color names.

    @Test
    fun iterable() {
        ColorManagement.enabled = false

        val c = Color(0.5, 0.75, 1.0)
        val array = c.toList()
        assertEquals(0.5, array[0], 0.0, "Color is iterable.")
        assertEquals(0.75, array[1], 0.0, "Color is iterable.")
        assertEquals(1.0, array[2], 0.0, "Color is iterable.")
    }
}
