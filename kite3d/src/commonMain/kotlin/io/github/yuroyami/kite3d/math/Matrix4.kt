/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Matrix4.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The clip-space coordinate convention a projection matrix targets.
 *
 * three.js selects between these with the `WebGLCoordinateSystem` /
 * `WebGPUCoordinateSystem` integer constants from `constants.js`; this enum
 * replaces those constants for [Matrix4.makePerspective]/[Matrix4.makeOrthographic].
 * [WebGL] maps clip-space z to `[-1, 1]`; [WebGPU] maps it to `[0, 1]`.
 */
public enum class CoordinateSystem {
    WebGL,
    WebGPU,
}

/**
 * Represents a 4x4 matrix.
 *
 * The most common use of a 4x4 matrix in 3D computer graphics is as a
 * transformation matrix — translation, rotation, shear, scale, reflection,
 * orthographic or perspective projection — applied to a vector by multiplication.
 *
 * A note on row-major and column-major ordering: the [set] method (and the
 * `Matrix4(...)` element constructor, exposed as an `ext/` convenience) take
 * arguments in **row-major** order, while internally the values are stored in the
 * [elements] array in **column-major** order. This matches three.js exactly.
 *
 * Matrices are **mutable** and **not thread-safe**; confine an instance (and any
 * object graph holding it) to a single thread, exactly as in three.js. Most
 * methods mutate `this` and return it for chaining.
 */
public class Matrix4 {

    /**
     * A column-major list of matrix values. Initialized to the 4x4 identity.
     *
     * Exposed as a `val` (dialect rule 12): three.js never reassigns the array,
     * only mutates its contents, so the reference is fixed while the values stay
     * mutable.
     */
    public val elements: DoubleArray = doubleArrayOf(
        1.0, 0.0, 0.0, 0.0,
        0.0, 1.0, 0.0, 0.0,
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0,
    )

    /**
     * Sets the elements of the matrix. The arguments are supposed to be in
     * row-major order.
     *
     * @return A reference to this matrix.
     */
    public fun set(
        n11: Double, n12: Double, n13: Double, n14: Double,
        n21: Double, n22: Double, n23: Double, n24: Double,
        n31: Double, n32: Double, n33: Double, n34: Double,
        n41: Double, n42: Double, n43: Double, n44: Double,
    ): Matrix4 {
        val te = elements

        te[0] = n11; te[4] = n12; te[8] = n13; te[12] = n14
        te[1] = n21; te[5] = n22; te[9] = n23; te[13] = n24
        te[2] = n31; te[6] = n32; te[10] = n33; te[14] = n34
        te[3] = n41; te[7] = n42; te[11] = n43; te[15] = n44

        return this
    }

    /**
     * Sets this matrix to the 4x4 identity matrix.
     *
     * @return A reference to this matrix.
     */
    public fun identity(): Matrix4 {
        set(
            1.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0,
        )

        return this
    }

    /**
     * Returns a new matrix with copied values from this instance.
     *
     * @return A clone of this instance.
     */
    public fun clone(): Matrix4 = Matrix4().fromArray(elements)

    /**
     * Copies the values of the given matrix [m] to this instance.
     *
     * @return A reference to this matrix.
     */
    public fun copy(m: Matrix4): Matrix4 {
        val te = elements
        val me = m.elements

        te[0] = me[0]; te[1] = me[1]; te[2] = me[2]; te[3] = me[3]
        te[4] = me[4]; te[5] = me[5]; te[6] = me[6]; te[7] = me[7]
        te[8] = me[8]; te[9] = me[9]; te[10] = me[10]; te[11] = me[11]
        te[12] = me[12]; te[13] = me[13]; te[14] = me[14]; te[15] = me[15]

        return this
    }

    /**
     * Copies the translation component of the given matrix [m] into this matrix's
     * translation component.
     *
     * @return A reference to this matrix.
     */
    public fun copyPosition(m: Matrix4): Matrix4 {
        val te = elements
        val me = m.elements

        te[12] = me[12]
        te[13] = me[13]
        te[14] = me[14]

        return this
    }

    /**
     * Sets the upper 3x3 elements of this matrix to the values of the given 3x3
     * matrix [m].
     *
     * @return A reference to this matrix.
     */
    public fun setFromMatrix3(m: Matrix3): Matrix4 {
        val me = m.elements

        set(
            me[0], me[3], me[6], 0.0,
            me[1], me[4], me[7], 0.0,
            me[2], me[5], me[8], 0.0,
            0.0, 0.0, 0.0, 1.0,
        )

        return this
    }

