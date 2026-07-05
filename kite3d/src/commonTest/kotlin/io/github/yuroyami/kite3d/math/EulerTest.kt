/*
 * Copyright © 2026 yuroyami — MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Euler.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EulerTest {

    // Fresh instances per access (some tests mutate these, e.g. reorder) — avoids
    // cross-test bleed, mirroring the TestConstants.kt convention.
    private val eulerZero get() = Euler(0.0, 0.0, 0.0, EulerOrder.XYZ)
    private val eulerAxyz get() = Euler(1.0, 0.0, 0.0, EulerOrder.XYZ)
    private val eulerAzyx get() = Euler(0.0, 1.0, 0.0, EulerOrder.ZYX)

    private fun matrixEquals4(a: Matrix4, b: Matrix4, tolerance: Double = 0.0001): Boolean {
        if (a.elements.size != b.elements.size) return false
        for (i in a.elements.indices) {
            val delta = abs(a.elements[i] - b.elements[i])
            if (delta > tolerance) return false
        }
        return true
    }

    private fun quatEquals(a: Quaternion, b: Quaternion, tolerance: Double = 0.0001): Boolean {
        val diff = abs(a.x - b.x) + abs(a.y - b.y) + abs(a.z - b.z) + abs(a.w - b.w)
        return diff < tolerance
    }

    @Test
    fun instancing() {
        val a = Euler()
        assertTrue(a == eulerZero, "Passed!")
        assertFalse(a == eulerAxyz, "Passed!")
        assertFalse(a == eulerAzyx, "Passed!")
    }

    @Test
    fun defaultOrder() {
        assertEquals(EulerOrder.XYZ, Euler.DEFAULT_ORDER, "Passed!")
    }

    @Test
    fun x() {
        var a = Euler()
        assertTrue(a.x == 0.0, "Passed!")

        a = Euler(1.0, 2.0, 3.0)
        assertTrue(a.x == 1.0, "Passed!")

        a = Euler(4.0, 5.0, 6.0, EulerOrder.XYZ)
        assertTrue(a.x == 4.0, "Passed!")

        a = Euler(7.0, 8.0, 9.0, EulerOrder.XYZ)
        a.x = 10.0
        assertTrue(a.x == 10.0, "Passed!")

        a = Euler(11.0, 12.0, 13.0, EulerOrder.XYZ)
        var b = false
        a.onChange { b = true }
        a.x = 14.0
        assertTrue(b, "Passed!")
        assertTrue(a.x == 14.0, "Passed!")
    }

    @Test
    fun y() {
        var a = Euler()
        assertTrue(a.y == 0.0, "Passed!")

        a = Euler(1.0, 2.0, 3.0)
        assertTrue(a.y == 2.0, "Passed!")

        a = Euler(4.0, 5.0, 6.0, EulerOrder.XYZ)
        assertTrue(a.y == 5.0, "Passed!")

        a = Euler(7.0, 8.0, 9.0, EulerOrder.XYZ)
        a.y = 10.0
        assertTrue(a.y == 10.0, "Passed!")

        a = Euler(11.0, 12.0, 13.0, EulerOrder.XYZ)
        var b = false
        a.onChange { b = true }
        a.y = 14.0
        assertTrue(b, "Passed!")
        assertTrue(a.y == 14.0, "Passed!")
    }

    @Test
    fun z() {
        var a = Euler()
        assertTrue(a.z == 0.0, "Passed!")

        a = Euler(1.0, 2.0, 3.0)
        assertTrue(a.z == 3.0, "Passed!")

        a = Euler(4.0, 5.0, 6.0, EulerOrder.XYZ)
        assertTrue(a.z == 6.0, "Passed!")

        a = Euler(7.0, 8.0, 9.0, EulerOrder.XYZ)
        a.z = 10.0
        assertTrue(a.z == 10.0, "Passed!")

        a = Euler(11.0, 12.0, 13.0, EulerOrder.XYZ)
        var b = false
        a.onChange { b = true }
        a.z = 14.0
        assertTrue(b, "Passed!")
        assertTrue(a.z == 14.0, "Passed!")
    }

    @Test
    fun order() {
        var a = Euler()
        assertTrue(a.order == Euler.DEFAULT_ORDER, "Passed!")

        a = Euler(1.0, 2.0, 3.0)
        assertTrue(a.order == Euler.DEFAULT_ORDER, "Passed!")

        a = Euler(4.0, 5.0, 6.0, EulerOrder.YZX)
        assertTrue(a.order == EulerOrder.YZX, "Passed!")

        a = Euler(7.0, 8.0, 9.0, EulerOrder.YZX)
        a.order = EulerOrder.ZXY
        assertTrue(a.order == EulerOrder.ZXY, "Passed!")

        a = Euler(11.0, 12.0, 13.0, EulerOrder.YZX)
        var b = false
        a.onChange { b = true }
        a.order = EulerOrder.ZXY
        assertTrue(b, "Passed!")
        assertTrue(a.order == EulerOrder.ZXY, "Passed!")
    }

    // The upstream `isEuler` test checks a duck-type flag on Euler and its absence
    // on Vector3; dialect rule 10 drops these flags (`is Euler` is used instead),
    // so the test has no Kotlin equivalent and is omitted.

    @Test
    fun cloneCopyEquals() {
        val a = eulerAxyz.clone()
        assertTrue(a == eulerAxyz, "Passed!")
        assertFalse(a == eulerZero, "Passed!")
        assertFalse(a == eulerAzyx, "Passed!")

        a.copy(eulerAzyx)
        assertTrue(a == eulerAzyx, "Passed!")
        assertFalse(a == eulerAxyz, "Passed!")
        assertFalse(a == eulerZero, "Passed!")
    }

    @Test
    fun quaternionSetFromEulerEulerSetFromQuaternion() {
        val testValues = listOf(eulerZero, eulerAxyz, eulerAzyx)
        for (v in testValues) {
            val q = Quaternion().setFromEuler(v)

            val v2 = Euler().setFromQuaternion(q, v.order)
            val q2 = Quaternion().setFromEuler(v2)
            assertTrue(quatEquals(q, q2), "Passed!")
        }
    }

    @Test
    fun matrix4MakeRotationFromEulerEulerSetFromRotationMatrix() {
        val testValues = listOf(eulerZero, eulerAxyz, eulerAzyx)
        for (v in testValues) {
            val m = Matrix4().makeRotationFromEuler(v)

            val v2 = Euler().setFromRotationMatrix(m, v.order)
            val m2 = Matrix4().makeRotationFromEuler(v2)
            assertTrue(matrixEquals4(m, m2, 0.0001), "Passed!")
        }
    }

    @Test
    fun reorder() {
        val testValues = listOf(eulerZero, eulerAxyz, eulerAzyx)
        for (v in testValues) {
            val q = Quaternion().setFromEuler(v)

            v.reorder(EulerOrder.YZX)
            val q2 = Quaternion().setFromEuler(v)
            assertTrue(quatEquals(q, q2), "Passed!")

            v.reorder(EulerOrder.ZXY)
            val q3 = Quaternion().setFromEuler(v)
            assertTrue(quatEquals(q, q3), "Passed!")
        }
    }

    @Test
    fun setGetPropertiesCheckCallbacks() {
        var callbacks = 0
        val a = Euler()
        a.onChange { callbacks++ }

        a.x = 1.0
        a.y = 2.0
        a.z = 3.0
        a.order = EulerOrder.ZYX

        assertEquals(1.0, a.x, 0.0, "get: check x")
        assertEquals(2.0, a.y, 0.0, "get: check y")
        assertEquals(3.0, a.z, 0.0, "get: check z")
        assertEquals(EulerOrder.ZYX, a.order, "get: check order")

        // Upstream asserts onChange fired once per setter (4 total).
        assertEquals(4, callbacks, "onChange called four times")
    }

    @Test
    fun cloneCopyCheckCallbacks() {
        var a = Euler(1.0, 2.0, 3.0, EulerOrder.ZXY)
        val b = Euler(4.0, 5.0, 6.0, EulerOrder.XZY)
        var fails = 0
        var succeeds = 0

        a.onChange { fails++ }
        b.onChange { fails++ }

        // clone doesn't trigger onChange
        a = b.clone()
        assertTrue(a == b, "clone: check if a equals b")

        // copy triggers onChange once
        a = Euler(1.0, 2.0, 3.0, EulerOrder.ZXY)
        a.onChange { succeeds++ }
        a.copy(b)
        assertTrue(a == b, "copy: check if a equals b")
        assertEquals(0, fails, "cbFail must never run (clone made a fresh instance)")
        assertEquals(1, succeeds, "copy triggers onChange once")
    }

    @Test
    fun toArrayTest() {
        val order = EulerOrder.YXZ
        val a = Euler(x, y, z, order)

        var array = a.toArray()
        assertEquals(x, array[0], "No array, no offset: check x")
        assertEquals(y, array[1], "No array, no offset: check y")
        assertEquals(z, array[2], "No array, no offset: check z")
        assertEquals(order, array[3], "No array, no offset: check order")

        array = mutableListOf()
        a.toArray(array)
        assertEquals(x, array[0], "With array, no offset: check x")
        assertEquals(y, array[1], "With array, no offset: check y")
        assertEquals(z, array[2], "With array, no offset: check z")
        assertEquals(order, array[3], "With array, no offset: check order")

        array = mutableListOf()
        a.toArray(array, 1)
        // Kotlin lists have no holes: the gap at [0] is filled with 0.0 (three.js
        // leaves it undefined).
        assertEquals(0.0, array[0], "With array and offset: check [0]")
        assertEquals(x, array[1], "With array and offset: check x")
        assertEquals(y, array[2], "With array and offset: check y")
        assertEquals(z, array[3], "With array and offset: check z")
        assertEquals(order, array[4], "With array and offset: check order")
    }

    @Test
    fun fromArray() {
        var callbacks = 0
        var a = Euler()
        a.onChange { callbacks++ }

        a.fromArray(listOf(x, y, z))
        assertEquals(x, a.x, 0.0, "No order: check x")
        assertEquals(y, a.y, 0.0, "No order: check y")
        assertEquals(z, a.z, 0.0, "No order: check z")
        assertEquals(EulerOrder.XYZ, a.order, "No order: check order")

        a = Euler()
        a.onChange { callbacks++ }
        a.fromArray(listOf(x, y, z, EulerOrder.ZXY))
        assertEquals(x, a.x, 0.0, "With order: check x")
        assertEquals(y, a.y, 0.0, "With order: check y")
        assertEquals(z, a.z, 0.0, "With order: check z")
        assertEquals(EulerOrder.ZXY, a.order, "With order: check order")

        assertEquals(2, callbacks, "onChange called once per fromArray")
    }

    @Test
    fun onChangeTest() {
        // Upstream `_onChange` stores and later checks the callback identity via the
        // internal `_onChangeCallback` field. That field is private here; the Kotlin
        // API surface is `onChange(cb)`, so we verify the callback actually fires
        // instead of poking the private field.
        var fired = false
        val a = Euler(11.0, 12.0, 13.0, EulerOrder.XYZ)
        a.onChange { fired = true }
        a.x = 1.0
        assertTrue(fired, "registered callback fires on change")
    }

    // Upstream `_onChangeCallback` directly reassigns and invokes the private
    // callback field and asserts `this === a` binding. Kotlin lambdas have no
    // `this` receiver to check and the field is private; the onChange firing is
    // covered by onChangeTest above, so this JS-internal test is omitted.

    @Test
    fun iterable() {
        val e = Euler(0.5, 0.75, 1.0, EulerOrder.YZX)
        val array = e.toList()
        assertEquals(0.5, array[0], "Euler is iterable.")
        assertEquals(0.75, array[1], "Euler is iterable.")
        assertEquals(1.0, array[2], "Euler is iterable.")
        assertEquals(EulerOrder.YZX, array[3], "Euler is iterable.")
    }
}
