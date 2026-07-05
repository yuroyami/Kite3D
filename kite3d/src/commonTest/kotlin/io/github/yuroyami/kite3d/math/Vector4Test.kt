/*
 * Copyright © 2026 yuroyami — MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Vector4.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Vector4Test {

    @Test
    fun instancing() {
        var a = Vector4()
        assertEquals(0.0, a.x)
        assertEquals(0.0, a.y)
        assertEquals(0.0, a.z)
        assertEquals(1.0, a.w)

        a = Vector4(x, y, z, w)
        assertEquals(x, a.x)
        assertEquals(y, a.y)
        assertEquals(z, a.z)
        assertEquals(w, a.w)
    }

    // isVector4: the duck-type flag is dropped in the Kotlin port (use `is Vector4`).

    @Test
    fun set() {
        val a = Vector4()
        assertEquals(0.0, a.x)
        assertEquals(0.0, a.y)
        assertEquals(0.0, a.z)
        assertEquals(1.0, a.w)

        a.set(x, y, z, w)
        assertEquals(x, a.x)
        assertEquals(y, a.y)
        assertEquals(z, a.z)
        assertEquals(w, a.w)
    }

    @Test
    fun setX() {
        val a = Vector4()
        assertEquals(0.0, a.x)

        a.setX(x)
        assertEquals(x, a.x)
    }

    @Test
    fun setY() {
        val a = Vector4()
        assertEquals(0.0, a.y)

        a.setY(y)
        assertEquals(y, a.y)
    }

    @Test
    fun setZ() {
        val a = Vector4()
        assertEquals(0.0, a.z)

        a.setZ(z)
        assertEquals(z, a.z)
    }

    @Test
    fun setW() {
        val a = Vector4()
        assertEquals(1.0, a.w)

        a.setW(w)
        assertEquals(w, a.w)
    }

    @Test
    fun copy() {
        val a = Vector4(x, y, z, w)
        val b = Vector4().copy(a)
        assertEquals(x, b.x)
        assertEquals(y, b.y)
        assertEquals(z, b.z)
        assertEquals(w, b.w)

        // ensure it is a true copy
        a.x = 0.0
        a.y = -1.0
        a.z = -2.0
        a.w = -3.0
        assertEquals(x, b.x)
        assertEquals(y, b.y)
        assertEquals(z, b.z)
        assertEquals(w, b.w)
    }

    @Test
    fun add() {
        val a = Vector4(x, y, z, w)
        val b = Vector4(-x, -y, -z, -w)

        a.add(b)
        assertEquals(0.0, a.x)
        assertEquals(0.0, a.y)
        assertEquals(0.0, a.z)
        assertEquals(0.0, a.w)
    }

    @Test
    fun addVectors() {
        val b = Vector4(-x, -y, -z, -w)
        val c = Vector4().addVectors(b, b)

        assertEquals(-2 * x, c.x)
        assertEquals(-2 * y, c.y)
        assertEquals(-2 * z, c.z)
        assertEquals(-2 * w, c.w)
    }

    @Test
    fun addScaledVector() {
        val a = Vector4(x, y, z, w)
        val b = Vector4(6.0, 7.0, 8.0, 9.0)
        val s = 3.0

        a.addScaledVector(b, s)
        assertEquals(x + b.x * s, a.x, 0.0, "Check x")
        assertEquals(y + b.y * s, a.y, 0.0, "Check y")
        assertEquals(z + b.z * s, a.z, 0.0, "Check z")
        assertEquals(w + b.w * s, a.w, 0.0, "Check w")
    }

    @Test
    fun sub() {
        val a = Vector4(x, y, z, w)
        val b = Vector4(-x, -y, -z, -w)

        a.sub(b)
        assertEquals(2 * x, a.x)
        assertEquals(2 * y, a.y)
        assertEquals(2 * z, a.z)
        assertEquals(2 * w, a.w)
    }

    @Test
    fun subVectors() {
        val a = Vector4(x, y, z, w)
        val c = Vector4().subVectors(a, a)
        assertEquals(0.0, c.x)
        assertEquals(0.0, c.y)
        assertEquals(0.0, c.z)
        assertEquals(0.0, c.w)
    }

    @Test
    fun applyMatrix4() {
        val a = Vector4(x, y, z, w)
        val m = Matrix4().makeRotationX(PI)
        val expected = Vector4(2.0, -3.0, -4.0, 5.0)

        a.applyMatrix4(m)
        assertTrue(abs(a.x - expected.x) <= eps, "Rotation matrix: check x")
        assertTrue(abs(a.y - expected.y) <= eps, "Rotation matrix: check y")
        assertTrue(abs(a.z - expected.z) <= eps, "Rotation matrix: check z")
        assertTrue(abs(a.w - expected.w) <= eps, "Rotation matrix: check w")

        a.set(x, y, z, w)
        m.makeTranslation(5.0, 7.0, 11.0)
        expected.set(27.0, 38.0, 59.0, 5.0)

        a.applyMatrix4(m)
        assertTrue(abs(a.x - expected.x) <= eps, "Translation matrix: check x")
        assertTrue(abs(a.y - expected.y) <= eps, "Translation matrix: check y")
        assertTrue(abs(a.z - expected.z) <= eps, "Translation matrix: check z")
        assertTrue(abs(a.w - expected.w) <= eps, "Translation matrix: check w")

        a.set(x, y, z, w)
        m.set(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0)
        expected.set(2.0, 3.0, 4.0, 4.0)

        a.applyMatrix4(m)
        assertTrue(abs(a.x - expected.x) <= eps, "Custom matrix: check x")
        assertTrue(abs(a.y - expected.y) <= eps, "Custom matrix: check y")
        assertTrue(abs(a.z - expected.z) <= eps, "Custom matrix: check z")
        assertTrue(abs(a.w - expected.w) <= eps, "Custom matrix: check w")

        a.set(x, y, z, w)
        m.set(2.0, 3.0, 5.0, 7.0, 11.0, 13.0, 17.0, 19.0, 23.0, 29.0, 31.0, 37.0, 41.0, 43.0, 47.0, 53.0)
        expected.set(68.0, 224.0, 442.0, 664.0)

        a.applyMatrix4(m)
        assertTrue(abs(a.x - expected.x) <= eps, "Bogus matrix: check x")
        assertTrue(abs(a.y - expected.y) <= eps, "Bogus matrix: check y")
        assertTrue(abs(a.z - expected.z) <= eps, "Bogus matrix: check z")
        assertTrue(abs(a.w - expected.w) <= eps, "Bogus matrix: check w")
    }

    @Test
    fun divide() {
        val a = Vector4(7.0, 8.0, 9.0, 0.0)
        val b = Vector4(2.0, 2.0, 3.0, 4.0)

        a.divide(b)
        assertEquals(3.5, a.x, 0.0, "Check divide x")
        assertEquals(4.0, a.y, 0.0, "Check divide y")
        assertEquals(3.0, a.z, 0.0, "Check divide z")
        assertEquals(0.0, a.w, 0.0, "Check divide w")
    }

    @Test
    fun setFromMatrixPosition() {
        val a = Vector4()
        val m = Matrix4().set(
            2.0, 3.0, 5.0, 7.0,
            11.0, 13.0, 17.0, 19.0,
            23.0, 29.0, 31.0, 37.0,
            41.0, 43.0, 47.0, 53.0,
        )

        a.setFromMatrixPosition(m)
        assertEquals(7.0, a.x, 0.0, "Check x")
        assertEquals(19.0, a.y, 0.0, "Check y")
        assertEquals(37.0, a.z, 0.0, "Check z")
        assertEquals(53.0, a.w, 0.0, "Check w")
    }

    @Test
    fun clampScalar() {
        val a = Vector4(-0.1, 0.01, 0.5, 1.5)
        val clamped = Vector4(0.1, 0.1, 0.5, 1.0)

        a.clampScalar(0.1, 1.0)
        assertTrue(abs(a.x - clamped.x) <= eps, "Check x")
        assertTrue(abs(a.y - clamped.y) <= eps, "Check y")
        assertTrue(abs(a.z - clamped.z) <= eps, "Check z")
        assertTrue(abs(a.w - clamped.w) <= eps, "Check w")
    }

    @Test
    fun negate() {
        val a = Vector4(x, y, z, w)

        a.negate()
        assertEquals(-x, a.x)
        assertEquals(-y, a.y)
        assertEquals(-z, a.z)
        assertEquals(-w, a.w)
    }

    @Test
    fun dot() {
        val a = Vector4(x, y, z, w)
        val b = Vector4(-x, -y, -z, -w)
        val c = Vector4(0.0, 0.0, 0.0, 0.0)

        var result = a.dot(b)
        assertEquals(-x * x - y * y - z * z - w * w, result)

        result = a.dot(c)
        assertEquals(0.0, result)
    }

    @Test
    fun manhattanLength() {
        val a = Vector4(x, 0.0, 0.0, 0.0)
        val b = Vector4(0.0, -y, 0.0, 0.0)
        val c = Vector4(0.0, 0.0, z, 0.0)
        val d = Vector4(0.0, 0.0, 0.0, w)
        val e = Vector4(0.0, 0.0, 0.0, 0.0)

        assertEquals(x, a.manhattanLength(), 0.0, "Positive x")
        assertEquals(y, b.manhattanLength(), 0.0, "Negative y")
        assertEquals(z, c.manhattanLength(), 0.0, "Positive z")
        assertEquals(w, d.manhattanLength(), 0.0, "Positive w")
        assertEquals(0.0, e.manhattanLength(), 0.0, "Empty initialization")

        a.set(x, y, z, w)
        assertEquals(
            abs(x) + abs(y) + abs(z) + abs(w),
            a.manhattanLength(),
            0.0,
            "All components",
        )
    }

    @Test
    fun normalize() {
        val a = Vector4(x, 0.0, 0.0, 0.0)
        val b = Vector4(0.0, -y, 0.0, 0.0)
        val c = Vector4(0.0, 0.0, z, 0.0)
        val d = Vector4(0.0, 0.0, 0.0, -w)

        a.normalize()
        assertEquals(1.0, a.length())
        assertEquals(1.0, a.x)

        b.normalize()
        assertEquals(1.0, b.length())
        assertEquals(-1.0, b.y)

        c.normalize()
        assertEquals(1.0, c.length())
        assertEquals(1.0, c.z)

        d.normalize()
        assertEquals(1.0, d.length())
        assertEquals(-1.0, d.w)
    }

    @Test
    fun setLength() {
        var a = Vector4(x, 0.0, 0.0, 0.0)

        assertEquals(x, a.length())
        a.setLength(y)
        assertEquals(y, a.length())

        a = Vector4(0.0, 0.0, 0.0, 0.0)
        assertEquals(0.0, a.length())
        a.setLength(y)
        assertEquals(0.0, a.length())
        // JS also asserts setLength() with a missing arg yields NaN; that is a JS
        // arity quirk with no Kotlin equivalent (length is a required Double).
    }

    @Test
    fun equalsTest() {
        val a = Vector4(x, 0.0, z, 0.0)
        val b = Vector4(0.0, -y, 0.0, -w)

        assertTrue(a.x != b.x)
        assertTrue(a.y != b.y)
        assertTrue(a.z != b.z)
        assertTrue(a.w != b.w)

        assertFalse(a == b)
        assertFalse(b == a)

        a.copy(b)
        assertEquals(b.x, a.x)
        assertEquals(b.y, a.y)
        assertEquals(b.z, a.z)
        assertEquals(b.w, a.w)

        assertTrue(a == b)
        assertTrue(b == a)
    }

    @Test
    fun fromArray() {
        val a = Vector4()
        val array = doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)

        a.fromArray(array)
        assertEquals(1.0, a.x, 0.0, "No offset: check x")
        assertEquals(2.0, a.y, 0.0, "No offset: check y")
        assertEquals(3.0, a.z, 0.0, "No offset: check z")
        assertEquals(4.0, a.w, 0.0, "No offset: check w")

        a.fromArray(array, 4)
        assertEquals(5.0, a.x, 0.0, "With offset: check x")
        assertEquals(6.0, a.y, 0.0, "With offset: check y")
        assertEquals(7.0, a.z, 0.0, "With offset: check z")
        assertEquals(8.0, a.w, 0.0, "With offset: check w")
    }

    @Test
    fun toArray() {
        val a = Vector4(x, y, z, w)

        val array1 = a.toArray()
        assertEquals(x, array1[0], 0.0, "No array, no offset: check x")
        assertEquals(y, array1[1], 0.0, "No array, no offset: check y")
        assertEquals(z, array1[2], 0.0, "No array, no offset: check z")
        assertEquals(w, array1[3], 0.0, "No array, no offset: check w")

        val array2 = mutableListOf<Double>()
        a.toArray(array2)
        assertEquals(x, array2[0], 0.0, "With array, no offset: check x")
        assertEquals(y, array2[1], 0.0, "With array, no offset: check y")
        assertEquals(z, array2[2], 0.0, "With array, no offset: check z")
        assertEquals(w, array2[3], 0.0, "With array, no offset: check w")

        val array3 = mutableListOf<Double>()
        a.toArray(array3, 1)
        // Kotlin lists have no holes: the gap at [0] is filled with 0.0 (three.js
        // leaves it undefined).
        assertEquals(0.0, array3[0], 0.0, "With array and offset: gap filled with 0")
        assertEquals(x, array3[1], 0.0, "With array and offset: check x")
        assertEquals(y, array3[2], 0.0, "With array and offset: check y")
        assertEquals(z, array3[3], 0.0, "With array and offset: check z")
        assertEquals(w, array3[4], 0.0, "With array and offset: check w")
    }

    @Test
    fun fromBufferAttribute() {
        val a = Vector4()
        val attr = TestBufferAttribute(doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0), 4)

        a.fromBufferAttribute(attr, 0)
        assertEquals(1.0, a.x, 0.0, "Offset 0: check x")
        assertEquals(2.0, a.y, 0.0, "Offset 0: check y")
        assertEquals(3.0, a.z, 0.0, "Offset 0: check z")
        assertEquals(4.0, a.w, 0.0, "Offset 0: check w")

        a.fromBufferAttribute(attr, 1)
        assertEquals(5.0, a.x, 0.0, "Offset 1: check x")
        assertEquals(6.0, a.y, 0.0, "Offset 1: check y")
        assertEquals(7.0, a.z, 0.0, "Offset 1: check z")
        assertEquals(8.0, a.w, 0.0, "Offset 1: check w")
    }

    @Test
    fun setXsetYsetZsetW() {
        val a = Vector4()
        assertEquals(0.0, a.x)
        assertEquals(0.0, a.y)
        assertEquals(0.0, a.z)
        assertEquals(1.0, a.w)

        a.setX(x)
        a.setY(y)
        a.setZ(z)
        a.setW(w)

        assertEquals(x, a.x)
        assertEquals(y, a.y)
        assertEquals(z, a.z)
        assertEquals(w, a.w)
    }

    @Test
    fun setComponentGetComponent() {
        val a = Vector4()
        assertEquals(0.0, a.x)
        assertEquals(0.0, a.y)
        assertEquals(0.0, a.z)
        assertEquals(1.0, a.w)

        a.setComponent(0, 1.0)
        a.setComponent(1, 2.0)
        a.setComponent(2, 3.0)
        a.setComponent(3, 4.0)
        assertEquals(1.0, a.getComponent(0))
        assertEquals(2.0, a.getComponent(1))
        assertEquals(3.0, a.getComponent(2))
        assertEquals(4.0, a.getComponent(3))
    }

    @Test
    fun setComponentGetComponentExceptions() {
        val a = Vector4()
        assertFailsWith<IllegalArgumentException> { a.setComponent(4, 0.0) }
        assertFailsWith<IllegalArgumentException> { a.getComponent(4) }
    }

    @Test
    fun setScalarAddScalarSubScalar() {
        val a = Vector4()
        val s = 3.0

        a.setScalar(s)
        assertEquals(s, a.x, 0.0, "setScalar: check x")
        assertEquals(s, a.y, 0.0, "setScalar: check y")
        assertEquals(s, a.z, 0.0, "setScalar: check z")
        assertEquals(s, a.w, 0.0, "setScalar: check w")

        a.addScalar(s)
        assertEquals(2 * s, a.x, 0.0, "addScalar: check x")
        assertEquals(2 * s, a.y, 0.0, "addScalar: check y")
        assertEquals(2 * s, a.z, 0.0, "addScalar: check z")
        assertEquals(2 * s, a.w, 0.0, "addScalar: check w")

        a.subScalar(2 * s)
        assertEquals(0.0, a.x, 0.0, "subScalar: check x")
        assertEquals(0.0, a.y, 0.0, "subScalar: check y")
        assertEquals(0.0, a.z, 0.0, "subScalar: check z")
        assertEquals(0.0, a.w, 0.0, "subScalar: check w")
    }

    @Test
    fun multiplyScalarDivideScalar() {
        val a = Vector4(x, y, z, w)
        val b = Vector4(-x, -y, -z, -w)

        a.multiplyScalar(-2.0)
        assertEquals(x * -2, a.x)
        assertEquals(y * -2, a.y)
        assertEquals(z * -2, a.z)
        assertEquals(w * -2, a.w)

        b.multiplyScalar(-2.0)
        assertEquals(2 * x, b.x)
        assertEquals(2 * y, b.y)
        assertEquals(2 * z, b.z)
        assertEquals(2 * w, b.w)

        a.divideScalar(-2.0)
        assertEquals(x, a.x)
        assertEquals(y, a.y)
        assertEquals(z, a.z)
        assertEquals(w, a.w)

        b.divideScalar(-2.0)
        assertEquals(-x, b.x)
        assertEquals(-y, b.y)
        assertEquals(-z, b.z)
        assertEquals(-w, b.w)
    }

    @Test
    fun minMaxClamp() {
        val a = Vector4(x, y, z, w)
        val b = Vector4(-x, -y, -z, -w)
        val c = Vector4()

        c.copy(a).min(b)
        assertEquals(-x, c.x)
        assertEquals(-y, c.y)
        assertEquals(-z, c.z)
        assertEquals(-w, c.w)

        c.copy(a).max(b)
        assertEquals(x, c.x)
        assertEquals(y, c.y)
        assertEquals(z, c.z)
        assertEquals(w, c.w)

        c.set(-2 * x, 2 * y, -2 * z, 2 * w)
        c.clamp(b, a)
        assertEquals(-x, c.x)
        assertEquals(y, c.y)
        assertEquals(-z, c.z)
        assertEquals(w, c.w)
    }

    @Test
    fun lengthLengthSq() {
        val a = Vector4(x, 0.0, 0.0, 0.0)
        val b = Vector4(0.0, -y, 0.0, 0.0)
        val c = Vector4(0.0, 0.0, z, 0.0)
        val d = Vector4(0.0, 0.0, 0.0, w)
        val e = Vector4(0.0, 0.0, 0.0, 0.0)

        assertEquals(x, a.length())
        assertEquals(x * x, a.lengthSq())
        assertEquals(y, b.length())
        assertEquals(y * y, b.lengthSq())
        assertEquals(z, c.length())
        assertEquals(z * z, c.lengthSq())
        assertEquals(w, d.length())
        assertEquals(w * w, d.lengthSq())
        assertEquals(0.0, e.length())
        assertEquals(0.0, e.lengthSq())

        a.set(x, y, z, w)
        assertEquals(sqrt(x * x + y * y + z * z + w * w), a.length())
        assertEquals(x * x + y * y + z * z + w * w, a.lengthSq())
    }

    @Test
    fun lerpClone() {
        val a = Vector4(x, 0.0, z, 0.0)
        val b = Vector4(0.0, -y, 0.0, -w)

        assertTrue(a.lerp(a, 0.0) == a.lerp(a, 0.5))
        assertTrue(a.lerp(a, 0.0) == a.lerp(a, 1.0))

        assertTrue(a.clone().lerp(b, 0.0) == a)

        assertEquals(x * 0.5, a.clone().lerp(b, 0.5).x)
        assertEquals(-y * 0.5, a.clone().lerp(b, 0.5).y)
        assertEquals(z * 0.5, a.clone().lerp(b, 0.5).z)
        assertEquals(-w * 0.5, a.clone().lerp(b, 0.5).w)

        assertTrue(a.clone().lerp(b, 1.0) == b)
    }

    @Test
    fun iterable() {
        val v = Vector4(0.0, 0.3, 0.7, 1.0)
        val array = v.toList()
        assertEquals(0.0, array[0], 0.0, "Vector4 is iterable")
        assertEquals(0.3, array[1], 0.0, "Vector4 is iterable")
        assertEquals(0.7, array[2], 0.0, "Vector4 is iterable")
        assertEquals(1.0, array[3], 0.0, "Vector4 is iterable")
    }
}