    /**
     * Extracts the basis of this matrix into the three axis vectors provided.
     *
     * @return A reference to this matrix.
     */
    public fun extractBasis(xAxis: Vector3, yAxis: Vector3, zAxis: Vector3): Matrix4 {
        if (determinant() == 0.0) {
            xAxis.set(1.0, 0.0, 0.0)
            yAxis.set(0.0, 1.0, 0.0)
            zAxis.set(0.0, 0.0, 1.0)

            return this
        }

        xAxis.setFromMatrixColumn(this, 0)
        yAxis.setFromMatrixColumn(this, 1)
        zAxis.setFromMatrixColumn(this, 2)

        return this
    }

    /**
     * Sets the given basis vectors as the columns of this matrix.
     *
     * @return A reference to this matrix.
     */
    public fun makeBasis(xAxis: Vector3, yAxis: Vector3, zAxis: Vector3): Matrix4 {
        set(
            xAxis.x, yAxis.x, zAxis.x, 0.0,
            xAxis.y, yAxis.y, zAxis.y, 0.0,
            xAxis.z, yAxis.z, zAxis.z, 0.0,
            0.0, 0.0, 0.0, 1.0,
        )

        return this
    }

    /**
     * Extracts the rotation component of the given matrix [m] into this matrix's
     * rotation component.
     *
     * Note: this method does not support reflection matrices.
     *
     * @return A reference to this matrix.
     */
    public fun extractRotation(m: Matrix4): Matrix4 {
        if (m.determinant() == 0.0) {
            return identity()
        }

        val te = elements
        val me = m.elements

        // three.js reuses a module-level `_v1` scratch here; a local is race-free
        // and equivalent.
        val v1 = Vector3()
        val scaleX = 1.0 / v1.setFromMatrixColumn(m, 0).length()
        val scaleY = 1.0 / v1.setFromMatrixColumn(m, 1).length()
        val scaleZ = 1.0 / v1.setFromMatrixColumn(m, 2).length()

        te[0] = me[0] * scaleX
        te[1] = me[1] * scaleX
        te[2] = me[2] * scaleX
        te[3] = 0.0

        te[4] = me[4] * scaleY
        te[5] = me[5] * scaleY
        te[6] = me[6] * scaleY
        te[7] = 0.0

        te[8] = me[8] * scaleZ
        te[9] = me[9] * scaleZ
        te[10] = me[10] * scaleZ
        te[11] = 0.0

        te[12] = 0.0
        te[13] = 0.0
        te[14] = 0.0
        te[15] = 1.0

        return this
    }

    /**
     * Sets the rotation component (the upper-left 3x3 matrix) of this matrix to the
     * rotation specified by the given Euler angles [euler]. The rest of the matrix
     * is set to the identity. Depending on the [Euler.order], there are six possible
     * outcomes.
     *
     * @return A reference to this matrix.
     */
    public fun makeRotationFromEuler(euler: Euler): Matrix4 {
        val te = elements

        val x = euler.x; val y = euler.y; val z = euler.z
        val a = cos(x); val b = sin(x)
        val c = cos(y); val d = sin(y)
        val e = cos(z); val f = sin(z)

        when (euler.order) {
            EulerOrder.XYZ -> {
                val ae = a * e; val af = a * f; val be = b * e; val bf = b * f

                te[0] = c * e
                te[4] = -c * f
                te[8] = d

                te[1] = af + be * d
                te[5] = ae - bf * d
                te[9] = -b * c

                te[2] = bf - ae * d
                te[6] = be + af * d
                te[10] = a * c
            }

            EulerOrder.YXZ -> {
                val ce = c * e; val cf = c * f; val de = d * e; val df = d * f

                te[0] = ce + df * b
                te[4] = de * b - cf
                te[8] = a * d

                te[1] = a * f
                te[5] = a * e
                te[9] = -b

                te[2] = cf * b - de
                te[6] = df + ce * b
                te[10] = a * c
            }

            EulerOrder.ZXY -> {
                val ce = c * e; val cf = c * f; val de = d * e; val df = d * f

                te[0] = ce - df * b
                te[4] = -a * f
                te[8] = de + cf * b

                te[1] = cf + de * b
                te[5] = a * e
                te[9] = df - ce * b

                te[2] = -a * d
                te[6] = b
                te[10] = a * c
            }

            EulerOrder.ZYX -> {
                val ae = a * e; val af = a * f; val be = b * e; val bf = b * f

                te[0] = c * e
                te[4] = be * d - af
                te[8] = ae * d + bf

                te[1] = c * f
                te[5] = bf * d + ae
                te[9] = af * d - be

                te[2] = -d
                te[6] = b * c
                te[10] = a * c
            }

            EulerOrder.YZX -> {
                val ac = a * c; val ad = a * d; val bc = b * c; val bd = b * d

                te[0] = c * e
                te[4] = bd - ac * f
                te[8] = bc * f + ad

                te[1] = f
                te[5] = a * e
                te[9] = -b * e

                te[2] = -d * e
                te[6] = ad * f + bc
                te[10] = ac - bd * f
            }

            EulerOrder.XZY -> {
                val ac = a * c; val ad = a * d; val bc = b * c; val bd = b * d

                te[0] = c * e
                te[4] = -f
                te[8] = d * e

                te[1] = ac * f + bd
                te[5] = a * e
                te[9] = ad * f - bc

                te[2] = bc * f - ad
                te[6] = b * e
                te[10] = bd * f + ac
            }
        }

        // bottom row
        te[3] = 0.0
        te[7] = 0.0
        te[11] = 0.0

        // last column
        te[12] = 0.0
        te[13] = 0.0
        te[14] = 0.0
        te[15] = 1.0

        return this
    }

