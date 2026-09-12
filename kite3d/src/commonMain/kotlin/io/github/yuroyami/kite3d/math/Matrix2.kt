/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Matrix2.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

/**
 * Represents a 2x2 matrix.
 *
 * [set] takes its arguments in row-major order, so a call reads the way you
 * write the matrix on paper. [elements] stores them in column-major order:
 * `Matrix2().set(11.0, 12.0, 21.0, 22.0)` stores `[11, 21, 12, 22]`. There is
 * no constructor that takes the elements.
 *
 * Matrices are **mutable** and **not thread-safe**. Use each instance from one
 * thread only.
 */
public class Matrix2 {

    /**
     * A column-major list of matrix values (length 4).
     */
    public val elements: DoubleArray = doubleArrayOf(
        1.0, 0.0,
        0.0, 1.0,
    )

    /**
     * Sets this matrix to the 2x2 identity matrix.
     *
     * @return A reference to this matrix.
     */
    public fun identity(): Matrix2 {

        set(
            1.0, 0.0,
            0.0, 1.0,
        )

        return this

    }

    /**
     * Sets the elements of the matrix from the given [array].
     *
     * @param array The matrix elements in column-major order.
     * @param offset Index of the first element in the array.
     * @return A reference to this matrix.
     */
    public fun fromArray(array: DoubleArray, offset: Int = 0): Matrix2 {

        for (i in 0 until 4) {

            elements[i] = array[i + offset]

        }

        return this

    }

    /**
     * Sets the elements of the matrix. The arguments are supposed to be in
     * row-major order.
     *
     * @param n11 1-1 matrix element.
     * @param n12 1-2 matrix element.
     * @param n21 2-1 matrix element.
     * @param n22 2-2 matrix element.
     * @return A reference to this matrix.
     */
    public fun set(n11: Double, n12: Double, n21: Double, n22: Double): Matrix2 {

        val te = elements

        te[0] = n11; te[2] = n12
        te[1] = n21; te[3] = n22

        return this

    }

    /**
     * Structural equality: `true` when [other] is a [Matrix2] whose [elements] are
     * element-wise equal (via `contentEquals`).
     */
    override fun equals(other: Any?): Boolean =
        other is Matrix2 && other.elements.contentEquals(elements)

    override fun hashCode(): Int = elements.contentHashCode()

    override fun toString(): String = "Matrix2(elements=${elements.joinToString()})"
}
