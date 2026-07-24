/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Matrix3.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.cos
import kotlin.math.sin

/**
 * Represents a 3x3 matrix.
 *
 * A note on row-major and column-major ordering: the [set] method (and the
 * `Matrix3(...)` element constructor, exposed as an `ext/` convenience) take
 * arguments in **row-major** order, while internally the values are stored in the
 * [elements] array in **column-major** order. This matches three.js exactly.
 *
 * Matrices are **mutable** and **not thread-safe**; confine an instance (and any
 * object graph holding it) to a single thread, exactly as in three.js. Most
 * methods mutate `this` and return it for chaining.
 */
public class Matrix3 {

    /**
     * A column-major list of matrix values. Initialized to the 3x3 identity.
     *
     * Exposed as a `val` (dialect rule 12): three.js never reassigns the array,
     * only mutates its contents, so the reference is fixed while the values stay
     * mutable.
     */
    public val elements: DoubleArray = doubleArrayOf(
        1.0, 0.0, 0.0,
        0.0, 1.0, 0.0,
        0.0, 0.0, 1.0,
    )

    /**
     * Sets the elements of the matrix. The arguments are supposed to be in
     * row-major order.
     *
     * @param n11 1-1 matrix element.
     * @param n12 1-2 matrix element.
     * @param n13 1-3 matrix element.
     * @param n21 2-1 matrix element.
     * @param n22 2-2 matrix element.
     * @param n23 2-3 matrix element.
     * @param n31 3-1 matrix element.
     * @param n32 3-2 matrix element.
     * @param n33 3-3 matrix element.
     * @return A reference to this matrix.
     */
    public fun set(
        n11: Double, n12: Double, n13: Double,
        n21: Double, n22: Double, n23: Double,
        n31: Double, n32: Double, n33: Double,
    ): Matrix3 {
        val te = elements

        te[0] = n11; te[1] = n21; te[2] = n31
        te[3] = n12; te[4] = n22; te[5] = n32
        te[6] = n13; te[7] = n23; te[8] = n33

        return this
    }

    /**
     * Sets this matrix to the 3x3 identity matrix.
     *
     * @return A reference to this matrix.
     */
    public fun identity(): Matrix3 {
        set(
            1.0, 0.0, 0.0,
            0.0, 1.0, 0.0,
            0.0, 0.0, 1.0,
        )

        return this
    }

    /**
     * Copies the values of the given matrix [m] to this instance.
     *
     * @return A reference to this matrix.
     */
    public fun copy(m: Matrix3): Matrix3 {
        val te = elements
        val me = m.elements

        te[0] = me[0]; te[1] = me[1]; te[2] = me[2]
        te[3] = me[3]; te[4] = me[4]; te[5] = me[5]
        te[6] = me[6]; te[7] = me[7]; te[8] = me[8]

        return this
    }

    /**
     * Extracts the basis of this matrix into the three axis vectors provided.
     *
     * @param xAxis The basis's x axis.
     * @param yAxis The basis's y axis.
     * @param zAxis The basis's z axis.
     * @return A reference to this matrix.
     */
    public fun extractBasis(xAxis: Vector3, yAxis: Vector3, zAxis: Vector3): Matrix3 {
        xAxis.setFromMatrix3Column(this, 0)
        yAxis.setFromMatrix3Column(this, 1)
        zAxis.setFromMatrix3Column(this, 2)

        return this
    }

    /**
     * Sets this matrix to the upper 3x3 matrix of the given 4x4 matrix [m].
     *
     * @return A reference to this matrix.
     */
    public fun setFromMatrix4(m: Matrix4): Matrix3 {
        val me = m.elements

        set(
            me[0], me[4], me[8],
            me[1], me[5], me[9],
            me[2], me[6], me[10],
        )

        return this
    }

    /**
     * Post-multiplies this matrix by the given 3x3 matrix [m].
     *
     * @return A reference to this matrix.
     */
    public fun multiply(m: Matrix3): Matrix3 {
        return multiplyMatrices(this, m)
    }

    /**
     * Pre-multiplies this matrix by the given 3x3 matrix [m].
     *
     * @return A reference to this matrix.
     */
    public fun premultiply(m: Matrix3): Matrix3 {
        return multiplyMatrices(m, this)
    }