    /**
     * Sets the rotation component of this matrix to the rotation specified by the
     * given Quaternion [q]. The rest of the matrix is set to the identity.
     *
     * @return A reference to this matrix.
     */
    public fun makeRotationFromQuaternion(q: Quaternion): Matrix4 {
        // three.js passes module-level `_zero`/`_one` scratch; locals are equivalent.
        return compose(Vector3(0.0, 0.0, 0.0), q, Vector3(1.0, 1.0, 1.0))
    }

    /**
     * Sets the rotation component of the transformation matrix, looking from [eye]
     * towards [target], oriented by the [up] direction.
     *
     * @return A reference to this matrix.
     */
    public fun lookAt(eye: Vector3, target: Vector3, up: Vector3): Matrix4 {
        val te = elements

        // three.js reuses module-level `_x`/`_y`/`_z` scratch; locals are race-free.
        val x = Vector3()
        val y = Vector3()
        val z = Vector3()

        z.subVectors(eye, target)

        if (z.lengthSq() == 0.0) {
            // eye and target are in the same position

            z.z = 1.0
        }

        z.normalize()
        x.crossVectors(up, z)

        if (x.lengthSq() == 0.0) {
            // up and z are parallel

            if (abs(up.z) == 1.0) {
                z.x += 0.0001
            } else {
                z.z += 0.0001
            }

            z.normalize()
            x.crossVectors(up, z)
        }

        x.normalize()
        y.crossVectors(z, x)

        te[0] = x.x; te[4] = y.x; te[8] = z.x
        te[1] = x.y; te[5] = y.y; te[9] = z.y
        te[2] = x.z; te[6] = y.z; te[10] = z.z

        return this
    }

    /**
     * Post-multiplies this matrix by the given 4x4 matrix [m].
     *
     * @return A reference to this matrix.
     */
    public fun multiply(m: Matrix4): Matrix4 {
        return multiplyMatrices(this, m)
    }

    /**
     * Pre-multiplies this matrix by the given 4x4 matrix [m].
     *
     * @return A reference to this matrix.
     */
    public fun premultiply(m: Matrix4): Matrix4 {
        return multiplyMatrices(m, this)
    }

