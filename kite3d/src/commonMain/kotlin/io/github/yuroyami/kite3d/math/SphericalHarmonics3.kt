/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/SphericalHarmonics3.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

/**
 * Represents a third-order spherical harmonics (SH). Light probes use this class
 * to encode lighting information.
 *
 * - Primary reference: [https://graphics.stanford.edu/papers/envmap/envmap.pdf]
 * - Secondary reference: [https://www.ppsloan.org/publications/StupidSH36.pdf]
 *
 * Instances are **mutable** and **not thread-safe**; confine an instance to a single
 * thread, exactly as in three.js.
 *
 * The `isSphericalHarmonics3` duck-typing flag is intentionally dropped
 * (dialect rule 10); use `is SphericalHarmonics3` instead.
 */
public class SphericalHarmonics3 {

    /**
     * An array holding the (9) SH coefficients.
     *
     * Exposed as a `val` (dialect rule 12): three.js never reassigns the array,
     * only mutates the [Vector3] entries.
     */
    public val coefficients: Array<Vector3> = Array(9) { Vector3() }

    /**
     * Sets the given SH coefficients to this instance by copying the values.
     *
     * @param coefficients The SH coefficients.
     * @return A reference to this spherical harmonics.
     */
    public fun set(coefficients: Array<Vector3>): SphericalHarmonics3 {
        for (i in 0 until 9) {
            this.coefficients[i].copy(coefficients[i])
        }

        return this
    }

    /**
     * Sets all SH coefficients to `0`.
     *
     * @return A reference to this spherical harmonics.
     */
    public fun zero(): SphericalHarmonics3 {
        for (i in 0 until 9) {
            coefficients[i].set(0.0, 0.0, 0.0)
        }

        return this
    }

    /**
     * Returns the radiance in the direction of the given normal.
     *
     * @param normal The normal vector (assumed to be unit length).
     * @param target The target vector that is used to store the method's result.
     * @return The radiance.
     */
    public fun getAt(normal: Vector3, target: Vector3): Vector3 {
        // normal is assumed to be unit length
        val x = normal.x
        val y = normal.y
        val z = normal.z

        val coeff = coefficients

        // band 0
        target.copy(coeff[0]).multiplyScalar(0.282095)

        // band 1
        target.addScaledVector(coeff[1], 0.488603 * y)
        target.addScaledVector(coeff[2], 0.488603 * z)
        target.addScaledVector(coeff[3], 0.488603 * x)

        // band 2
        target.addScaledVector(coeff[4], 1.092548 * (x * y))
        target.addScaledVector(coeff[5], 1.092548 * (y * z))
        target.addScaledVector(coeff[6], 0.315392 * (3.0 * z * z - 1.0))
        target.addScaledVector(coeff[7], 1.092548 * (x * z))
        target.addScaledVector(coeff[8], 0.546274 * (x * x - y * y))

        return target
    }

    /**
     * Returns the irradiance (radiance convolved with cosine lobe) in the
     * direction of the given normal.
     *
     * @param normal The normal vector (assumed to be unit length).
     * @param target The target vector that is used to store the method's result.
     * @return The irradiance.
     */
    public fun getIrradianceAt(normal: Vector3, target: Vector3): Vector3 {
        // normal is assumed to be unit length
        val x = normal.x
        val y = normal.y
        val z = normal.z

        val coeff = coefficients

        // band 0
        target.copy(coeff[0]).multiplyScalar(0.886227) // π * 0.282095

        // band 1
        target.addScaledVector(coeff[1], 2.0 * 0.511664 * y) // ( 2 * π / 3 ) * 0.488603
        target.addScaledVector(coeff[2], 2.0 * 0.511664 * z)
        target.addScaledVector(coeff[3], 2.0 * 0.511664 * x)

        // band 2
        target.addScaledVector(coeff[4], 2.0 * 0.429043 * x * y) // ( π / 4 ) * 1.092548
        target.addScaledVector(coeff[5], 2.0 * 0.429043 * y * z)
        target.addScaledVector(coeff[6], 0.743125 * z * z - 0.247708) // ( π / 4 ) * 0.315392 * 3
        target.addScaledVector(coeff[7], 2.0 * 0.429043 * x * z)
        target.addScaledVector(coeff[8], 0.429043 * (x * x - y * y)) // ( π / 4 ) * 0.546274

        return target
    }

    /**
     * Adds the given SH to this instance.
     *
     * @param sh The SH to add.
     * @return A reference to this spherical harmonics.
     */
    public fun add(sh: SphericalHarmonics3): SphericalHarmonics3 {
        for (i in 0 until 9) {
            coefficients[i].add(sh.coefficients[i])
        }

        return this
    }

