/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Spherical.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Represents points in 3D space as
 * [spherical coordinates](https://en.wikipedia.org/wiki/Spherical_coordinate_system).
 *
 * Instances are **mutable** and **not thread-safe**; confine an instance to a
 * single thread, exactly as in three.js.
 *
 * @param radius The radius, or the Euclidean distance (straight-line distance)
 *   from the point to the origin.
 * @param phi The polar angle in radians from the y (up) axis.
 * @param theta The equator/azimuthal angle in radians around the y (up) axis.
 */
public class Spherical(
    /** The radius, or the Euclidean distance (straight-line distance) from the point to the origin. */
    public var radius: Double = 1.0,
    /** The polar angle in radians from the y (up) axis. */
    public var phi: Double = 0.0,
    /** The equator/azimuthal angle in radians around the y (up) axis. */
    public var theta: Double = 0.0,
) {

    /**
     * Sets the spherical components by copying the given values.
     *
     * @param radius The radius.
     * @param phi The polar angle.
     * @param theta The azimuthal angle.
     * @return A reference to this spherical.
     */
    public fun set(radius: Double, phi: Double, theta: Double): Spherical {

        this.radius = radius
        this.phi = phi
        this.theta = theta

        return this

    }

    /**
     * Copies the values of the given spherical to this instance.
     *
     * @param other The spherical to copy.
     * @return A reference to this spherical.
     */
    public fun copy(other: Spherical): Spherical {

        radius = other.radius
        phi = other.phi
        theta = other.theta

        return this

    }

    /**
     * Restricts the polar angle [phi] to be between `0.000001` and `PI - 0.000001`.
     *
     * @return A reference to this spherical.
     */
    public fun makeSafe(): Spherical {

        phi = MathUtils.clamp(phi, EPS, PI - EPS)

        return this

    }

    /**
     * Sets the spherical components from the given vector which is assumed to hold
     * Cartesian coordinates.
     *
     * @param v The vector to set.
     * @return A reference to this spherical.
     */
    public fun setFromVector3(v: Vector3): Spherical {

        return setFromCartesianCoords(v.x, v.y, v.z)

    }

    /**
     * Sets the spherical components from the given Cartesian coordinates.
     *
     * @param x The x value.
     * @param y The y value.
     * @param z The z value.
     * @return A reference to this spherical.
     */
    public fun setFromCartesianCoords(x: Double, y: Double, z: Double): Spherical {

        radius = sqrt(x * x + y * y + z * z)

        if (radius == 0.0) {

            theta = 0.0
            phi = 0.0

        } else {

            theta = atan2(x, z)
            phi = acos(MathUtils.clamp(y / radius, -1.0, 1.0))

        }

        return this

    }

    /**
     * Returns a new spherical with copied values from this instance.
     *
     * @return A clone of this instance.
     */
    public fun clone(): Spherical = Spherical().copy(this)

    /**
     * Structural equality: `true` when [other] is a [Spherical] with equal
     * components (component-wise `==`, so `NaN != NaN`).
     */
    override fun equals(other: Any?): Boolean =
        other is Spherical && other.radius == radius && other.phi == phi && other.theta == theta

    override fun hashCode(): Int {
        // (v + 0.0) collapses -0.0 to +0.0, keeping hashCode consistent with
        // equals treating -0.0 == +0.0.
        var result = (radius + 0.0).hashCode()
        result = 31 * result + (phi + 0.0).hashCode()
        result = 31 * result + (theta + 0.0).hashCode()
        return result
    }

    override fun toString(): String = "Spherical(radius=$radius, phi=$phi, theta=$theta)"

    private companion object {
        /** The epsilon used by [makeSafe] to clamp [phi] away from the poles. */
        const val EPS: Double = 0.000001
    }
}