    /**
     * Multiplies the given 4x4 matrices [a] and [b] and stores the result in this
     * matrix.
     *
     * @return A reference to this matrix.
     */
    public fun multiplyMatrices(a: Matrix4, b: Matrix4): Matrix4 {
        val ae = a.elements
        val be = b.elements
        val te = elements

        val a11 = ae[0]; val a12 = ae[4]; val a13 = ae[8]; val a14 = ae[12]
        val a21 = ae[1]; val a22 = ae[5]; val a23 = ae[9]; val a24 = ae[13]
        val a31 = ae[2]; val a32 = ae[6]; val a33 = ae[10]; val a34 = ae[14]
        val a41 = ae[3]; val a42 = ae[7]; val a43 = ae[11]; val a44 = ae[15]

        val b11 = be[0]; val b12 = be[4]; val b13 = be[8]; val b14 = be[12]
        val b21 = be[1]; val b22 = be[5]; val b23 = be[9]; val b24 = be[13]
        val b31 = be[2]; val b32 = be[6]; val b33 = be[10]; val b34 = be[14]
        val b41 = be[3]; val b42 = be[7]; val b43 = be[11]; val b44 = be[15]

        te[0] = a11 * b11 + a12 * b21 + a13 * b31 + a14 * b41
        te[4] = a11 * b12 + a12 * b22 + a13 * b32 + a14 * b42
        te[8] = a11 * b13 + a12 * b23 + a13 * b33 + a14 * b43
        te[12] = a11 * b14 + a12 * b24 + a13 * b34 + a14 * b44

        te[1] = a21 * b11 + a22 * b21 + a23 * b31 + a24 * b41
        te[5] = a21 * b12 + a22 * b22 + a23 * b32 + a24 * b42
        te[9] = a21 * b13 + a22 * b23 + a23 * b33 + a24 * b43
        te[13] = a21 * b14 + a22 * b24 + a23 * b34 + a24 * b44

        te[2] = a31 * b11 + a32 * b21 + a33 * b31 + a34 * b41
        te[6] = a31 * b12 + a32 * b22 + a33 * b32 + a34 * b42
        te[10] = a31 * b13 + a32 * b23 + a33 * b33 + a34 * b43
        te[14] = a31 * b14 + a32 * b24 + a33 * b34 + a34 * b44

        te[3] = a41 * b11 + a42 * b21 + a43 * b31 + a44 * b41
        te[7] = a41 * b12 + a42 * b22 + a43 * b32 + a44 * b42
        te[11] = a41 * b13 + a42 * b23 + a43 * b33 + a44 * b43
        te[15] = a41 * b14 + a42 * b24 + a43 * b34 + a44 * b44

        return this
    }

    /**
     * Multiplies every component of the matrix by the given scalar [s].
     *
     * @return A reference to this matrix.
     */
    public fun multiplyScalar(s: Double): Matrix4 {
        val te = elements

        te[0] *= s; te[4] *= s; te[8] *= s; te[12] *= s
        te[1] *= s; te[5] *= s; te[9] *= s; te[13] *= s
        te[2] *= s; te[6] *= s; te[10] *= s; te[14] *= s
        te[3] *= s; te[7] *= s; te[11] *= s; te[15] *= s

        return this
    }

    /**
     * Computes and returns the determinant of this matrix.
     *
     * @return The determinant.
     */
    public fun determinant(): Double {
        val te = elements

        val n11 = te[0]; val n12 = te[4]; val n13 = te[8]; val n14 = te[12]
        val n21 = te[1]; val n22 = te[5]; val n23 = te[9]; val n24 = te[13]
        val n31 = te[2]; val n32 = te[6]; val n33 = te[10]; val n34 = te[14]
        val n41 = te[3]; val n42 = te[7]; val n43 = te[11]; val n44 = te[15]

        val t11 = n23 * n34 - n24 * n33
        val t12 = n22 * n34 - n24 * n32
        val t13 = n22 * n33 - n23 * n32

        val t21 = n21 * n34 - n24 * n31
        val t22 = n21 * n33 - n23 * n31
        val t23 = n21 * n32 - n22 * n31

        return n11 * (n42 * t11 - n43 * t12 + n44 * t13) -
            n12 * (n41 * t11 - n43 * t21 + n44 * t22) +
            n13 * (n41 * t12 - n42 * t21 + n44 * t23) -
            n14 * (n41 * t13 - n42 * t22 + n43 * t23)
    }

    /**
     * Transposes this matrix in place.
     *
     * @return A reference to this matrix.
     */
    public fun transpose(): Matrix4 {
        val te = elements
        var tmp: Double

        tmp = te[1]; te[1] = te[4]; te[4] = tmp
        tmp = te[2]; te[2] = te[8]; te[8] = tmp
        tmp = te[6]; te[6] = te[9]; te[9] = tmp

        tmp = te[3]; te[3] = te[12]; te[12] = tmp
        tmp = te[7]; te[7] = te[13]; te[13] = tmp
        tmp = te[11]; te[11] = te[14]; te[14] = tmp

        return this
    }

    /**
     * Sets the position component of this matrix from the given vector [v], without
     * affecting the rest of the matrix.
     *
     * @return A reference to this matrix.
     */
    public fun setPosition(v: Vector3): Matrix4 {
        val te = elements

        te[12] = v.x
        te[13] = v.y
        te[14] = v.z

        return this
    }

