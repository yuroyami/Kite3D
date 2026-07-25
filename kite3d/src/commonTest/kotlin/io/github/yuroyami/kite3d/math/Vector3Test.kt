/*
 * Copyright © 2026 yuroyami. MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Vector3.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Vector3Test {

    @Test
    fun instancing() {
        var a = Vector3()
        assertEquals(0.0, a.x)
        assertEquals(0.0, a.y)
        assertEquals(0.0, a.z)

        a = Vector3(x, y, z)
        assertEquals(x, a.x)
        assertEquals(y, a.y)
        assertEquals(z, a.z)
    }

    // isVector3: the duck-type flag is dropped in the Kotlin port (use `is Vector3`).

    @Test
    fun set() {
        val a = Vector3()
        assertEquals(0.0, a.x)
        assertEquals(0.0, a.y)
        assertEquals(0.0, a.z)

        a.set(x, y, z)
        assertEquals(x, a.x)
        assertEquals(y, a.y)
        assertEquals(z, a.z)
    }

    @Test
    fun copy() {
        val a = Vector3(x, y, z)
        val b = Vector3().copy(a)
        assertEquals(x, b.x)
        assertEquals(y, b.y)
        assertEquals(z, b.z)

        // ensure it is a true copy
        a.x = 0.0
        a.y = -1.0
        a.z = -2.0
        assertEquals(x, b.x)
        assertEquals(y, b.y)
        assertEquals(z, b.z)
    }

    @Test
    fun add() {
        val a = Vector3(x, y, z)
        val b = Vector3(-x, -y, -z)

        a.add(b)
        assertEquals(0.0, a.x)
        assertEquals(0.0, a.y)
        assertEquals(0.0, a.z)

        val c = Vector3().addVectors(b, b)
        assertEquals(-2 * x, c.x)
        assertEquals(-2 * y, c.y)
        assertEquals(-2 * z, c.z)
    }

    @Test
    fun addScaledVector() {
        val a = Vector3(x, y, z)
        val b = Vector3(2.0, 3.0, 4.0)
        val s = 3.0

        a.addScaledVector(b, s)
        assertEquals(x + b.x * s, a.x, 0.0, "Check x")
        assertEquals(y + b.y * s, a.y, 0.0, "Check y")
        assertEquals(z + b.z * s, a.z, 0.0, "Check z")
    }

    @Test
    fun sub() {
        val a = Vector3(x, y, z)
        val b = Vector3(-x, -y, -z)

        a.sub(b)
        assertEquals(2 * x, a.x)
        assertEquals(2 * y, a.y)
        assertEquals(2 * z, a.z)

        val c = Vector3().subVectors(a, a)
        assertEquals(0.0, c.x)
        assertEquals(0.0, c.y)
        assertEquals(0.0, c.z)
    }

    @Test
    fun multiplyVectors() {
        val a = Vector3(x, y, z)
        val b = Vector3(2.0, 3.0, -5.0)

        val c = Vector3().multiplyVectors(a, b)
        assertEquals(x * 2, c.x, 0.0, "Check x")
        assertEquals(y * 3, c.y, 0.0, "Check y")
        assertEquals(z * -5, c.z, 0.0, "Check z")
    }

    @Test
    fun applyEuler() {
        val a = Vector3(x, y, z)
        val euler = Euler(90.0, -45.0, 0.0)
        val expected = Vector3(-2.352970120501014, -4.7441750936226645, 0.9779234597246458)

        a.applyEuler(euler)
        assertTrue(abs(a.x - expected.x) <= eps, "Check x")
        assertTrue(abs(a.y - expected.y) <= eps, "Check y")
        assertTrue(abs(a.z - expected.z) <= eps, "Check z")
    }

    @Test
    fun applyAxisAngle() {
        val a = Vector3(x, y, z)
        val axis = Vector3(0.0, 1.0, 0.0)
        val angle = PI / 4.0
        val expected = Vector3(3 * sqrt(2.0), 3.0, sqrt(2.0))

        a.applyAxisAngle(axis, angle)
        assertTrue(abs(a.x - expected.x) <= eps, "Check x")
        assertTrue(abs(a.y - expected.y) <= eps, "Check y")
        assertTrue(abs(a.z - expected.z) <= eps, "Check z")
    }

    @Test
    fun applyMatrix3() {
        val a = Vector3(x, y, z)
        val m = Matrix3().set(2.0, 3.0, 5.0, 7.0, 11.0, 13.0, 17.0, 19.0, 23.0)

        a.applyMatrix3(m)
        assertEquals(33.0, a.x, 0.0, "Check x")
        assertEquals(99.0, a.y, 0.0, "Check y")
        assertEquals(183.0, a.z, 0.0, "Check z")
    }

    @Test
    fun applyMatrix4() {
        val a = Vector3(x, y, z)
        val b = Vector4(x, y, z, 1.0)

        var m: Matrix4 = Matrix4().makeRotationX(PI)
        a.applyMatrix4(m)
        b.applyMatrix4(m)
        assertEquals(b.x / b.w, a.x)
        assertEquals(b.y / b.w, a.y)
        assertEquals(b.z / b.w, a.z)

        m = Matrix4().makeTranslation(3.0, 2.0, 1.0)
        a.applyMatrix4(m)
        b.applyMatrix4(m)
        assertEquals(b.x / b.w, a.x)
        assertEquals(b.y / b.w, a.y)
        assertEquals(b.z / b.w, a.z)

        m = Matrix4().set(
            1.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 1.0, 0.0,
        )
        a.applyMatrix4(m)
        b.applyMatrix4(m)
        assertEquals(b.x / b.w, a.x)
        assertEquals(b.y / b.w, a.y)
        assertEquals(b.z / b.w, a.z)
    }

    @Test
    fun applyQuaternion() {
        val a = Vector3(x, y, z)

        a.applyQuaternion(Quaternion())
        assertEquals(x, a.x, 0.0, "Identity rotation: check x")
        assertEquals(y, a.y, 0.0, "Identity rotation: check y")
        assertEquals(z, a.z, 0.0, "Identity rotation: check z")
    }

    @Test
    fun transformDirection() {
        val a = Vector3(x, y, z)
        val m = Matrix4()
        val transformed = Vector3(0.3713906763541037, 0.5570860145311556, 0.7427813527082074)

        a.transformDirection(m)
        assertTrue(abs(a.x - transformed.x) <= eps, "Check x")
        assertTrue(abs(a.y - transformed.y) <= eps, "Check y")
        assertTrue(abs(a.z - transformed.z) <= eps, "Check z")
    }

    @Test
    fun clampScalar() {
        val a = Vector3(-0.01, 0.5, 1.5)
        val clamped = Vector3(0.1, 0.5, 1.0)

        a.clampScalar(0.1, 1.0)
        assertTrue(abs(a.x - clamped.x) <= 0.001, "Check x")
        assertTrue(abs(a.y - clamped.y) <= 0.001, "Check y")
        assertTrue(abs(a.z - clamped.z) <= 0.001, "Check z")
    }

    @Test
    fun negate() {
        val a = Vector3(x, y, z)

        a.negate()
        assertEquals(-x, a.x)
        assertEquals(-y, a.y)
        assertEquals(-z, a.z)
    }

    @Test
    fun dot() {
        val a = Vector3(x, y, z)
        val b = Vector3(-x, -y, -z)
        val c = Vector3()

        var result = a.dot(b)
        assertEquals(-x * x - y * y - z * z, result)

        result = a.dot(c)
        assertEquals(0.0, result)
    }

    @Test
    fun manhattanLength() {
        val a = Vector3(x, 0.0, 0.0)
        val b = Vector3(0.0, -y, 0.0)
        val c = Vector3(0.0, 0.0, z)
        val d = Vector3()

        assertEquals(x, a.manhattanLength(), 0.0, "Positive x")
        assertEquals(y, b.manhattanLength(), 0.0, "Negative y")
        assertEquals(z, c.manhattanLength(), 0.0, "Positive z")
        assertEquals(0.0, d.manhattanLength(), 0.0, "Empty initialization")

        a.set(x, y, z)
        assertEquals(abs(x) + abs(y) + abs(z), a.manhattanLength(), 0.0, "All components")
    }

    @Test
    fun normalize() {
        val a = Vector3(x, 0.0, 0.0)
        val b = Vector3(0.0, -y, 0.0)
        val c = Vector3(0.0, 0.0, z)

        a.normalize()
        assertEquals(1.0, a.length())
        assertEquals(1.0, a.x)

        b.normalize()
        assertEquals(1.0, b.length())
        assertEquals(-1.0, b.y)

        c.normalize()
        assertEquals(1.0, c.length())
        assertEquals(1.0, c.z)
    }

    @Test
    fun setLength() {
        var a = Vector3(x, 0.0, 0.0)

        assertEquals(x, a.length())
        a.setLength(y)
        assertEquals(y, a.length())

        a = Vector3(0.0, 0.0, 0.0)
        assertEquals(0.0, a.length())
        a.setLength(y)
        assertEquals(0.0, a.length())
        // JS also asserts setLength() with a missing arg yields NaN; that is a JS
        // arity quirk with no Kotlin equivalent (length is a required Double).
    }

    @Test
    fun cross() {
        val a = Vector3(x, y, z)
        val b = Vector3(2 * x, -y, 0.5 * z)
        val crossed = Vector3(18.0, 12.0, -18.0)

        a.cross(b)
        assertTrue(abs(a.x - crossed.x) <= eps, "Check x")
        assertTrue(abs(a.y - crossed.y) <= eps, "Check y")
        assertTrue(abs(a.z - crossed.z) <= eps, "Check z")
    }

    @Test
    fun crossVectors() {
        val a = Vector3(x, y, z)
        val b = Vector3(x, -y, z)
        val c = Vector3()
        val crossed = Vector3(24.0, 0.0, -12.0)

        c.crossVectors(a, b)
        assertTrue(abs(c.x - crossed.x) <= eps, "Check x")
        assertTrue(abs(c.y - crossed.y) <= eps, "Check y")
        assertTrue(abs(c.z - crossed.z) <= eps, "Check z")
    }

    @Test
    fun projectOnVector() {
        val a = Vector3(1.0, 0.0, 0.0)
        val b = Vector3()
        val normal = Vector3(10.0, 0.0, 0.0)

        assertTrue(b.copy(a).projectOnVector(normal) == Vector3(1.0, 0.0, 0.0))

        a.set(0.0, 1.0, 0.0)
        assertTrue(b.copy(a).projectOnVector(normal) == Vector3(0.0, 0.0, 0.0))

        a.set(0.0, 0.0, -1.0)
        assertTrue(b.copy(a).projectOnVector(normal) == Vector3(0.0, 0.0, 0.0))

        a.set(-1.0, 0.0, 0.0)
        assertTrue(b.copy(a).projectOnVector(normal) == Vector3(-1.0, 0.0, 0.0))
    }

    @Test
    fun projectOnPlane() {
        val a = Vector3(1.0, 0.0, 0.0)
        val b = Vector3()
        val normal = Vector3(1.0, 0.0, 0.0)

        assertTrue(b.copy(a).projectOnPlane(normal) == Vector3(0.0, 0.0, 0.0))

        a.set(0.0, 1.0, 0.0)
        assertTrue(b.copy(a).projectOnPlane(normal) == Vector3(0.0, 1.0, 0.0))

        a.set(0.0, 0.0, -1.0)
        assertTrue(b.copy(a).projectOnPlane(normal) == Vector3(0.0, 0.0, -1.0))

        a.set(-1.0, 0.0, 0.0)
        assertTrue(b.copy(a).projectOnPlane(normal) == Vector3(0.0, 0.0, 0.0))
    }

    @Test
    fun reflect() {
        val a = Vector3()
        val normal = Vector3(0.0, 1.0, 0.0)
        val b = Vector3()

        a.set(0.0, -1.0, 0.0)
        assertTrue(b.copy(a).reflect(normal) == Vector3(0.0, 1.0, 0.0))

        a.set(1.0, -1.0, 0.0)
        assertTrue(b.copy(a).reflect(normal) == Vector3(1.0, 1.0, 0.0))

        a.set(1.0, -1.0, 0.0)
        normal.set(0.0, -1.0, 0.0)
        assertTrue(b.copy(a).reflect(normal) == Vector3(1.0, 1.0, 0.0))
    }

    @Test
    fun angleTo() {
        val a = Vector3(0.0, -0.18851655680720186, 0.9820700116639124)
        val b = Vector3(0.0, 0.18851655680720186, -0.9820700116639124)

        assertEquals(0.0, a.angleTo(a), 1e-12)
        assertEquals(PI, a.angleTo(b), 1e-12)

        val vx = Vector3(1.0, 0.0, 0.0)
        val vy = Vector3(0.0, 1.0, 0.0)
        val vz = Vector3(0.0, 0.0, 1.0)

        assertEquals(PI / 2, vx.angleTo(vy), 1e-12)
        assertEquals(PI / 2, vx.angleTo(vz), 1e-12)
        assertEquals(PI / 2, vz.angleTo(vx), 1e-12)

        assertTrue(abs(vx.angleTo(Vector3(1.0, 1.0, 0.0)) - PI / 4) < 0.0000001)
    }

    @Test
    fun setFromSpherical() {
        val a = Vector3()
        val phi = acos(-0.5)
        val theta = sqrt(PI) * phi
        val sph = Spherical(10.0, phi, theta)
        val expected = Vector3(-4.677914006701843, -5.0, -7.288149322420796)

        a.setFromSpherical(sph)
        assertTrue(abs(a.x - expected.x) <= eps, "Check x")
        assertTrue(abs(a.y - expected.y) <= eps, "Check y")
        assertTrue(abs(a.z - expected.z) <= eps, "Check z")
    }

    @Test
    fun setFromCylindrical() {
        val a = Vector3()
        val cyl = Cylindrical(10.0, PI * 0.125, 20.0)
        val expected = Vector3(3.826834323650898, 20.0, 9.238795325112868)

        a.setFromCylindrical(cyl)
        assertTrue(abs(a.x - expected.x) <= eps, "Check x")
        assertTrue(abs(a.y - expected.y) <= eps, "Check y")
        assertTrue(abs(a.z - expected.z) <= eps, "Check z")
    }

    @Test
    fun setFromMatrixPosition() {
        val a = Vector3()
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
    }

    @Test
    fun setFromMatrixScale() {
        val a = Vector3()
        val m = Matrix4().set(
            2.0, 3.0, 5.0, 7.0,
            11.0, 13.0, 17.0, 19.0,
            23.0, 29.0, 31.0, 37.0,
            41.0, 43.0, 47.0, 53.0,
        )
        val expected = Vector3(25.573423705088842, 31.921779399024736, 35.70714214271425)

        a.setFromMatrixScale(m)
        assertTrue(abs(a.x - expected.x) <= eps, "Check x")
        assertTrue(abs(a.y - expected.y) <= eps, "Check y")
        assertTrue(abs(a.z - expected.z) <= eps, "Check z")
    }

    @Test
    fun setFromMatrixColumn() {
        val a = Vector3()
        val m = Matrix4().set(
            2.0, 3.0, 5.0, 7.0,
            11.0, 13.0, 17.0, 19.0,
            23.0, 29.0, 31.0, 37.0,
            41.0, 43.0, 47.0, 53.0,
        )

        a.setFromMatrixColumn(m, 0)
        assertEquals(2.0, a.x, 0.0, "Index 0: check x")
        assertEquals(11.0, a.y, 0.0, "Index 0: check y")
        assertEquals(23.0, a.z, 0.0, "Index 0: check z")

        a.setFromMatrixColumn(m, 2)
        assertEquals(5.0, a.x, 0.0, "Index 2: check x")
        assertEquals(17.0, a.y, 0.0, "Index 2: check y")
        assertEquals(31.0, a.z, 0.0, "Index 2: check z")
    }

    @Test
    fun setFromColor() {
        // Upstream Vector3.tests.js has no `setFromColor` case; this mirrors the
        // method (reads c.r/c.g/c.b into x/y/z). Color(x,y,z) stores components in
        // the working color space, which equals the default LinearSRGB input space,
        // so no conversion occurs and r/g/b are exactly x/y/z.
        val a = Vector3()
        val c = Color(x, y, z)

        a.setFromColor(c)
        assertEquals(c.r, a.x, 0.0, "Check x")
        assertEquals(c.g, a.y, 0.0, "Check y")
        assertEquals(c.b, a.z, 0.0, "Check z")
        assertEquals(x, a.x, 0.0, "Check x value")
        assertEquals(y, a.y, 0.0, "Check y value")
        assertEquals(z, a.z, 0.0, "Check z value")
    }

    @Test
    fun equalsTest() {
        val a = Vector3(x, 0.0, z)
        val b = Vector3(0.0, -y, 0.0)

        assertTrue(a.x != b.x)
        assertTrue(a.y != b.y)
        assertTrue(a.z != b.z)

        assertFalse(a == b)
        assertFalse(b == a)

        a.copy(b)
        assertEquals(b.x, a.x)
        assertEquals(b.y, a.y)
        assertEquals(b.z, a.z)

        assertTrue(a == b)
        assertTrue(b == a)
    }

    @Test
    fun fromArray() {
        val a = Vector3()
        val array = doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)

        a.fromArray(array)
        assertEquals(1.0, a.x, 0.0, "No offset: check x")
        assertEquals(2.0, a.y, 0.0, "No offset: check y")
        assertEquals(3.0, a.z, 0.0, "No offset: check z")

        a.fromArray(array, 3)
        assertEquals(4.0, a.x, 0.0, "With offset: check x")
        assertEquals(5.0, a.y, 0.0, "With offset: check y")
        assertEquals(6.0, a.z, 0.0, "With offset: check z")
    }

    @Test
    fun toArray() {
        val a = Vector3(x, y, z)

        val array1 = a.toArray()
        assertEquals(x, array1[0], 0.0, "No array, no offset: check x")
        assertEquals(y, array1[1], 0.0, "No array, no offset: check y")
        assertEquals(z, array1[2], 0.0, "No array, no offset: check z")

        val array2 = mutableListOf<Double>()
        a.toArray(array2)
        assertEquals(x, array2[0], 0.0, "With array, no offset: check x")
        assertEquals(y, array2[1], 0.0, "With array, no offset: check y")
        assertEquals(z, array2[2], 0.0, "With array, no offset: check z")

        val array3 = mutableListOf<Double>()
        a.toArray(array3, 1)
        // Kotlin lists have no holes: the gap at [0] is filled with 0.0 (three.js
        // leaves it undefined).
        assertEquals(0.0, array3[0], 0.0, "With array and offset: gap filled with 0")
        assertEquals(x, array3[1], 0.0, "With array and offset: check x")
        assertEquals(y, array3[2], 0.0, "With array and offset: check y")
        assertEquals(z, array3[3], 0.0, "With array and offset: check z")
    }

    @Test
    fun fromBufferAttribute() {
        val a = Vector3()
        val attr = TestBufferAttribute(doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0), 3)

        a.fromBufferAttribute(attr, 0)
        assertEquals(1.0, a.x, 0.0, "Offset 0: check x")
        assertEquals(2.0, a.y, 0.0, "Offset 0: check y")
        assertEquals(3.0, a.z, 0.0, "Offset 0: check z")

        a.fromBufferAttribute(attr, 1)
        assertEquals(4.0, a.x, 0.0, "Offset 1: check x")
        assertEquals(5.0, a.y, 0.0, "Offset 1: check y")
        assertEquals(6.0, a.z, 0.0, "Offset 1: check z")
    }

    @Test
    fun randomDirection() {
        val vec = Vector3()

        vec.randomDirection()

        val zero = Vector3()
        assertFalse(vec == zero, "randomizes at least one component of the vector")

        assertTrue((1 - vec.length()) <= MathUtils.EPSILON, "produces a unit vector")
    }

    @Test
    fun setXsetYsetZ() {
        val a = Vector3()
        assertEquals(0.0, a.x)
        assertEquals(0.0, a.y)
        assertEquals(0.0, a.z)

        a.setX(x)
        a.setY(y)
        a.setZ(z)

        assertEquals(x, a.x)
        assertEquals(y, a.y)
        assertEquals(z, a.z)
    }

    @Test
    fun setComponentGetComponent() {
        val a = Vector3()
        assertEquals(0.0, a.x)
        assertEquals(0.0, a.y)
        assertEquals(0.0, a.z)

        a.setComponent(0, 1.0)
        a.setComponent(1, 2.0)
        a.setComponent(2, 3.0)
        assertEquals(1.0, a.getComponent(0))
        assertEquals(2.0, a.getComponent(1))
        assertEquals(3.0, a.getComponent(2))
    }

    @Test
    fun setComponentGetComponentExceptions() {
        val a = Vector3()
        assertFailsWith<IllegalArgumentException> { a.setComponent(3, 0.0) }
        assertFailsWith<IllegalArgumentException> { a.getComponent(3) }
    }

    @Test
    fun minMaxClamp() {
        val a = Vector3(x, y, z)
        val b = Vector3(-x, -y, -z)
        val c = Vector3()

        c.copy(a).min(b)
        assertEquals(-x, c.x)
        assertEquals(-y, c.y)
        assertEquals(-z, c.z)

        c.copy(a).max(b)
        assertEquals(x, c.x)
        assertEquals(y, c.y)
        assertEquals(z, c.z)

        c.set(-2 * x, 2 * y, -2 * z)
        c.clamp(b, a)
        assertEquals(-x, c.x)
        assertEquals(y, c.y)
        assertEquals(-z, c.z)
    }

    @Test
    fun distanceToDistanceToSquared() {
        val a = Vector3(x, 0.0, 0.0)
        val b = Vector3(0.0, -y, 0.0)
        val c = Vector3(0.0, 0.0, z)
        val d = Vector3()

        assertEquals(x, a.distanceTo(d))
        assertEquals(x * x, a.distanceToSquared(d))

        assertEquals(y, b.distanceTo(d))
        assertEquals(y * y, b.distanceToSquared(d))

        assertEquals(z, c.distanceTo(d))
        assertEquals(z * z, c.distanceToSquared(d))
    }

    @Test
    fun setScalarAddScalarSubScalar() {
        val a = Vector3()
        val s = 3.0

        a.setScalar(s)
        assertEquals(s, a.x, 0.0, "setScalar: check x")
        assertEquals(s, a.y, 0.0, "setScalar: check y")
        assertEquals(s, a.z, 0.0, "setScalar: check z")

        a.addScalar(s)
        assertEquals(2 * s, a.x, 0.0, "addScalar: check x")
        assertEquals(2 * s, a.y, 0.0, "addScalar: check y")
        assertEquals(2 * s, a.z, 0.0, "addScalar: check z")

        a.subScalar(2 * s)
        assertEquals(0.0, a.x, 0.0, "subScalar: check x")
        assertEquals(0.0, a.y, 0.0, "subScalar: check y")
        assertEquals(0.0, a.z, 0.0, "subScalar: check z")
    }

    @Test
    fun multiplyDivide() {
        val a = Vector3(x, y, z)
        val b = Vector3(2 * x, 2 * y, 2 * z)
        val c = Vector3(4 * x, 4 * y, 4 * z)

        a.multiply(b)
        assertEquals(x * b.x, a.x, 0.0, "multiply: check x")
        assertEquals(y * b.y, a.y, 0.0, "multiply: check y")
        assertEquals(z * b.z, a.z, 0.0, "multiply: check z")

        b.divide(c)
        assertTrue(abs(b.x - 0.5) <= eps, "divide: check x")
        assertTrue(abs(b.y - 0.5) <= eps, "divide: check y")
        assertTrue(abs(b.z - 0.5) <= eps, "divide: check z")
    }

    @Test
    fun multiplyScalarDivideScalar() {
        val a = Vector3(x, y, z)
        val b = Vector3(-x, -y, -z)

        a.multiplyScalar(-2.0)
        assertEquals(x * -2, a.x)
        assertEquals(y * -2, a.y)
        assertEquals(z * -2, a.z)

        b.multiplyScalar(-2.0)
        assertEquals(2 * x, b.x)
        assertEquals(2 * y, b.y)
        assertEquals(2 * z, b.z)

        a.divideScalar(-2.0)
        assertEquals(x, a.x)
        assertEquals(y, a.y)
        assertEquals(z, a.z)

        b.divideScalar(-2.0)
        assertEquals(-x, b.x)
        assertEquals(-y, b.y)
        assertEquals(-z, b.z)
    }

    // project/unproject need a core Camera type, which this module does not
    // define. Test skipped.

    @Test
    fun lengthLengthSq() {
        val a = Vector3(x, 0.0, 0.0)
        val b = Vector3(0.0, -y, 0.0)
        val c = Vector3(0.0, 0.0, z)
        val d = Vector3()

        assertEquals(x, a.length())
        assertEquals(x * x, a.lengthSq())
        assertEquals(y, b.length())
        assertEquals(y * y, b.lengthSq())
        assertEquals(z, c.length())
        assertEquals(z * z, c.lengthSq())
        assertEquals(0.0, d.length())
        assertEquals(0.0, d.lengthSq())

        a.set(x, y, z)
        assertEquals(sqrt(x * x + y * y + z * z), a.length())
        assertEquals(x * x + y * y + z * z, a.lengthSq())
    }

    @Test
    fun lerpClone() {
        val a = Vector3(x, 0.0, z)
        val b = Vector3(0.0, -y, 0.0)

        assertTrue(a.clone().lerp(a, 0.0) == a.clone().lerp(a, 0.5))
        assertTrue(a.clone().lerp(a, 0.0) == a.clone().lerp(a, 1.0))

        assertTrue(a.clone().lerp(b, 0.0) == a)

        assertEquals(x * 0.5, a.clone().lerp(b, 0.5).x)
        assertEquals(-y * 0.5, a.clone().lerp(b, 0.5).y)
        assertEquals(z * 0.5, a.clone().lerp(b, 0.5).z)

        assertTrue(a.clone().lerp(b, 1.0) == b)
    }

    @Test
    fun iterable() {
        val v = Vector3(0.0, 0.5, 1.0)
        val array = v.toList()
        assertEquals(0.0, array[0], 0.0, "Vector3 is iterable")
        assertEquals(0.5, array[1], 0.0, "Vector3 is iterable")
        assertEquals(1.0, array[2], 0.0, "Vector3 is iterable")
    }
}