    /**
     * Multiplies the given 3x3 matrices [a] and [b] and stores the result in this
     * matrix.
     *
     * @return A reference to this matrix.
     */
    public fun multiplyMatrices(a: Matrix3, b: Matrix3): Matrix3 {
        val ae = a.elements
        val be = b.elements
        val te = elements

        val a11 = ae[0]; val a12 = ae[3]; val a13 = ae[6]
        val a21 = ae[1]; val a22 = ae[4]; val a23 = ae[7]
        val a31 = ae[2]; val a32 = ae[5]; val a33 = ae[8]

        val b11 = be[0]; val b12 = be[3]; val b13 = be[6]
        val b21 = be[1]; val b22 = be[4]; val b23 = be[7]
        val b31 = be[2]; val b32 = be[5]; val b33 = be[8]

        te[0] = a11 * b11 + a12 * b21 + a13 * b31
        te[3] = a11 * b12 + a12 * b22 + a13 * b32
        te[6] = a11 * b13 + a12 * b23 + a13 * b33

        te[1] = a21 * b11 + a22 * b21 + a23 * b31
        te[4] = a21 * b12 + a22 * b22 + a23 * b32
        te[7] = a21 * b13 + a22 * b23 + a23 * b33

        te[2] = a31 * b11 + a32 * b21 + a33 * b31
        te[5] = a31 * b12 + a32 * b22 + a33 * b32
        te[8] = a31 * b13 + a32 * b23 + a33 * b33

        return this
    }

    /**
     * Multiplies every component of the matrix by the given scalar [s].
     *
     * @return A reference to this matrix.
     */
    public fun multiplyScalar(s: Double): Matrix3 {
        val te = elements

        te[0] *= s; te[3] *= s; te[6] *= s
        te[1] *= s; te[4] *= s; te[7] *= s
        te[2] *= s; te[5] *= s; te[8] *= s

        return this
    }

    /**
     * Computes and returns the determinant of this matrix.
     *
     * @return The determinant.
     */
    public fun determinant(): Double {
        val te = elements

        val a = te[0]; val b = te[1]; val c = te[2]
        val d = te[3]; val e = te[4]; val f = te[5]
        val g = te[6]; val h = te[7]; val i = te[8]

        return a * e * i - a * f * h - b * d * i + b * f * g + c * d * h - c * e * g
    }