    /**
     * Sets the position component of this matrix from the given components, without
     * affecting the rest of the matrix.
     *
     * @return A reference to this matrix.
     */
    public fun setPosition(x: Double, y: Double, z: Double): Matrix4 {
        val te = elements

        te[12] = x
        te[13] = y
        te[14] = z

        return this
    }

    /**
     * Inverts this matrix, using the analytic method. You can not invert a matrix
     * with a determinant of zero; if you attempt this, the method produces a zero
     * matrix instead.
     *
     * @return A reference to this matrix.
     */
    public fun invert(): Matrix4 {
        // based on https://github.com/toji/gl-matrix
        val te = elements

        val n11 = te[0]; val n21 = te[1]; val n31 = te[2]; val n41 = te[3]
        val n12 = te[4]; val n22 = te[5]; val n32 = te[6]; val n42 = te[7]
        val n13 = te[8]; val n23 = te[9]; val n33 = te[10]; val n43 = te[11]
        val n14 = te[12]; val n24 = te[13]; val n34 = te[14]; val n44 = te[15]

        val t1 = n11 * n22 - n21 * n12
        val t2 = n11 * n32 - n31 * n12
        val t3 = n11 * n42 - n41 * n12
        val t4 = n21 * n32 - n31 * n22
        val t5 = n21 * n42 - n41 * n22
        val t6 = n31 * n42 - n41 * n32
        val t7 = n13 * n24 - n23 * n14
        val t8 = n13 * n34 - n33 * n14
        val t9 = n13 * n44 - n43 * n14
        val t10 = n23 * n34 - n33 * n24
        val t11 = n23 * n44 - n43 * n24
        val t12 = n33 * n44 - n43 * n34

        val det = t1 * t12 - t2 * t11 + t3 * t10 + t4 * t9 - t5 * t8 + t6 * t7

        if (det == 0.0) return set(
            0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0,
        )

        val detInv = 1.0 / det

        te[0] = (n22 * t12 - n32 * t11 + n42 * t10) * detInv
        te[1] = (n31 * t11 - n21 * t12 - n41 * t10) * detInv
        te[2] = (n24 * t6 - n34 * t5 + n44 * t4) * detInv
        te[3] = (n33 * t5 - n23 * t6 - n43 * t4) * detInv

        te[4] = (n32 * t9 - n12 * t12 - n42 * t8) * detInv
        te[5] = (n11 * t12 - n31 * t9 + n41 * t8) * detInv
        te[6] = (n34 * t3 - n14 * t6 - n44 * t2) * detInv
        te[7] = (n13 * t6 - n33 * t3 + n43 * t2) * detInv

        te[8] = (n12 * t11 - n22 * t9 + n42 * t7) * detInv
        te[9] = (n21 * t9 - n11 * t11 - n41 * t7) * detInv
        te[10] = (n14 * t5 - n24 * t3 + n44 * t1) * detInv
        te[11] = (n23 * t3 - n13 * t5 - n43 * t1) * detInv

        te[12] = (n22 * t8 - n12 * t10 - n32 * t7) * detInv
        te[13] = (n11 * t10 - n21 * t8 + n31 * t7) * detInv
        te[14] = (n24 * t2 - n14 * t4 - n34 * t1) * detInv
        te[15] = (n13 * t4 - n23 * t2 + n33 * t1) * detInv

        return this
    }

    /**
     * Multiplies the columns of this matrix by the given scale vector [v].
     *
     * @return A reference to this matrix.
     */
    public fun scale(v: Vector3): Matrix4 {
        val te = elements
        val x = v.x; val y = v.y; val z = v.z

        te[0] *= x; te[4] *= y; te[8] *= z
        te[1] *= x; te[5] *= y; te[9] *= z
        te[2] *= x; te[6] *= y; te[10] *= z
        te[3] *= x; te[7] *= y; te[11] *= z

        return this
    }

    /**
     * Returns the maximum scale value of the three axes.
     *
     * @return The maximum scale.
     */
    public fun getMaxScaleOnAxis(): Double {
        val te = elements

        val scaleXSq = te[0] * te[0] + te[1] * te[1] + te[2] * te[2]
        val scaleYSq = te[4] * te[4] + te[5] * te[5] + te[6] * te[6]
        val scaleZSq = te[8] * te[8] + te[9] * te[9] + te[10] * te[10]

        return sqrt(maxOf(scaleXSq, scaleYSq, scaleZSq))
    }

    /**
     * Sets this matrix as a translation transform from the given vector [v].
     *
     * @return A reference to this matrix.
     */
    public fun makeTranslation(v: Vector3): Matrix4 {
        set(
            1.0, 0.0, 0.0, v.x,
            0.0, 1.0, 0.0, v.y,
            0.0, 0.0, 1.0, v.z,
            0.0, 0.0, 0.0, 1.0,
        )

        return this
    }

