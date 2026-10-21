/*
 * Copyright © 2026 yuroyami. MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Vector2.tests.js (MIT).
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

class Vector2Test {

    @Test
    fun instancing() {
        var a = Vector2()
        assertEquals(0.0, a.x)
        assertEquals(0.0, a.y)

        a = Vector2(x, y)
        assertEquals(x, a.x)
        assertEquals(y, a.y)
    }

    @Test
    fun properties() {
        val a = Vector2(0.0, 0.0)
        a.width = 100.0
        assertEquals(100.0, a.width, 0.0, "width setter/getter")
        assertEquals(100.0, a.x, 0.0, "width aliases x")
        a.height = 200.0
        assertEquals(200.0, a.height, 0.0, "height setter/getter")
        assertEquals(200.0, a.y, 0.0, "height aliases y")
    }

    @Test
    fun set() {
        val a = Vector2()
        assertEquals(0.0, a.x)
        assertEquals(0.0, a.y)

        a.set(x, y)
        assertEquals(x, a.x)
        assertEquals(y, a.y)
    }

    @Test
    fun copy() {
        val a = Vector2(x, y)
        val b = Vector2().copy(a)
        assertEquals(x, b.x)
        assertEquals(y, b.y)

        // ensure it is a true copy
        a.x = 0.0
        a.y = -1.0
        assertEquals(x, b.x)
        assertEquals(y, b.y)
    }

    @Test
    fun add() {
        val a = Vector2(x, y)
        val b = Vector2(-x, -y)

        a.add(b)
        assertEquals(0.0, a.x)
        assertEquals(0.0, a.y)

        val c = Vector2().addVectors(b, b)
        assertEquals(-2 * x, c.x)
        assertEquals(-2 * y, c.y)
    }

    @Test
    fun addScaledVector() {
        val a = Vector2(x, y)
        val b = Vector2(2.0, 3.0)
        val s = 3.0

        a.addScaledVector(b, s)
        assertEquals(x + b.x * s, a.x, 0.0, "Check x")
        assertEquals(y + b.y * s, a.y, 0.0, "Check y")
    }

    @Test
    fun sub() {
        val a = Vector2(x, y)
        val b = Vector2(-x, -y)

        a.sub(b)
        assertEquals(2 * x, a.x)
        assertEquals(2 * y, a.y)

        val c = Vector2().subVectors(a, a)
        assertEquals(0.0, c.x)
        assertEquals(0.0, c.y)
    }

    @Test
    fun applyMatrix3() {
        val a = Vector2(x, y)
        val m = Matrix3().set(2.0, 3.0, 5.0, 7.0, 11.0, 13.0, 17.0, 19.0, 23.0)

        a.applyMatrix3(m)
        assertEquals(18.0, a.x, 0.0, "Check x")
        assertEquals(60.0, a.y, 0.0, "Check y")
    }

    @Test
    fun negate() {
        val a = Vector2(x, y)
        a.negate()
        assertEquals(-x, a.x)
        assertEquals(-y, a.y)
    }

    @Test
    fun dot() {
        val a = Vector2(x, y)
        val b = Vector2(-x, -y)
        val c = Vector2()

        assertEquals(-x * x - y * y, a.dot(b))
        assertEquals(0.0, a.dot(c))
    }

    @Test
    fun cross() {
        val a = Vector2(x, y)
        val b = Vector2(2 * x, -y)
        val answer = -18.0
        assertTrue(abs(answer - a.cross(b)) <= eps, "Check cross")
    }

    @Test
    fun manhattanLength() {
        val a = Vector2(x, 0.0)
        val b = Vector2(0.0, -y)
        val c = Vector2()

        assertEquals(x, a.manhattanLength(), 0.0, "Positive component")
        assertEquals(y, b.manhattanLength(), 0.0, "Negative component")
        assertEquals(0.0, c.manhattanLength(), 0.0, "Empty component")

        a.set(x, y)
        assertEquals(abs(x) + abs(y), a.manhattanLength(), 0.0, "Two components")
    }

    @Test
    fun normalize() {
        val a = Vector2(x, 0.0)
        val b = Vector2(0.0, -y)

        a.normalize()
        assertEquals(1.0, a.length())
        assertEquals(1.0, a.x)

        b.normalize()
        assertEquals(1.0, b.length())
        assertEquals(-1.0, b.y)
    }

    @Test
    fun angleTo() {
        val a = Vector2(-0.18851655680720186, 0.9820700116639124)
        val b = Vector2(0.18851655680720186, -0.9820700116639124)

        assertEquals(0.0, a.angleTo(a), 1e-12)
        assertEquals(PI, a.angleTo(b), 1e-12)

        val vx = Vector2(1.0, 0.0)
        val vy = Vector2(0.0, 1.0)

        assertEquals(PI / 2, vx.angleTo(vy), 1e-12)
        assertEquals(PI / 2, vy.angleTo(vx), 1e-12)

        assertTrue(abs(vx.angleTo(Vector2(1.0, 1.0)) - PI / 4) < 0.0000001)
    }

    @Test
    fun setLength() {
        var a = Vector2(x, 0.0)

        assertEquals(x, a.length())
        a.setLength(y)
        assertEquals(y, a.length())

        a = Vector2(0.0, 0.0)
        assertEquals(0.0, a.length())
        a.setLength(y)
        assertEquals(0.0, a.length())
        // JS also asserts setLength() with a missing arg yields NaN; that is a
        // JS arity quirk with no Kotlin equivalent (length is a required Double).
    }

    @Test
    fun equalsTest() {
        val a = Vector2(x, 0.0)
        val b = Vector2(0.0, -y)

        assertTrue(a.x != b.x)
        assertTrue(a.y != b.y)

        assertFalse(a == b)
        assertFalse(b == a)

        a.copy(b)
        assertEquals(b.x, a.x)
        assertEquals(b.y, a.y)

        assertTrue(a == b)
        assertTrue(b == a)
    }

    @Test
    fun fromArray() {
        val a = Vector2()
        val array = doubleArrayOf(1.0, 2.0, 3.0, 4.0)

        a.fromArray(array)
        assertEquals(1.0, a.x, 0.0, "No offset: x")
        assertEquals(2.0, a.y, 0.0, "No offset: y")

        a.fromArray(array, 2)
        assertEquals(3.0, a.x, 0.0, "Offset: x")
        assertEquals(4.0, a.y, 0.0, "Offset: y")
    }

    @Test
    fun toArray() {
        val a = Vector2(x, y)

        val array1 = a.toArray()
        assertEquals(x, array1[0], 0.0, "No offset: x")
        assertEquals(y, array1[1], 0.0, "No offset: y")

        val array2 = mutableListOf<Double>()
        a.toArray(array2)
        assertEquals(x, array2[0], 0.0, "Into list: x")
        assertEquals(y, array2[1], 0.0, "Into list: y")

        val array3 = mutableListOf<Double>()
        a.toArray(array3, 1)
        // Kotlin lists have no holes: the gap at [0] is filled with 0.0 (three.js
        // leaves it undefined).
        assertEquals(0.0, array3[0], 0.0, "Offset: gap filled with 0")
        assertEquals(x, array3[1], 0.0, "Offset: x")
        assertEquals(y, array3[2], 0.0, "Offset: y")
    }

    @Test
    fun fromBufferAttribute() {
        val a = Vector2()
        val attr = TestBufferAttribute(doubleArrayOf(1.0, 2.0, 3.0, 4.0), 2)

        a.fromBufferAttribute(attr, 0)
        assertEquals(1.0, a.x, 0.0, "Index 0: x")
        assertEquals(2.0, a.y, 0.0, "Index 0: y")

        a.fromBufferAttribute(attr, 1)
        assertEquals(3.0, a.x, 0.0, "Index 1: x")
        assertEquals(4.0, a.y, 0.0, "Index 1: y")
    }

    @Test
    fun setXY() {
        val a = Vector2()
        assertEquals(0.0, a.x)
        assertEquals(0.0, a.y)

        a.setX(x)
        a.setY(y)
        assertEquals(x, a.x)
        assertEquals(y, a.y)
    }

    @Test
    fun setComponentGetComponent() {
        val a = Vector2()
        assertEquals(0.0, a.x)
        assertEquals(0.0, a.y)

        a.setComponent(0, 1.0)
        a.setComponent(1, 2.0)
        assertEquals(1.0, a.getComponent(0))
        assertEquals(2.0, a.getComponent(1))
    }

    @Test
    fun setComponentGetComponentExceptions() {
        val a = Vector2(0.0, 0.0)
        assertFailsWith<IllegalArgumentException> { a.setComponent(2, 0.0) }
        assertFailsWith<IllegalArgumentException> { a.getComponent(2) }
    }

    @Test
    fun multiplyDivideScalar() {
        val a = Vector2(x, y)
        val b = Vector2(-x, -y)

        a.multiplyScalar(-2.0)
        assertEquals(x * -2, a.x)
        assertEquals(y * -2, a.y)

        b.multiplyScalar(-2.0)
        assertEquals(2 * x, b.x)
        assertEquals(2 * y, b.y)

        a.divideScalar(-2.0)
        assertEquals(x, a.x)
        assertEquals(y, a.y)

        b.divideScalar(-2.0)
        assertEquals(-x, b.x)
        assertEquals(-y, b.y)
    }

    @Test
    fun minMaxClamp() {
        val a = Vector2(x, y)
        val b = Vector2(-x, -y)
        val c = Vector2()

        c.copy(a).min(b)
        assertEquals(-x, c.x)
        assertEquals(-y, c.y)

        c.copy(a).max(b)
        assertEquals(x, c.x)
        assertEquals(y, c.y)

        c.set(-2 * x, 2 * y)
        c.clamp(b, a)
        assertEquals(-x, c.x)
        assertEquals(y, c.y)

        c.set(-2 * x, 2 * x)
        c.clampScalar(-x, x)
        assertEquals(-x, c.x, 0.0, "scalar clamp x")
        assertEquals(x, c.y, 0.0, "scalar clamp y")
    }

    @Test
    fun rounding() {
        assertEquals(Vector2(-1.0, 0.0), Vector2(-0.1, 0.1).floor(), "floor .1")
        assertEquals(Vector2(-1.0, 0.0), Vector2(-0.5, 0.5).floor(), "floor .5")
        assertEquals(Vector2(-1.0, 0.0), Vector2(-0.9, 0.9).floor(), "floor .9")

        assertEquals(Vector2(0.0, 1.0), Vector2(-0.1, 0.1).ceil(), "ceil .1")
        assertEquals(Vector2(0.0, 1.0), Vector2(-0.5, 0.5).ceil(), "ceil .5")
        assertEquals(Vector2(0.0, 1.0), Vector2(-0.9, 0.9).ceil(), "ceil .9")

        assertEquals(Vector2(0.0, 0.0), Vector2(-0.1, 0.1).round(), "round .1")
        assertEquals(Vector2(0.0, 1.0), Vector2(-0.5, 0.5).round(), "round .5")
        assertEquals(Vector2(-1.0, 1.0), Vector2(-0.9, 0.9).round(), "round .9")

        assertEquals(Vector2(0.0, 0.0), Vector2(-0.1, 0.1).roundToZero(), "roundToZero .1")
        assertEquals(Vector2(0.0, 0.0), Vector2(-0.5, 0.5).roundToZero(), "roundToZero .5")
        assertEquals(Vector2(0.0, 0.0), Vector2(-0.9, 0.9).roundToZero(), "roundToZero .9")
        assertEquals(Vector2(-1.0, 1.0), Vector2(-1.1, 1.1).roundToZero(), "roundToZero 1.1")
        assertEquals(Vector2(-1.0, 1.0), Vector2(-1.5, 1.5).roundToZero(), "roundToZero 1.5")
        assertEquals(Vector2(-1.0, 1.0), Vector2(-1.9, 1.9).roundToZero(), "roundToZero 1.9")
    }

    @Test
    fun lengthLengthSq() {
        val a = Vector2(x, 0.0)
        val b = Vector2(0.0, -y)
        val c = Vector2()

        assertEquals(x, a.length())
        assertEquals(x * x, a.lengthSq())
        assertEquals(y, b.length())
        assertEquals(y * y, b.lengthSq())
        assertEquals(0.0, c.length())
        assertEquals(0.0, c.lengthSq())

        a.set(x, y)
        assertEquals(sqrt(x * x + y * y), a.length())
        assertEquals(x * x + y * y, a.lengthSq())
    }

    @Test
    fun distanceToDistanceToSquared() {
        val a = Vector2(x, 0.0)
        val b = Vector2(0.0, -y)
        val c = Vector2()

        assertEquals(x, a.distanceTo(c))
        assertEquals(x * x, a.distanceToSquared(c))

        assertEquals(y, b.distanceTo(c))
        assertEquals(y * y, b.distanceToSquared(c))
    }

    @Test
    fun lerpClone() {
        val a = Vector2(x, 0.0)
        val b = Vector2(0.0, -y)

        assertTrue(a.clone().lerp(a, 0.0) == a.clone().lerp(a, 0.5))
        assertTrue(a.clone().lerp(a, 0.0) == a.clone().lerp(a, 1.0))

        assertTrue(a.clone().lerp(b, 0.0) == a)

        assertEquals(x * 0.5, a.clone().lerp(b, 0.5).x)
        assertEquals(-y * 0.5, a.clone().lerp(b, 0.5).y)

        assertTrue(a.clone().lerp(b, 1.0) == b)
    }

    @Test
    fun setScalarAddScalarSubScalar() {
        val a = Vector2(1.0, 1.0)
        val s = 3.0

        a.setScalar(s)
        assertEquals(s, a.x, 0.0, "setScalar: x")
        assertEquals(s, a.y, 0.0, "setScalar: y")

        a.addScalar(s)
        assertEquals(2 * s, a.x, 0.0, "addScalar: x")
        assertEquals(2 * s, a.y, 0.0, "addScalar: y")

        a.subScalar(2 * s)
        assertEquals(0.0, a.x, 0.0, "subScalar: x")
        assertEquals(0.0, a.y, 0.0, "subScalar: y")
    }

    @Test
    fun multiplyDivide() {
        val a = Vector2(x, y)
        val b = Vector2(2 * x, 2 * y)
        val c = Vector2(4 * x, 4 * y)

        a.multiply(b)
        assertEquals(x * b.x, a.x, 0.0, "multiply: x")
        assertEquals(y * b.y, a.y, 0.0, "multiply: y")

        b.divide(c)
        assertEquals(0.5, b.x, 0.0, "divide: x")
        assertEquals(0.5, b.y, 0.0, "divide: y")
    }

    @Test
    fun iterable() {
        val v = Vector2(0.0, 1.0)
        val array = v.toList()
        assertEquals(0.0, array[0], 0.0, "Vector2 is iterable")
        assertEquals(1.0, array[1], 0.0, "Vector2 is iterable")
    }
}
