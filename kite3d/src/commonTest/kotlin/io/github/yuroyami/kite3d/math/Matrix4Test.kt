/*
 * Copyright © 2026 yuroyami — MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Matrix4.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Mirrors the JS `matrixEquals4` tolerance helper (default tolerance 0.0001).
// Top-level `internal` so both Matrix3Test and Matrix4Test can use it.
internal fun matrixEquals4(a: Matrix4, b: Matrix4, tolerance: Double = 0.0001): Boolean {
    if (a.elements.size != b.elements.size) return false
    for (i in a.elements.indices) {
        val delta = abs(a.elements[i] - b.elements[i])
        if (delta > tolerance) return false
    }
    return true
}

class Matrix4Test {

    // from Euler.js — mirrors the JS `eulerEquals` helper.
    private fun eulerEquals(a: Euler, b: Euler, tolerance: Double = 0.0001): Boolean {
        val diff = abs(a.x - b.x) + abs(a.y - b.y) + abs(a.z - b.z)
        return diff < tolerance
    }

    @Test
    fun instancing() {
        val a = Matrix4()
        assertTrue(a.determinant() == 1.0, "Passed!")

        // three.js also tests the row-major element constructor `new Matrix4(...)`;
        // that convenience lives in ext/ (dialect rule 3), so here we exercise
        // set(...) which the constructor delegates to.
        val b = Matrix4().set(
            0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0,
        )
        assertTrue(b.elements[0] == 0.0)
        assertTrue(b.elements[1] == 4.0)
        assertTrue(b.elements[2] == 8.0)
        assertTrue(b.elements[3] == 12.0)
        assertTrue(b.elements[4] == 1.0)
        assertTrue(b.elements[5] == 5.0)
        assertTrue(b.elements[6] == 9.0)
        assertTrue(b.elements[7] == 13.0)
        assertTrue(b.elements[8] == 2.0)
        assertTrue(b.elements[9] == 6.0)
        assertTrue(b.elements[10] == 10.0)
        assertTrue(b.elements[11] == 14.0)
        assertTrue(b.elements[12] == 3.0)
        assertTrue(b.elements[13] == 7.0)
        assertTrue(b.elements[14] == 11.0)
        assertTrue(b.elements[15] == 15.0)

        assertFalse(matrixEquals4(a, b), "Passed!")
    }

    // three.js's `isMatrix4` duck-typing flag is intentionally dropped (dialect rule
    // 11): type identity is expressed with Kotlin's `is Matrix4`, which needs no test.

    @Test
    fun set() {
        val b = Matrix4()
        assertTrue(b.determinant() == 1.0, "Passed!")

        b.set(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0)
        assertTrue(b.elements[0] == 0.0)
        assertTrue(b.elements[1] == 4.0)
        assertTrue(b.elements[2] == 8.0)
        assertTrue(b.elements[3] == 12.0)
        assertTrue(b.elements[4] == 1.0)
        assertTrue(b.elements[5] == 5.0)
        assertTrue(b.elements[6] == 9.0)
        assertTrue(b.elements[7] == 13.0)
        assertTrue(b.elements[8] == 2.0)
        assertTrue(b.elements[9] == 6.0)
        assertTrue(b.elements[10] == 10.0)
        assertTrue(b.elements[11] == 14.0)
        assertTrue(b.elements[12] == 3.0)
        assertTrue(b.elements[13] == 7.0)
        assertTrue(b.elements[14] == 11.0)
        assertTrue(b.elements[15] == 15.0)
    }

    @Test
    fun identity() {
        val b = Matrix4().set(
            0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0,
        )
        assertTrue(b.elements[0] == 0.0)
        assertTrue(b.elements[1] == 4.0)
        assertTrue(b.elements[2] == 8.0)
        assertTrue(b.elements[3] == 12.0)
        assertTrue(b.elements[4] == 1.0)
        assertTrue(b.elements[5] == 5.0)
        assertTrue(b.elements[6] == 9.0)
        assertTrue(b.elements[7] == 13.0)
        assertTrue(b.elements[8] == 2.0)
        assertTrue(b.elements[9] == 6.0)
        assertTrue(b.elements[10] == 10.0)
        assertTrue(b.elements[11] == 14.0)
        assertTrue(b.elements[12] == 3.0)
        assertTrue(b.elements[13] == 7.0)
        assertTrue(b.elements[14] == 11.0)
        assertTrue(b.elements[15] == 15.0)

        val a = Matrix4()
        assertFalse(matrixEquals4(a, b), "Passed!")

        b.identity()
        assertTrue(matrixEquals4(a, b), "Passed!")
    }

    @Test
    fun clone() {
        val a = Matrix4().set(
            0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0,
        )
        val b = a.clone()

        assertTrue(matrixEquals4(a, b), "Passed!")

        // ensure that it is a true copy
        a.elements[0] = 2.0
        assertFalse(matrixEquals4(a, b), "Passed!")
    }

    @Test
    fun copy() {
        val a = Matrix4().set(
            0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0,
        )
        val b = Matrix4().copy(a)

        assertTrue(matrixEquals4(a, b), "Passed!")

        // ensure that it is a true copy
        a.elements[0] = 2.0
        assertFalse(matrixEquals4(a, b), "Passed!")
    }

    @Test
    fun setFromMatrix3() {
        val a = Matrix3().set(
            0.0, 1.0, 2.0,
            3.0, 4.0, 5.0,
            6.0, 7.0, 8.0,
        )
        val b = Matrix4()
        val c = Matrix4().set(
            0.0, 1.0, 2.0, 0.0,
            3.0, 4.0, 5.0, 0.0,
            6.0, 7.0, 8.0, 0.0,
            0.0, 0.0, 0.0, 1.0,
        )
        b.setFromMatrix3(a)
        assertTrue(b == c)
    }

    @Test
    fun copyPosition() {
        val a = Matrix4().set(
            1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0,
        )
        val b = Matrix4().set(
            1.0, 2.0, 3.0, 0.0, 5.0, 6.0, 7.0, 0.0, 9.0, 10.0, 11.0, 0.0, 13.0, 14.0, 15.0, 16.0,
        )

        assertFalse(matrixEquals4(a, b), "a and b initially not equal")

        b.copyPosition(a)
        assertTrue(matrixEquals4(a, b), "a and b equal after copyPosition()")
    }

    @Test
    fun makeBasisExtractBasis() {
        val identityBasis = listOf(Vector3(1.0, 0.0, 0.0), Vector3(0.0, 1.0, 0.0), Vector3(0.0, 0.0, 1.0))
        val a = Matrix4().makeBasis(identityBasis[0], identityBasis[1], identityBasis[2])
        val identity = Matrix4()
        assertTrue(matrixEquals4(a, identity), "Passed!")

        val testBases = listOf(listOf(Vector3(0.0, 1.0, 0.0), Vector3(-1.0, 0.0, 0.0), Vector3(0.0, 0.0, 1.0)))
        for (testBasis in testBases) {
            val b = Matrix4().makeBasis(testBasis[0], testBasis[1], testBasis[2])
            val outBasis = listOf(Vector3(), Vector3(), Vector3())
            b.extractBasis(outBasis[0], outBasis[1], outBasis[2])
            // check what goes in, is what comes out.
            for (j in outBasis.indices) {
                assertTrue(outBasis[j] == testBasis[j], "Passed!")
            }

            // get the basis out the hard way
            for (j in identityBasis.indices) {
                outBasis[j].copy(identityBasis[j])
                outBasis[j].applyMatrix4(b)
            }

            // did the multiply method of basis extraction work?
            for (j in outBasis.indices) {
                assertTrue(outBasis[j] == testBasis[j], "Passed!")
            }
        }
    }

    @Test
    fun makeRotationFromEulerExtractRotation() {
        val testValues = listOf(
            Euler(0.0, 0.0, 0.0, EulerOrder.XYZ),
            Euler(1.0, 0.0, 0.0, EulerOrder.XYZ),
            Euler(0.0, 1.0, 0.0, EulerOrder.ZYX),
            Euler(0.0, 0.0, 0.5, EulerOrder.YZX),
            Euler(0.0, 0.0, -0.5, EulerOrder.YZX),
        )

        for (i in testValues.indices) {
            val v = testValues[i]

            val m = Matrix4().makeRotationFromEuler(v)

            val v2 = Euler().setFromRotationMatrix(m, v.order)
            val m2 = Matrix4().makeRotationFromEuler(v2)

            assertTrue(
                matrixEquals4(m, m2, eps),
                "makeRotationFromEuler #$i: original and Euler-derived matrices are equal",
            )
            assertTrue(
                eulerEquals(v, v2, eps),
                "makeRotationFromEuler #$i: original and matrix-derived Eulers are equal",
            )

            val m3 = Matrix4().extractRotation(m2)
            val v3 = Euler().setFromRotationMatrix(m3, v.order)

            assertTrue(
                matrixEquals4(m, m3, eps),
                "extractRotation #$i: original and extracted matrices are equal",
            )
            assertTrue(
                eulerEquals(v, v3, eps),
                "extractRotation #$i: original and extracted Eulers are equal",
            )
        }
    }

    @Test
    fun lookAt() {
        val a = Matrix4()
        val expected = Matrix4().identity()
        val eye = Vector3(0.0, 0.0, 0.0)
        val target = Vector3(0.0, 1.0, -1.0)
        val up = Vector3(0.0, 1.0, 0.0)

        a.lookAt(eye, target, up)
        val rotation = Euler().setFromRotationMatrix(a)
        // JS `assert.numEqual` uses a 0.0001 tolerance.
        assertEquals(45.0, rotation.x * (180.0 / PI), eps, "Check the rotation")

        // eye and target are in the same position
        eye.copy(target)
        a.lookAt(eye, target, up)
        assertTrue(matrixEquals4(a, expected), "Check the result for eye == target")

        // up and z are parallel
        eye.set(0.0, 1.0, 0.0)
        target.set(0.0, 0.0, 0.0)
        a.lookAt(eye, target, up)
        expected.set(
            1.0, 0.0, 0.0, 0.0,
            0.0, 0.0001, 1.0, 0.0,
            0.0, -1.0, 0.0001, 0.0,
            0.0, 0.0, 0.0, 1.0,
        )
        assertTrue(matrixEquals4(a, expected), "Check the result for when up and z are parallel")
    }

    @Test
    fun multiply() {
        val lhs = Matrix4().set(
            2.0, 3.0, 5.0, 7.0, 11.0, 13.0, 17.0, 19.0, 23.0, 29.0, 31.0, 37.0, 41.0, 43.0, 47.0, 53.0,
        )
        val rhs = Matrix4().set(
            59.0, 61.0, 67.0, 71.0, 73.0, 79.0, 83.0, 89.0, 97.0, 101.0, 103.0, 107.0, 109.0, 113.0, 127.0, 131.0,
        )

        lhs.multiply(rhs)

        assertTrue(lhs.elements[0] == 1585.0)
        assertTrue(lhs.elements[1] == 5318.0)
        assertTrue(lhs.elements[2] == 10514.0)
        assertTrue(lhs.elements[3] == 15894.0)
        assertTrue(lhs.elements[4] == 1655.0)
        assertTrue(lhs.elements[5] == 5562.0)
        assertTrue(lhs.elements[6] == 11006.0)
        assertTrue(lhs.elements[7] == 16634.0)
        assertTrue(lhs.elements[8] == 1787.0)
        assertTrue(lhs.elements[9] == 5980.0)
        assertTrue(lhs.elements[10] == 11840.0)
        assertTrue(lhs.elements[11] == 17888.0)
        assertTrue(lhs.elements[12] == 1861.0)
        assertTrue(lhs.elements[13] == 6246.0)
        assertTrue(lhs.elements[14] == 12378.0)
        assertTrue(lhs.elements[15] == 18710.0)
    }

    @Test
    fun premultiply() {
        val lhs = Matrix4().set(
            2.0, 3.0, 5.0, 7.0, 11.0, 13.0, 17.0, 19.0, 23.0, 29.0, 31.0, 37.0, 41.0, 43.0, 47.0, 53.0,
        )
        val rhs = Matrix4().set(
            59.0, 61.0, 67.0, 71.0, 73.0, 79.0, 83.0, 89.0, 97.0, 101.0, 103.0, 107.0, 109.0, 113.0, 127.0, 131.0,
        )

        rhs.premultiply(lhs)

        assertTrue(rhs.elements[0] == 1585.0)
        assertTrue(rhs.elements[1] == 5318.0)
        assertTrue(rhs.elements[2] == 10514.0)
        assertTrue(rhs.elements[3] == 15894.0)
        assertTrue(rhs.elements[4] == 1655.0)
        assertTrue(rhs.elements[5] == 5562.0)
        assertTrue(rhs.elements[6] == 11006.0)
        assertTrue(rhs.elements[7] == 16634.0)
        assertTrue(rhs.elements[8] == 1787.0)
        assertTrue(rhs.elements[9] == 5980.0)
        assertTrue(rhs.elements[10] == 11840.0)
        assertTrue(rhs.elements[11] == 17888.0)
        assertTrue(rhs.elements[12] == 1861.0)
        assertTrue(rhs.elements[13] == 6246.0)
        assertTrue(rhs.elements[14] == 12378.0)
        assertTrue(rhs.elements[15] == 18710.0)
    }

    @Test
    fun multiplyMatrices() {
        // Reference (numpy dot product), see three.js test comment:
        // [[ 1585  1655  1787  1861]
        //  [ 5318  5562  5980  6246]
        //  [10514 11006 11840 12378]
        //  [15894 16634 17888 18710]]
        val lhs = Matrix4().set(
            2.0, 3.0, 5.0, 7.0, 11.0, 13.0, 17.0, 19.0, 23.0, 29.0, 31.0, 37.0, 41.0, 43.0, 47.0, 53.0,
        )
        val rhs = Matrix4().set(
            59.0, 61.0, 67.0, 71.0, 73.0, 79.0, 83.0, 89.0, 97.0, 101.0, 103.0, 107.0, 109.0, 113.0, 127.0, 131.0,
        )
        val ans = Matrix4()

        ans.multiplyMatrices(lhs, rhs)

        assertTrue(ans.elements[0] == 1585.0)
        assertTrue(ans.elements[1] == 5318.0)
        assertTrue(ans.elements[2] == 10514.0)
        assertTrue(ans.elements[3] == 15894.0)
        assertTrue(ans.elements[4] == 1655.0)
        assertTrue(ans.elements[5] == 5562.0)
        assertTrue(ans.elements[6] == 11006.0)
        assertTrue(ans.elements[7] == 16634.0)
        assertTrue(ans.elements[8] == 1787.0)
        assertTrue(ans.elements[9] == 5980.0)
        assertTrue(ans.elements[10] == 11840.0)
        assertTrue(ans.elements[11] == 17888.0)
        assertTrue(ans.elements[12] == 1861.0)
        assertTrue(ans.elements[13] == 6246.0)
        assertTrue(ans.elements[14] == 12378.0)
        assertTrue(ans.elements[15] == 18710.0)
    }

    @Test
    fun multiplyScalar() {
        val b = Matrix4().set(
            0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0,
        )
        assertTrue(b.elements[0] == 0.0)
        assertTrue(b.elements[1] == 4.0)
        assertTrue(b.elements[2] == 8.0)
        assertTrue(b.elements[3] == 12.0)
        assertTrue(b.elements[4] == 1.0)
        assertTrue(b.elements[5] == 5.0)
        assertTrue(b.elements[6] == 9.0)
        assertTrue(b.elements[7] == 13.0)
        assertTrue(b.elements[8] == 2.0)
        assertTrue(b.elements[9] == 6.0)
        assertTrue(b.elements[10] == 10.0)
        assertTrue(b.elements[11] == 14.0)
        assertTrue(b.elements[12] == 3.0)
        assertTrue(b.elements[13] == 7.0)
        assertTrue(b.elements[14] == 11.0)
        assertTrue(b.elements[15] == 15.0)

        b.multiplyScalar(2.0)
        assertTrue(b.elements[0] == 0.0 * 2.0)
        assertTrue(b.elements[1] == 4.0 * 2.0)
        assertTrue(b.elements[2] == 8.0 * 2.0)
        assertTrue(b.elements[3] == 12.0 * 2.0)
        assertTrue(b.elements[4] == 1.0 * 2.0)
        assertTrue(b.elements[5] == 5.0 * 2.0)
        assertTrue(b.elements[6] == 9.0 * 2.0)
        assertTrue(b.elements[7] == 13.0 * 2.0)
        assertTrue(b.elements[8] == 2.0 * 2.0)
        assertTrue(b.elements[9] == 6.0 * 2.0)
        assertTrue(b.elements[10] == 10.0 * 2.0)
        assertTrue(b.elements[11] == 14.0 * 2.0)
        assertTrue(b.elements[12] == 3.0 * 2.0)
        assertTrue(b.elements[13] == 7.0 * 2.0)
        assertTrue(b.elements[14] == 11.0 * 2.0)
        assertTrue(b.elements[15] == 15.0 * 2.0)
    }

    @Test
    fun determinant() {
        val a = Matrix4()
        assertTrue(a.determinant() == 1.0, "Passed!")

        a.elements[0] = 2.0
        assertTrue(a.determinant() == 2.0, "Passed!")

        a.elements[0] = 0.0
        assertTrue(a.determinant() == 0.0, "Passed!")

        // calculated via http://www.euclideanspace.com/maths/algebra/matrix/functions/determinant/fourD/index.htm
        a.set(2.0, 3.0, 4.0, 5.0, -1.0, -21.0, -3.0, -4.0, 6.0, 7.0, 8.0, 10.0, -8.0, -9.0, -10.0, -12.0)
        assertTrue(a.determinant() == 76.0, "Passed!")
    }

    @Test
    fun transpose() {
        val a = Matrix4()
        var b = a.clone().transpose()
        assertTrue(matrixEquals4(a, b), "Passed!")

        b = Matrix4().set(
            0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0,
        )
        val c = b.clone().transpose()
        assertFalse(matrixEquals4(b, c), "Passed!")
        c.transpose()
        assertTrue(matrixEquals4(b, c), "Passed!")
    }

    @Test
    fun setPosition() {
        val a = Matrix4().set(
            0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0,
        )
        val b = Vector3(-1.0, -2.0, -3.0)
        val c = Matrix4().set(
            0.0, 1.0, 2.0, -1.0, 4.0, 5.0, 6.0, -2.0, 8.0, 9.0, 10.0, -3.0, 12.0, 13.0, 14.0, 15.0,
        )

        a.setPosition(b)
        assertTrue(matrixEquals4(a, c), "Passed!")

        val d = Matrix4().set(
            0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0,
        )
        val e = Matrix4().set(
            0.0, 1.0, 2.0, -1.0, 4.0, 5.0, 6.0, -2.0, 8.0, 9.0, 10.0, -3.0, 12.0, 13.0, 14.0, 15.0,
        )

        d.setPosition(-1.0, -2.0, -3.0)
        assertTrue(matrixEquals4(d, e), "Passed!")
    }

    @Test
    fun invert() {
        val zero = Matrix4().set(
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        )
        val identity = Matrix4()

        val a = Matrix4()
        val b = Matrix4().set(
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        )

        a.copy(b).invert()
        assertTrue(matrixEquals4(a, zero), "Passed!")

        val testMatrices = listOf(
            Matrix4().makeRotationX(0.3),
            Matrix4().makeRotationX(-0.3),
            Matrix4().makeRotationY(0.3),
            Matrix4().makeRotationY(-0.3),
            Matrix4().makeRotationZ(0.3),
            Matrix4().makeRotationZ(-0.3),
            Matrix4().makeScale(1.0, 2.0, 3.0),
            Matrix4().makeScale(1.0 / 8.0, 1.0 / 2.0, 1.0 / 3.0),
            Matrix4().makePerspective(-1.0, 1.0, 1.0, -1.0, 1.0, 1000.0),
            Matrix4().makePerspective(-16.0, 16.0, 9.0, -9.0, 0.1, 10000.0),
            Matrix4().makeTranslation(1.0, 2.0, 3.0),
        )

        for (m in testMatrices) {
            val mInverse = Matrix4().copy(m).invert()
            val mSelfInverse = m.clone()
            mSelfInverse.copy(mSelfInverse).invert()

            // self-inverse should be the same as inverse
            assertTrue(matrixEquals4(mSelfInverse, mInverse), "Passed!")

            // the determinant of the inverse should be the reciprocal
            assertTrue(abs(m.determinant() * mInverse.determinant() - 1.0) < 0.0001, "Passed!")

            val mProduct = Matrix4().multiplyMatrices(m, mInverse)

            // the determinant of the identity matrix is 1
            assertTrue(abs(mProduct.determinant() - 1.0) < 0.0001, "Passed!")
            assertTrue(matrixEquals4(mProduct, identity), "Passed!")
        }
    }

    @Test
    fun scale() {
        val a = Matrix4().set(
            1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0,
        )
        val b = Vector3(2.0, 3.0, 4.0)
        val c = Matrix4().set(
            2.0, 6.0, 12.0, 4.0, 10.0, 18.0, 28.0, 8.0, 18.0, 30.0, 44.0, 12.0, 26.0, 42.0, 60.0, 16.0,
        )

        a.scale(b)
        assertTrue(matrixEquals4(a, c), "Passed!")
    }

    @Test
    fun getMaxScaleOnAxis() {
        val a = Matrix4().set(
            1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0,
        )
        val expected = sqrt(3.0 * 3.0 + 7.0 * 7.0 + 11.0 * 11.0)

        assertTrue(abs(a.getMaxScaleOnAxis() - expected) <= eps, "Check result")
    }

    @Test
    fun makeTranslation() {
        val a = Matrix4()
        val b = Vector3(2.0, 3.0, 4.0)
        val c = Matrix4().set(
            1.0, 0.0, 0.0, 2.0, 0.0, 1.0, 0.0, 3.0, 0.0, 0.0, 1.0, 4.0, 0.0, 0.0, 0.0, 1.0,
        )

        a.makeTranslation(b.x, b.y, b.z)
        assertTrue(matrixEquals4(a, c), "Passed!")

        a.makeTranslation(b)
        assertTrue(matrixEquals4(a, c), "Passed!")
    }

    @Test
    fun makeRotationX() {
        val a = Matrix4()
        val b = sqrt(3.0) / 2.0
        val c = Matrix4().set(
            1.0, 0.0, 0.0, 0.0, 0.0, b, -0.5, 0.0, 0.0, 0.5, b, 0.0, 0.0, 0.0, 0.0, 1.0,
        )

        a.makeRotationX(PI / 6)
        assertTrue(matrixEquals4(a, c), "Passed!")
    }

    @Test
    fun makeRotationY() {
        val a = Matrix4()
        val b = sqrt(3.0) / 2.0
        val c = Matrix4().set(
            b, 0.0, 0.5, 0.0, 0.0, 1.0, 0.0, 0.0, -0.5, 0.0, b, 0.0, 0.0, 0.0, 0.0, 1.0,
        )

        a.makeRotationY(PI / 6)
        assertTrue(matrixEquals4(a, c), "Passed!")
    }

    @Test
    fun makeRotationZ() {
        val a = Matrix4()
        val b = sqrt(3.0) / 2.0
        val c = Matrix4().set(
            b, -0.5, 0.0, 0.0, 0.5, b, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0,
        )

        a.makeRotationZ(PI / 6)
        assertTrue(matrixEquals4(a, c), "Passed!")
    }

    @Test
    fun makeRotationAxis() {
        val axis = Vector3(1.5, 0.0, 1.0).normalize()
        val radians = MathUtils.degToRad(45.0)
        val a = Matrix4().makeRotationAxis(axis, radians)

        val expected = Matrix4().set(
            0.9098790095958609, -0.39223227027636803, 0.13518148560620882, 0.0,
            0.39223227027636803, 0.7071067811865476, -0.588348405414552, 0.0,
            0.13518148560620882, 0.588348405414552, 0.7972277715906868, 0.0,
            0.0, 0.0, 0.0, 1.0,
        )

        assertTrue(matrixEquals4(a, expected), "Check numeric result")
    }

    @Test
    fun makeScale() {
        val a = Matrix4()
        val c = Matrix4().set(
            2.0, 0.0, 0.0, 0.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 4.0, 0.0, 0.0, 0.0, 0.0, 1.0,
        )

        a.makeScale(2.0, 3.0, 4.0)
        assertTrue(matrixEquals4(a, c), "Passed!")
    }

    @Test
    fun makeShear() {
        val a = Matrix4()
        val c = Matrix4().set(
            1.0, 3.0, 5.0, 0.0, 1.0, 1.0, 6.0, 0.0, 2.0, 4.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0,
        )

        a.makeShear(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
        assertTrue(matrixEquals4(a, c), "Passed!")
    }

    @Test
    fun composeDecompose() {
        val tValues = listOf(
            Vector3(),
            Vector3(3.0, 0.0, 0.0),
            Vector3(0.0, 4.0, 0.0),
            Vector3(0.0, 0.0, 5.0),
            Vector3(-6.0, 0.0, 0.0),
            Vector3(0.0, -7.0, 0.0),
            Vector3(0.0, 0.0, -8.0),
            Vector3(-2.0, 5.0, -9.0),
            Vector3(-2.0, -5.0, -9.0),
        )

        val sValues = listOf(
            Vector3(1.0, 1.0, 1.0),
            Vector3(2.0, 2.0, 2.0),
            Vector3(1.0, -1.0, 1.0),
            Vector3(-1.0, 1.0, 1.0),
            Vector3(1.0, 1.0, -1.0),
            Vector3(2.0, -2.0, 1.0),
            Vector3(-1.0, 2.0, -2.0),
            Vector3(-1.0, -1.0, -1.0),
            Vector3(-2.0, -2.0, -2.0),
        )

        val rValues = listOf(
            Quaternion(),
            Quaternion().setFromEuler(Euler(1.0, 1.0, 0.0)),
            Quaternion().setFromEuler(Euler(1.0, -1.0, 1.0)),
            Quaternion(0.0, 0.9238795292366128, 0.0, 0.38268342717215614),
        )

        for (t in tValues) {
            for (s in sValues) {
                for (r in rValues) {
                    val m = Matrix4().compose(t, r, s)
                    val t2 = Vector3()
                    val r2 = Quaternion()
                    val s2 = Vector3()

                    m.decompose(t2, r2, s2)

                    val m2 = Matrix4().compose(t2, r2, s2)

                    assertTrue(matrixEquals4(m, m2), "Passed!")
                }
            }
        }
    }

    @Test
    fun makePerspective() {
        val a = Matrix4().makePerspective(-1.0, 1.0, -1.0, 1.0, 1.0, 100.0)
        val expected = Matrix4().set(
            1.0, 0.0, 0.0, 0.0,
            0.0, -1.0, 0.0, 0.0,
            0.0, 0.0, -101.0 / 99.0, -200.0 / 99.0,
            0.0, 0.0, -1.0, 0.0,
        )
        assertTrue(matrixEquals4(a, expected), "Check result")
    }

    @Test
    fun makeOrthographic() {
        val a = Matrix4().makeOrthographic(-1.0, 1.0, -1.0, 1.0, 1.0, 100.0)
        val expected = Matrix4().set(
            1.0, 0.0, 0.0, 0.0,
            0.0, -1.0, 0.0, 0.0,
            0.0, 0.0, -2.0 / 99.0, -101.0 / 99.0,
            0.0, 0.0, 0.0, 1.0,
        )

        assertTrue(matrixEquals4(a, expected), "Check result")
    }

    @Test
    fun equalsTest() {
        val a = Matrix4().set(
            0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0,
        )
        val b = Matrix4().set(
            0.0, -1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0,
        )

        assertFalse(a == b, "Check that a does not equal b")
        assertFalse(b == a, "Check that b does not equal a")

        a.copy(b)
        assertTrue(a == b, "Check that a equals b after copy()")
        assertTrue(b == a, "Check that b equals a after copy()")
    }

    @Test
    fun fromArray() {
        val a = Matrix4()
        val b = Matrix4().set(
            1.0, 5.0, 9.0, 13.0, 2.0, 6.0, 10.0, 14.0, 3.0, 7.0, 11.0, 15.0, 4.0, 8.0, 12.0, 16.0,
        )

        a.fromArray(doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0))
        assertTrue(a == b, "Passed")
    }

    @Test
    fun toArray() {
        val a = Matrix4().set(
            1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0,
        )
        val noOffset = listOf(1.0, 5.0, 9.0, 13.0, 2.0, 6.0, 10.0, 14.0, 3.0, 7.0, 11.0, 15.0, 4.0, 8.0, 12.0, 16.0)
        // three.js leaves index 0 undefined; Kotlin MutableList fills the gap with 0.0.
        val withOffset =
            listOf(0.0, 1.0, 5.0, 9.0, 13.0, 2.0, 6.0, 10.0, 14.0, 3.0, 7.0, 11.0, 15.0, 4.0, 8.0, 12.0, 16.0)

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