    /**
     * Sets this matrix as a translation transform.
     *
     * @param x The amount to translate in the X axis.
     * @param y The amount to translate in the Y axis.
     * @param z The amount to translate in the Z axis.
     * @return A reference to this matrix.
     */
    public fun makeTranslation(x: Double, y: Double, z: Double): Matrix4 {
        set(
            1.0, 0.0, 0.0, x,
            0.0, 1.0, 0.0, y,
            0.0, 0.0, 1.0, z,
            0.0, 0.0, 0.0, 1.0,
        )

        return this
    }

    /**
     * Sets this matrix as a rotational transformation around the X axis by the given
     * angle [theta] (in radians).
     *
     * @return A reference to this matrix.
     */
    public fun makeRotationX(theta: Double): Matrix4 {
        val c = cos(theta); val s = sin(theta)

        set(
            1.0, 0.0, 0.0, 0.0,
            0.0, c, -s, 0.0,
            0.0, s, c, 0.0,
            0.0, 0.0, 0.0, 1.0,
        )

        return this
    }

    /**
     * Sets this matrix as a rotational transformation around the Y axis by the given
     * angle [theta] (in radians).
     *
     * @return A reference to this matrix.
     */
    public fun makeRotationY(theta: Double): Matrix4 {
        val c = cos(theta); val s = sin(theta)

        set(
            c, 0.0, s, 0.0,
            0.0, 1.0, 0.0, 0.0,
            -s, 0.0, c, 0.0,
            0.0, 0.0, 0.0, 1.0,
        )

        return this
    }

    /**
     * Sets this matrix as a rotational transformation around the Z axis by the given
     * angle [theta] (in radians).
     *
     * @return A reference to this matrix.
     */
    public fun makeRotationZ(theta: Double): Matrix4 {
        val c = cos(theta); val s = sin(theta)

        set(
            c, -s, 0.0, 0.0,
            s, c, 0.0, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0,
        )

        return this
    }

    /**
     * Sets this matrix as a rotational transformation around the given [axis] by the
     * given angle [angle] (in radians).
     *
     * @param axis The normalized rotation axis.
     * @param angle The rotation in radians.
     * @return A reference to this matrix.
     */
    public fun makeRotationAxis(axis: Vector3, angle: Double): Matrix4 {
        // Based on http://www.gamedev.net/reference/articles/article1199.asp

        val c = cos(angle)
        val s = sin(angle)
        val t = 1.0 - c
        val x = axis.x; val y = axis.y; val z = axis.z
        val tx = t * x; val ty = t * y

        set(
            tx * x + c, tx * y - s * z, tx * z + s * y, 0.0,
            tx * y + s * z, ty * y + c, ty * z - s * x, 0.0,
            tx * z - s * y, ty * z + s * x, t * z * z + c, 0.0,
            0.0, 0.0, 0.0, 1.0,
        )

        return this
    }

    /**
     * Sets this matrix as a scale transformation.
     *
     * @param x The amount to scale in the X axis.
     * @param y The amount to scale in the Y axis.
     * @param z The amount to scale in the Z axis.
     * @return A reference to this matrix.
     */
    public fun makeScale(x: Double, y: Double, z: Double): Matrix4 {
        set(
            x, 0.0, 0.0, 0.0,
            0.0, y, 0.0, 0.0,
            0.0, 0.0, z, 0.0,
            0.0, 0.0, 0.0, 1.0,
        )

        return this
    }

    /**
     * Sets this matrix as a shear transformation.
     *
     * @param xy The amount to shear X by Y.
     * @param xz The amount to shear X by Z.
     * @param yx The amount to shear Y by X.
     * @param yz The amount to shear Y by Z.
     * @param zx The amount to shear Z by X.
     * @param zy The amount to shear Z by Y.
     * @return A reference to this matrix.
     */
    public fun makeShear(
        xy: Double, xz: Double, yx: Double, yz: Double, zx: Double, zy: Double,
    ): Matrix4 {
        set(
            1.0, yx, zx, 0.0,
            xy, 1.0, zy, 0.0,
            xz, yz, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0,
        )

        return this
    }

