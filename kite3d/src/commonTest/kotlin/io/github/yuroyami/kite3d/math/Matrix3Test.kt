/*
 * Copyright © 2026 yuroyami. MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Matrix3.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Matrix3Test {

    // Mirrors the JS `matrixEquals3` tolerance helper (default tolerance 0.0001).
    // The JS helper compares `.elements` of any equal length, so it is also applied
    // to Matrix4 pairs (the invert test). These overloads cover both.
    private fun matrixEquals3(a: Matrix3, b: Matrix3, tolerance: Double = 0.0001): Boolean =
        elementsEqual(a.elements, b.elements, tolerance)

    private fun matrixEquals3(a: Matrix4, b: Matrix4, tolerance: Double = 0.0001): Boolean =
        elementsEqual(a.elements, b.elements, tolerance)

    private fun elementsEqual(ae: DoubleArray, be: DoubleArray, tolerance: Double): Boolean {
        if (ae.size != be.size) return false
        for (i in ae.indices) {
            val delta = abs(ae[i] - be[i])
            if (delta > tolerance) return false
        }
        return true
    }

    // Mirrors the JS `toMatrix4` helper: embeds a Matrix3 into the upper-left of a
    // Matrix4.
    private fun toMatrix4(m3: Matrix3): Matrix4 {
        val result = Matrix4()
        val re = result.elements
        val me = m3.elements
        re[0] = me[0]
        re[1] = me[1]
        re[2] = me[2]
        re[4] = me[3]
        re[5] = me[4]
        re[6] = me[5]
        re[8] = me[6]
        re[9] = me[7]
        re[10] = me[8]
        return result
    }

    @Test
    fun instancing() {
        val a = Matrix3()
        assertTrue(a.determinant() == 1.0, "Passed!")

        // three.js also tests the row-major element constructor `new Matrix3(...)`;
        // that convenience lives in ext/ (dialect rule 3), so here we exercise
        // set(...) which the constructor delegates to.
        val b = Matrix3().set(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
        assertTrue(b.elements[0] == 0.0)
        assertTrue(b.elements[1] == 3.0)
        assertTrue(b.elements[2] == 6.0)
        assertTrue(b.elements[3] == 1.0)
        assertTrue(b.elements[4] == 4.0)
        assertTrue(b.elements[5] == 7.0)
        assertTrue(b.elements[6] == 2.0)
        assertTrue(b.elements[7] == 5.0)
        assertTrue(b.elements[8] == 8.0)

        assertFalse(matrixEquals3(a, b), "Passed!")
    }

    // three.js's `isMatrix3` duck-typing flag is intentionally dropped (dialect rule
    // 11): type identity is expressed with Kotlin's `is Matrix3`, which needs no test.

    @Test
    fun set() {
        val b = Matrix3()
        assertTrue(b.determinant() == 1.0, "Passed!")

        b.set(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
        assertTrue(b.elements[0] == 0.0)
        assertTrue(b.elements[1] == 3.0)
        assertTrue(b.elements[2] == 6.0)
        assertTrue(b.elements[3] == 1.0)
        assertTrue(b.elements[4] == 4.0)
        assertTrue(b.elements[5] == 7.0)
        assertTrue(b.elements[6] == 2.0)
        assertTrue(b.elements[7] == 5.0)
        assertTrue(b.elements[8] == 8.0)
    }

    @Test
    fun identity() {
        val b = Matrix3().set(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
        assertTrue(b.elements[0] == 0.0)
        assertTrue(b.elements[1] == 3.0)
        assertTrue(b.elements[2] == 6.0)
        assertTrue(b.elements[3] == 1.0)
        assertTrue(b.elements[4] == 4.0)
        assertTrue(b.elements[5] == 7.0)
        assertTrue(b.elements[6] == 2.0)
        assertTrue(b.elements[7] == 5.0)
        assertTrue(b.elements[8] == 8.0)

        val a = Matrix3()
        assertFalse(matrixEquals3(a, b), "Passed!")

        b.identity()
        assertTrue(matrixEquals3(a, b), "Passed!")
    }

    @Test
    fun clone() {
        val a = Matrix3().set(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
        val b = a.clone()

        assertTrue(matrixEquals3(a, b), "Passed!")

        // ensure that it is a true copy
        a.elements[0] = 2.0
        assertFalse(matrixEquals3(a, b), "Passed!")
    }

    @Test
    fun copy() {
        val a = Matrix3().set(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
        val b = Matrix3().copy(a)

        assertTrue(matrixEquals3(a, b), "Passed!")

        // ensure that it is a true copy
        a.elements[0] = 2.0
        assertFalse(matrixEquals3(a, b), "Passed!")
    }

    @Test
    fun setFromMatrix4() {
        val a = Matrix4().set(
            0.0, 1.0, 2.0, 3.0,
            4.0, 5.0, 6.0, 7.0,
            8.0, 9.0, 10.0, 11.0,
            12.0, 13.0, 14.0, 15.0,
        )
        val b = Matrix3()
        val c = Matrix3().set(0.0, 1.0, 2.0, 4.0, 5.0, 6.0, 8.0, 9.0, 10.0)
        b.setFromMatrix4(a)
        assertTrue(b == c)
    }

    @Test
    fun multiplyPremultiply() {
        // both simply just wrap multiplyMatrices
        val a = Matrix3().set(2.0, 3.0, 5.0, 7.0, 11.0, 13.0, 17.0, 19.0, 23.0)
        val b = Matrix3().set(29.0, 31.0, 37.0, 41.0, 43.0, 47.0, 53.0, 59.0, 61.0)
        val expectedMultiply = doubleArrayOf(446.0, 1343.0, 2491.0, 486.0, 1457.0, 2701.0, 520.0, 1569.0, 2925.0)
        val expectedPremultiply = doubleArrayOf(904.0, 1182.0, 1556.0, 1131.0, 1489.0, 1967.0, 1399.0, 1845.0, 2435.0)

        a.multiply(b)
        assertTrue(a.elements.contentEquals(expectedMultiply), "multiply: check result")

        a.set(2.0, 3.0, 5.0, 7.0, 11.0, 13.0, 17.0, 19.0, 23.0)
        a.premultiply(b)
        assertTrue(a.elements.contentEquals(expectedPremultiply), "premultiply: check result")
    }

    @Test
    fun multiplyMatrices() {
        // Reference (numpy dot product), see three.js test comment:
        // [[ 446  486  520]
        //  [1343 1457 1569]
        //  [2491 2701 2925]]
        val lhs = Matrix3().set(2.0, 3.0, 5.0, 7.0, 11.0, 13.0, 17.0, 19.0, 23.0)
        val rhs = Matrix3().set(29.0, 31.0, 37.0, 41.0, 43.0, 47.0, 53.0, 59.0, 61.0)
        val ans = Matrix3()

        ans.multiplyMatrices(lhs, rhs)

        assertTrue(ans.elements[0] == 446.0)
        assertTrue(ans.elements[1] == 1343.0)
        assertTrue(ans.elements[2] == 2491.0)
        assertTrue(ans.elements[3] == 486.0)
        assertTrue(ans.elements[4] == 1457.0)
        assertTrue(ans.elements[5] == 2701.0)
        assertTrue(ans.elements[6] == 520.0)
        assertTrue(ans.elements[7] == 1569.0)
        assertTrue(ans.elements[8] == 2925.0)
    }

    @Test
    fun multiplyScalar() {
        val b = Matrix3().set(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
        assertTrue(b.elements[0] == 0.0)
        assertTrue(b.elements[1] == 3.0)
        assertTrue(b.elements[2] == 6.0)
        assertTrue(b.elements[3] == 1.0)
        assertTrue(b.elements[4] == 4.0)
        assertTrue(b.elements[5] == 7.0)
        assertTrue(b.elements[6] == 2.0)
        assertTrue(b.elements[7] == 5.0)
        assertTrue(b.elements[8] == 8.0)

        b.multiplyScalar(2.0)
        assertTrue(b.elements[0] == 0.0 * 2.0)
        assertTrue(b.elements[1] == 3.0 * 2.0)
        assertTrue(b.elements[2] == 6.0 * 2.0)
        assertTrue(b.elements[3] == 1.0 * 2.0)
        assertTrue(b.elements[4] == 4.0 * 2.0)
        assertTrue(b.elements[5] == 7.0 * 2.0)
        assertTrue(b.elements[6] == 2.0 * 2.0)
        assertTrue(b.elements[7] == 5.0 * 2.0)
        assertTrue(b.elements[8] == 8.0 * 2.0)
    }

    @Test
    fun determinant() {
        val a = Matrix3()
        assertTrue(a.determinant() == 1.0, "Passed!")

        a.elements[0] = 2.0
        assertTrue(a.determinant() == 2.0, "Passed!")

        a.elements[0] = 0.0
        assertTrue(a.determinant() == 0.0, "Passed!")

        // calculated via http://www.euclideanspace.com/maths/algebra/matrix/functions/determinant/threeD/index.htm
        a.set(2.0, 3.0, 4.0, 5.0, 13.0, 7.0, 8.0, 9.0, 11.0)
        assertTrue(a.determinant() == -73.0, "Passed!")
    }

    @Test
    fun invert() {
        val zero = Matrix3().set(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        val identity4 = Matrix4()
        val a = Matrix3().set(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        val b = Matrix3()

        b.copy(a).invert()
        assertTrue(matrixEquals3(b, zero), "Matrix a is zero matrix")

        val testMatrices = listOf(
            Matrix4().makeRotationX(0.3),
            Matrix4().makeRotationX(-0.3),
            Matrix4().makeRotationY(0.3),
            Matrix4().makeRotationY(-0.3),
            Matrix4().makeRotationZ(0.3),
            Matrix4().makeRotationZ(-0.3),
            Matrix4().makeScale(1.0, 2.0, 3.0),
            Matrix4().makeScale(1.0 / 8.0, 1.0 / 2.0, 1.0 / 3.0),
        )

        for (m in testMatrices) {
            a.setFromMatrix4(m)
            val mInverse3 = b.copy(a).invert()

            val mInverse = toMatrix4(mInverse3)

            // the determinant of the inverse should be the reciprocal
            assertTrue(abs(a.determinant() * mInverse3.determinant() - 1.0) < 0.0001, "Passed!")
            assertTrue(abs(m.determinant() * mInverse.determinant() - 1.0) < 0.0001, "Passed!")

            val mProduct = Matrix4().multiplyMatrices(m, mInverse)
            assertTrue(abs(mProduct.determinant() - 1.0) < 0.0001, "Passed!")
            assertTrue(matrixEquals3(mProduct, identity4), "Passed!")
        }
    }

    @Test
    fun transpose() {
        val a = Matrix3()
        var b = a.clone().transpose()
        assertTrue(matrixEquals3(a, b), "Passed!")

        b = Matrix3().set(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
        val c = b.clone().transpose()
        assertFalse(matrixEquals3(b, c), "Passed!")
        c.transpose()
        assertTrue(matrixEquals3(b, c), "Passed!")
    }

    @Test
    fun getNormalMatrix() {
        val a = Matrix3()
        val b = Matrix4().set(
            2.0, 3.0, 5.0, 7.0,
            11.0, 13.0, 17.0, 19.0,
            23.0, 29.0, 31.0, 37.0,
            41.0, 43.0, 47.0, 57.0,
        )
        val expected = Matrix3().set(
            -1.2857142857142856, 0.7142857142857143, 0.2857142857142857,
            0.7428571428571429, -0.7571428571428571, 0.15714285714285714,
            -0.19999999999999998, 0.3, -0.09999999999999999,
        )

        a.getNormalMatrix(b)
        assertTrue(matrixEquals3(a, expected), "Check resulting Matrix3")
    }

    @Test
    fun transposeIntoArray() {
        val a = Matrix3().set(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
        val b = DoubleArray(9)
        a.transposeIntoArray(b)

        assertTrue(b[0] == 0.0)
        assertTrue(b[1] == 1.0)
        assertTrue(b[2] == 2.0)
        assertTrue(b[3] == 3.0)
        assertTrue(b[4] == 4.0)
        assertTrue(b[5] == 5.0)
        assertTrue(b[6] == 6.0)
        assertTrue(b[7] == 7.0)
        assertTrue(b[8] == 8.0)
    }

    @Test
    fun setUvTransform() {
        val a = Matrix3().set(
            0.1767766952966369, 0.17677669529663687, 0.32322330470336313,
            -0.17677669529663687, 0.1767766952966369, 0.5,
            0.0, 0.0, 1.0,
        )
        val b = Matrix3()
        val centerX = 0.5
        val centerY = 0.5
        val offsetX = 0.0
        val offsetY = 0.0
        val repeatX = 0.25
        val repeatY = 0.25
        val rotation = 0.7753981633974483

        val expected = Matrix3().set(
            0.1785355940258599, 0.17500011904519763, 0.32323214346447127,
            -0.17500011904519763, 0.1785355940258599, 0.4982322625096689,
            0.0, 0.0, 1.0,
        )

        a.setUvTransform(
            offsetX, offsetY,
            repeatX, repeatY,
            rotation,
            centerX, centerY,
        )

        b.identity()
            .translate(-centerX, -centerY)
            .rotate(rotation)
            .scale(repeatX, repeatY)
            .translate(centerX, centerY)
            .translate(offsetX, offsetY)

        assertTrue(matrixEquals3(a, expected), "Check direct method")
        assertTrue(matrixEquals3(b, expected), "Check indirect method")
    }

    @Test
    fun scale() {
        val a = Matrix3().set(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0)
        val expected = Matrix3().set(
            0.25, 0.5, 0.75,
            1.0, 1.25, 1.5,
            7.0, 8.0, 9.0,
        )

        a.scale(0.25, 0.25)
        assertTrue(matrixEquals3(a, expected), "Check scaling result")
    }

    @Test
    fun rotate() {
        val a = Matrix3().set(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0)
        val expected = Matrix3().set(
            3.5355339059327373, 4.949747468305833, 6.363961030678928,
            2.121320343559643, 2.121320343559643, 2.1213203435596433,
            7.0, 8.0, 9.0,
        )

        a.rotate(PI / 4)
        assertTrue(matrixEquals3(a, expected), "Check rotated result")
    }

    @Test
    fun translate() {
        val a = Matrix3().set(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0)
        val expected = Matrix3().set(22.0, 26.0, 30.0, 53.0, 61.0, 69.0, 7.0, 8.0, 9.0)

        a.translate(3.0, 7.0)
        assertTrue(matrixEquals3(a, expected), "Check translation result")
    }

    @Test
    fun makeTranslation() {
        val a = Matrix3()
        val b = Vector2(1.0, 2.0)
        val c = Matrix3().set(1.0, 0.0, 1.0, 0.0, 1.0, 2.0, 0.0, 0.0, 1.0)

        a.makeTranslation(b.x, b.y)
        assertTrue(matrixEquals3(a, c), "Check translation result")

        a.makeTranslation(b)
        assertTrue(matrixEquals3(a, c), "Check translation result")
    }

    @Test
    fun equalsTest() {
        val a = Matrix3().set(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
        val b = Matrix3().set(0.0, -1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)

        assertFalse(a == b, "Check that a does not equal b")
        assertFalse(b == a, "Check that b does not equal a")

        a.copy(b)
        assertTrue(a == b, "Check that a equals b after copy()")
        assertTrue(b == a, "Check that b equals a after copy()")
    }

    @Test
    fun fromArray() {
        var b = Matrix3()
        b.fromArray(doubleArrayOf(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0))

        assertTrue(b.elements[0] == 0.0)
        assertTrue(b.elements[1] == 1.0)
        assertTrue(b.elements[2] == 2.0)
        assertTrue(b.elements[3] == 3.0)
        assertTrue(b.elements[4] == 4.0)
        assertTrue(b.elements[5] == 5.0)
        assertTrue(b.elements[6] == 6.0)
        assertTrue(b.elements[7] == 7.0)
        assertTrue(b.elements[8] == 8.0)

        b = Matrix3()
        b.fromArray(
            doubleArrayOf(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0),
            10,
        )

        assertTrue(b.elements[0] == 10.0)
        assertTrue(b.elements[1] == 11.0)
        assertTrue(b.elements[2] == 12.0)
        assertTrue(b.elements[3] == 13.0)
        assertTrue(b.elements[4] == 14.0)
        assertTrue(b.elements[5] == 15.0)
        assertTrue(b.elements[6] == 16.0)
        assertTrue(b.elements[7] == 17.0)
        assertTrue(b.elements[8] == 18.0)
    }

    @Test
    fun toArray() {
        val a = Matrix3().set(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0)
        val noOffset = listOf(1.0, 4.0, 7.0, 2.0, 5.0, 8.0, 3.0, 6.0, 9.0)
        // three.js leaves index 0 undefined; Kotlin MutableList fills the gap with 0.0.
        val withOffset = listOf(0.0, 1.0, 4.0, 7.0, 2.0, 5.0, 8.0, 3.0, 6.0, 9.0)

        var array = a.toArray()
        assertTrue(array == noOffset, "No array, no offset")

        array = mutableListOf()
        a.toArray(array)
        assertTrue(array == noOffset, "With array, no offset")

        array = mutableListOf()
        a.toArray(array, 1)
        assertTrue(array == withOffset, "With array, with offset")
    }
}