    /**
     * A convenience method for performing [add] and [scale] at once.
     *
     * @param sh The SH to add.
     * @param s The scale factor.
     * @return A reference to this spherical harmonics.
     */
    public fun addScaledSH(sh: SphericalHarmonics3, s: Double): SphericalHarmonics3 {
        for (i in 0 until 9) {
            coefficients[i].addScaledVector(sh.coefficients[i], s)
        }

        return this
    }

    /**
     * Scales this SH by the given scale factor.
     *
     * @param s The scale factor.
     * @return A reference to this spherical harmonics.
     */
    public fun scale(s: Double): SphericalHarmonics3 {
        for (i in 0 until 9) {
            coefficients[i].multiplyScalar(s)
        }

        return this
    }

    /**
     * Linear interpolates between the given SH and this instance by the given
     * alpha factor.
     *
     * @param sh The SH to interpolate with.
     * @param alpha The alpha factor.
     * @return A reference to this spherical harmonics.
     */
    public fun lerp(sh: SphericalHarmonics3, alpha: Double): SphericalHarmonics3 {
        for (i in 0 until 9) {
            coefficients[i].lerp(sh.coefficients[i], alpha)
        }

        return this
    }

    /**
     * Copies the values of the given spherical harmonics to this instance.
     *
     * @param sh The spherical harmonics to copy.
     * @return A reference to this spherical harmonics.
     */
    public fun copy(sh: SphericalHarmonics3): SphericalHarmonics3 = set(sh.coefficients)

    /**
     * Returns a new spherical harmonics with copied values from this instance.
     *
     * @return A clone of this instance.
     */
    public fun clone(): SphericalHarmonics3 = SphericalHarmonics3().copy(this)

    /**
     * Sets the SH coefficients of this instance from the given array.
     *
     * @param array An array holding the SH coefficients.
     * @param offset The array offset where to start copying.
     * @return A reference to this spherical harmonics.
     */
    public fun fromArray(array: DoubleArray, offset: Int = 0): SphericalHarmonics3 {
        val coefficients = this.coefficients

        for (i in 0 until 9) {
            coefficients[i].fromArray(array, offset + (i * 3))
        }

        return this
    }

    /**
     * Returns an array with the SH coefficients, or copies them into the provided
     * array. The coefficients are represented as numbers.
     *
     * @param array The target array.
     * @param offset The array offset where to start copying.
     * @return An array with flat SH coefficients.
     */
    public fun toArray(
        array: MutableList<Double> = mutableListOf(),
        offset: Int = 0,
    ): MutableList<Double> {
        val coefficients = this.coefficients

        for (i in 0 until 9) {
            coefficients[i].toArray(array, offset + (i * 3))
        }

        return array
    }

    /**
     * Structural equality: `true` when [other] is a [SphericalHarmonics3] whose nine
     * coefficients are all equal (via [Vector3.equals], so `NaN != NaN`).
     *
     * Mirrors three.js's `SphericalHarmonics3.equals`, but as the [equals] override
     * (dialect rule 11) it also tolerates a non-[SphericalHarmonics3] / null argument.
     */
    override fun equals(other: Any?): Boolean {
        if (other !is SphericalHarmonics3) return false

        for (i in 0 until 9) {
            if (coefficients[i] != other.coefficients[i]) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        var result = 1
        for (i in 0 until 9) {
            result = 31 * result + coefficients[i].hashCode()
        }
        return result
    }

    override fun toString(): String =
        "SphericalHarmonics3(coefficients=${coefficients.joinToString()})"

    public companion object {

        /**
         * Computes the SH basis for the given normal vector.
         *
         * @param normal The normal.
         * @param shBasis The target array holding the SH basis.
         */
        public fun getBasisAt(normal: Vector3, shBasis: DoubleArray) {
            // normal is assumed to be unit length
            val x = normal.x
            val y = normal.y
            val z = normal.z

            // band 0
            shBasis[0] = 0.282095

            // band 1
            shBasis[1] = 0.488603 * y
            shBasis[2] = 0.488603 * z
            shBasis[3] = 0.488603 * x

            // band 2
            shBasis[4] = 1.092548 * x * y
            shBasis[5] = 1.092548 * y * z
            shBasis[6] = 0.315392 * (3.0 * z * z - 1.0)
            shBasis[7] = 1.092548 * x * z
            shBasis[8] = 0.546274 * (x * x - y * y)
        }
    }
}