    /**
     * Sets this matrix to the transformation composed of the given [position],
     * rotation ([quaternion]) and [scale].
     *
     * @return A reference to this matrix.
     */
    public fun compose(position: Vector3, quaternion: Quaternion, scale: Vector3): Matrix4 {
        val te = elements

        val x = quaternion.x; val y = quaternion.y; val z = quaternion.z; val w = quaternion.w
        val x2 = x + x; val y2 = y + y; val z2 = z + z
        val xx = x * x2; val xy = x * y2; val xz = x * z2
        val yy = y * y2; val yz = y * z2; val zz = z * z2
        val wx = w * x2; val wy = w * y2; val wz = w * z2

        val sx = scale.x; val sy = scale.y; val sz = scale.z

        te[0] = (1.0 - (yy + zz)) * sx
        te[1] = (xy + wz) * sx
        te[2] = (xz - wy) * sx
        te[3] = 0.0

        te[4] = (xy - wz) * sy
        te[5] = (1.0 - (xx + zz)) * sy
        te[6] = (yz + wx) * sy
        te[7] = 0.0

        te[8] = (xz + wy) * sz
        te[9] = (yz - wx) * sz
        te[10] = (1.0 - (xx + yy)) * sz
        te[11] = 0.0

        te[12] = position.x
        te[13] = position.y
        te[14] = position.z
        te[15] = 1.0

        return this
    }

    /**
     * Decomposes this matrix into its [position], rotation ([quaternion]) and
     * [scale] components and stores the result in the given objects.
     *
     * Note: not all matrices are decomposable in this way. For example, if an object
     * has a non-uniformly scaled parent, then the object's world matrix may not be
     * decomposable, and this method may not be appropriate.
     *
     * @return A reference to this matrix.
     */
    public fun decompose(position: Vector3, quaternion: Quaternion, scale: Vector3): Matrix4 {
        val te = elements

        position.x = te[12]
        position.y = te[13]
        position.z = te[14]

        val det = determinant()

        if (det == 0.0) {
            scale.set(1.0, 1.0, 1.0)
            quaternion.identity()

            return this
        }

        // three.js reuses module-level `_v1`/`_m1` scratch; locals are race-free.
        val v1 = Vector3()
        var sx = v1.set(te[0], te[1], te[2]).length()
        val sy = v1.set(te[4], te[5], te[6]).length()
        val sz = v1.set(te[8], te[9], te[10]).length()

        // if determinant is negative, we need to invert one scale
        if (det < 0.0) sx = -sx

        // scale the rotation part
        val m1 = Matrix4()
        m1.copy(this)

        val invSX = 1.0 / sx
        val invSY = 1.0 / sy
        val invSZ = 1.0 / sz

        m1.elements[0] *= invSX
        m1.elements[1] *= invSX
        m1.elements[2] *= invSX

        m1.elements[4] *= invSY
        m1.elements[5] *= invSY
        m1.elements[6] *= invSY

        m1.elements[8] *= invSZ
        m1.elements[9] *= invSZ
        m1.elements[10] *= invSZ

        quaternion.setFromRotationMatrix(m1)

        scale.x = sx
        scale.y = sy
        scale.z = sz

        return this
    }

    /**
     * Creates a perspective projection matrix. Used internally by
     * `PerspectiveCamera.updateProjectionMatrix`.
     *
     * @param left Left boundary of the viewing frustum at the near plane.
     * @param right Right boundary of the viewing frustum at the near plane.
     * @param top Top boundary of the viewing frustum at the near plane.
     * @param bottom Bottom boundary of the viewing frustum at the near plane.
     * @param near The distance from the camera to the near plane.
     * @param far The distance from the camera to the far plane.
     * @param coordinateSystem The target clip-space coordinate system.
     * @param reversedDepth Whether to use a reversed depth.
     * @return A reference to this matrix.
     */
    public fun makePerspective(
        left: Double,
        right: Double,
        top: Double,
        bottom: Double,
        near: Double,
        far: Double,
        coordinateSystem: CoordinateSystem = CoordinateSystem.WebGL,
        reversedDepth: Boolean = false,
    ): Matrix4 {
        val te = elements

        val x = 2.0 * near / (right - left)
        val y = 2.0 * near / (top - bottom)

        val a = (right + left) / (right - left)
        val b = (top + bottom) / (top - bottom)

        val c: Double
        val d: Double

        if (reversedDepth) {
            c = near / (far - near)
            d = (far * near) / (far - near)
        } else {
            when (coordinateSystem) {
                CoordinateSystem.WebGL -> {
                    c = -(far + near) / (far - near)
                    d = (-2.0 * far * near) / (far - near)
                }

                CoordinateSystem.WebGPU -> {
                    c = -far / (far - near)
                    d = (-far * near) / (far - near)
                }
            }
        }

        te[0] = x; te[4] = 0.0; te[8] = a; te[12] = 0.0
        te[1] = 0.0; te[5] = y; te[9] = b; te[13] = 0.0
        te[2] = 0.0; te[6] = 0.0; te[10] = c; te[14] = d
        te[3] = 0.0; te[7] = 0.0; te[11] = -1.0; te[15] = 0.0

        return this
    }