    /**
     * Inverts this matrix, using the analytic method. You can not invert a matrix
     * with a determinant of zero; if you attempt this, the method produces a zero
     * matrix instead.
     *
     * @return A reference to this matrix.
     */
    public fun invert(): Matrix3 {
        val te = elements

        val n11 = te[0]; val n21 = te[1]; val n31 = te[2]
        val n12 = te[3]; val n22 = te[4]; val n32 = te[5]
        val n13 = te[6]; val n23 = te[7]; val n33 = te[8]

        val t11 = n33 * n22 - n32 * n23
        val t12 = n32 * n13 - n33 * n12
        val t13 = n23 * n12 - n22 * n13

        val det = n11 * t11 + n21 * t12 + n31 * t13

        if (det == 0.0) return set(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        val detInv = 1.0 / det

        te[0] = t11 * detInv
        te[1] = (n31 * n23 - n33 * n21) * detInv
        te[2] = (n32 * n21 - n31 * n22) * detInv

        te[3] = t12 * detInv
        te[4] = (n33 * n11 - n31 * n13) * detInv
        te[5] = (n31 * n12 - n32 * n11) * detInv

        te[6] = t13 * detInv
        te[7] = (n21 * n13 - n23 * n11) * detInv
        te[8] = (n22 * n11 - n21 * n12) * detInv

        return this
    }

    /**
     * Transposes this matrix in place.
     *
     * @return A reference to this matrix.
     */
    public fun transpose(): Matrix3 {
        var tmp: Double
        val m = elements

        tmp = m[1]; m[1] = m[3]; m[3] = tmp
        tmp = m[2]; m[2] = m[6]; m[6] = tmp
        tmp = m[5]; m[5] = m[7]; m[7] = tmp

        return this
    }

    /**
     * Computes the normal matrix, which is the inverse transpose of the upper-left
     * 3x3 portion of the given 4x4 matrix [matrix4].
     *
     * @return A reference to this matrix.
     */
    public fun getNormalMatrix(matrix4: Matrix4): Matrix3 {
        return setFromMatrix4(matrix4).invert().transpose()
    }

    /**
     * Transposes this matrix into the supplied array [r], and returns itself
     * unchanged.
     *
     * @return A reference to this matrix.
     */
    public fun transposeIntoArray(r: DoubleArray): Matrix3 {
        val m = elements

        r[0] = m[0]
        r[1] = m[3]
        r[2] = m[6]
        r[3] = m[1]
        r[4] = m[4]
        r[5] = m[7]
        r[6] = m[2]
        r[7] = m[5]
        r[8] = m[8]

        return this
    }

    /**
     * Sets the UV transform matrix from offset, repeat, rotation, and center.
     *
     * @param tx Offset x.
     * @param ty Offset y.
     * @param sx Repeat x.
     * @param sy Repeat y.
     * @param rotation Rotation, in radians. Positive values rotate counterclockwise.
     * @param cx Center x of rotation.
     * @param cy Center y of rotation.
     * @return A reference to this matrix.
     */
    public fun setUvTransform(
        tx: Double, ty: Double, sx: Double, sy: Double, rotation: Double, cx: Double, cy: Double,
    ): Matrix3 {
        val c = cos(rotation)
        val s = sin(rotation)

        set(
            sx * c, sx * s, -sx * (c * cx + s * cy) + cx + tx,
            -sy * s, sy * c, -sy * (-s * cx + c * cy) + cy + ty,
            0.0, 0.0, 1.0,
        )

        return this
    }

    /**
     * Scales this matrix with the given scalar values.
     *
     * @param sx The amount to scale in the X axis.
     * @param sy The amount to scale in the Y axis.
     * @return A reference to this matrix.
     */
    public fun scale(sx: Double, sy: Double): Matrix3 {
        // three.js uses a module-level `_m3` scratch; a local matches the dialect
        // (no file-level mutable scratch) at no fidelity cost.
        premultiply(Matrix3().makeScale(sx, sy))

        return this
    }

    /**
     * Rotates this matrix by the given angle [theta] (in radians).
     *
     * @return A reference to this matrix.
     */
    public fun rotate(theta: Double): Matrix3 {
        premultiply(Matrix3().makeRotation(-theta))

        return this
    }

    /**
     * Translates this matrix by the given scalar values.
     *
     * @param tx The amount to translate in the X axis.
     * @param ty The amount to translate in the Y axis.
     * @return A reference to this matrix.
     */
    public fun translate(tx: Double, ty: Double): Matrix3 {
        premultiply(Matrix3().makeTranslation(tx, ty))

        return this
    }

    // for 2D Transforms

    /**
     * Sets this matrix as a 2D translation transform from the translation vector [v].
     *
     * @return A reference to this matrix.
     */
    public fun makeTranslation(v: Vector2): Matrix3 {
        set(
            1.0, 0.0, v.x,
            0.0, 1.0, v.y,
            0.0, 0.0, 1.0,
        )

        return this
    }

    /**
     * Sets this matrix as a 2D translation transform.
     *
     * @param x The amount to translate in the X axis.
     * @param y The amount to translate in the Y axis.
     * @return A reference to this matrix.
     */
    public fun makeTranslation(x: Double, y: Double): Matrix3 {
        set(
            1.0, 0.0, x,
            0.0, 1.0, y,
            0.0, 0.0, 1.0,
        )

        return this
    }

    /**
     * Sets this matrix as a 2D rotational transformation (counterclockwise) by the
     * given angle [theta] (in radians).
     *
     * @return A reference to this matrix.
     */
    public fun makeRotation(theta: Double): Matrix3 {
        // counterclockwise

        val c = cos(theta)
        val s = sin(theta)

        set(
            c, -s, 0.0,
            s, c, 0.0,
            0.0, 0.0, 1.0,
        )

        return this
    }

    /**
     * Sets this matrix as a 2D scale transform.
     *
     * @param x The amount to scale in the X axis.
     * @param y The amount to scale in the Y axis.
     * @return A reference to this matrix.
     */
    public fun makeScale(x: Double, y: Double): Matrix3 {
        set(
            x, 0.0, 0.0,
            0.0, y, 0.0,
            0.0, 0.0, 1.0,
        )

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
    public fun fromArray(array: DoubleArray, offset: Int = 0): Matrix3 {
        for (i in 0 until 9) {
            elements[i] = array[i + offset]
        }

        return this
    }

    /**
     * Writes the elements of this matrix into [array] at [offset] (column-major
     * order), growing the list as needed, and returns it.
     *
     * @param offset Index of the first element in the array.
     * @return The matrix elements in column-major order.
     */
    public fun toArray(array: MutableList<Double> = mutableListOf(), offset: Int = 0): MutableList<Double> {
        val te = elements

        while (array.size < offset + 9) array.add(0.0)

        array[offset] = te[0]
        array[offset + 1] = te[1]
        array[offset + 2] = te[2]

        array[offset + 3] = te[3]
        array[offset + 4] = te[4]
        array[offset + 5] = te[5]

        array[offset + 6] = te[6]
        array[offset + 7] = te[7]
        array[offset + 8] = te[8]

        return array
    }

    /**
     * Returns a new matrix with copied values from this instance.
     *
     * @return A clone of this instance.
     */
    public fun clone(): Matrix3 = Matrix3().fromArray(elements)

    /**
     * Structural equality: `true` when [other] is a [Matrix3] whose [elements] are
     * element-wise equal (via [DoubleArray.contentEquals], so `NaN != NaN`).
     */
    override fun equals(other: Any?): Boolean =
        other is Matrix3 && other.elements.contentEquals(elements)

    override fun hashCode(): Int = elements.contentHashCode()

    override fun toString(): String = "Matrix3(elements=${elements.joinToString()})"
}