    /**
     * Creates an orthographic projection matrix. Used internally by
     * `OrthographicCamera.updateProjectionMatrix`.
     *
     * @param left Left boundary of the viewing frustum at the near plane.
     * @param right Right boundary of the viewing frustum at the near plane.
     * @param top Top boundary of the viewing frustum at the near plane.
     * @param bottom Bottom boundary of the viewing frustum at the near plane.
     * @param near The distance from the camera to the near plane.
     * @param far The distance from the camera to the far plane.
     * @param coordinateSystem The target clip-space coordinate system.
     * @param reversedDepth Whether to use a reversed depth.
     * @return A reference to this matrix.
     */
    public fun makeOrthographic(
        left: Double,
        right: Double,
        top: Double,
        bottom: Double,
        near: Double,
        far: Double,
        coordinateSystem: CoordinateSystem = CoordinateSystem.WebGL,
        reversedDepth: Boolean = false,
    ): Matrix4 {
        val te = elements

        val x = 2.0 / (right - left)
        val y = 2.0 / (top - bottom)

        val a = -(right + left) / (right - left)
        val b = -(top + bottom) / (top - bottom)

        val c: Double
        val d: Double

        if (reversedDepth) {
            c = 1.0 / (far - near)
            d = far / (far - near)
        } else {
            when (coordinateSystem) {
                CoordinateSystem.WebGL -> {
                    c = -2.0 / (far - near)
                    d = -(far + near) / (far - near)
                }

                CoordinateSystem.WebGPU -> {
                    c = -1.0 / (far - near)
                    d = -near / (far - near)
                }
            }
        }

        te[0] = x; te[4] = 0.0; te[8] = 0.0; te[12] = a
        te[1] = 0.0; te[5] = y; te[9] = 0.0; te[13] = b
        te[2] = 0.0; te[6] = 0.0; te[10] = c; te[14] = d
        te[3] = 0.0; te[7] = 0.0; te[11] = 0.0; te[15] = 1.0

        return this
    }

    // three.js's `equals(matrix)` (exact, element-wise) is expressed by the
    // `equals(Any?)` override below, so `==` and `.equals` stay in sync (dialect
    // rule 11 — no bare same-type overload). Tests use `a == b`.

    /**
     * Sets the elements of the matrix from the given [array] (column-major order).
     *
     * @param offset Index of the first element in the array.
     * @return A reference to this matrix.
     */
    public fun fromArray(array: DoubleArray, offset: Int = 0): Matrix4 {
        for (i in 0 until 16) {
            elements[i] = array[i + offset]
        }

        return this
    }

    /**
     * Writes the elements of this matrix into [array] at [offset] (column-major
     * order), growing the list as needed, and returns it.
     *
     * @param offset Index of the first element in the array.
     */
    public fun toArray(array: MutableList<Double> = mutableListOf(), offset: Int = 0): MutableList<Double> {
        val te = elements

        while (array.size < offset + 16) array.add(0.0)

        array[offset] = te[0]
        array[offset + 1] = te[1]
        array[offset + 2] = te[2]
        array[offset + 3] = te[3]

        array[offset + 4] = te[4]
        array[offset + 5] = te[5]
        array[offset + 6] = te[6]
        array[offset + 7] = te[7]

        array[offset + 8] = te[8]
        array[offset + 9] = te[9]
        array[offset + 10] = te[10]
        array[offset + 11] = te[11]

        array[offset + 12] = te[12]
        array[offset + 13] = te[13]
        array[offset + 14] = te[14]
        array[offset + 15] = te[15]

        return array
    }

    /**
     * Structural equality: `true` when [other] is a [Matrix4] whose [elements] are
     * element-wise equal (via [DoubleArray.contentEquals], so `NaN != NaN`).
     */
    override fun equals(other: Any?): Boolean =
        other is Matrix4 && other.elements.contentEquals(elements)

    override fun hashCode(): Int = elements.contentHashCode()

    override fun toString(): String = "Matrix4(elements=${elements.joinToString()})"
}
