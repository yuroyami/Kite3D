/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Cylindrical.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Represents points in 3D space as
 * [cylindrical coordinates](https://en.wikipedia.org/wiki/Cylindrical_coordinate_system).
 *
 * Instances are **mutable** and **not thread-safe**; confine an instance to a
 * single thread, exactly as in three.js.
 *
 * @param radius The distance from the origin to a point in the x-z plane.
 * @param theta A counterclockwise angle in the x-z plane measured in radians from
 *   the positive z-axis.
 * @param y The height above the x-z plane.
 */
public class Cylindrical(
    /** The distance from the origin to a point in the x-z plane. */
    public var radius: Double = 1.0,
    /** A counterclockwise angle in the x-z plane measured in radians from the positive z-axis. */
    public var theta: Double = 0.0,
    /** The height above the x-z plane. */
    public var y: Double = 0.0,
) {

    /**
     * Sets the cylindrical components by copying the given values.
     *
     * @param radius The radius.
     * @param theta The theta angle.
     * @param y The height value.
     * @return A reference to this cylindrical.
     */
    public fun set(radius: Double, theta: Double, y: Double): Cylindrical {

        this.radius = radius
        this.theta = theta
        this.y = y

        return this

    }

    /**
     * Copies the values of the given cylindrical to this instance.
     *
     * @param other The cylindrical to copy.
     * @return A reference to this cylindrical.
     */
    public fun copy(other: Cylindrical): Cylindrical {

        radius = other.radius
        theta = other.theta
        y = other.y

        return this

    }

    /**
     * Sets the cylindrical components from the given vector which is assumed to
     * hold Cartesian coordinates.
     *
     * @param v The vector to set.
     * @return A reference to this cylindrical.
     */
    public fun setFromVector3(v: Vector3): Cylindrical {

        return setFromCartesianCoords(v.x, v.y, v.z)

    }

    /**
     * Sets the cylindrical components from the given Cartesian coordinates.
     *
     * @param x The x value.
     * @param y The y value.
     * @param z The z value.
     * @return A reference to this cylindrical.
     */
    public fun setFromCartesianCoords(x: Double, y: Double, z: Double): Cylindrical {

        radius = sqrt(x * x + z * z)
        theta = atan2(x, z)
        this.y = y

        return this

    }

    /**
     * Returns a new cylindrical with copied values from this instance.
     *
     * @return A clone of this instance.
     */
    public fun clone(): Cylindrical = Cylindrical().copy(this)

    /**
     * Structural equality: `true` when [other] is a [Cylindrical] with equal
     * components (component-wise `==`, so `NaN != NaN`).
     */
    override fun equals(other: Any?): Boolean =
        other is Cylindrical && other.radius == radius && other.theta == theta && other.y == y

    override fun hashCode(): Int {
        // (v + 0.0) collapses -0.0 to +0.0, keeping hashCode consistent with
        // equals treating -0.0 == +0.0.
        var result = (radius + 0.0).hashCode()
        result = 31 * result + (theta + 0.0).hashCode()
        result = 31 * result + (y + 0.0).hashCode()
        return result
    }

    override fun toString(): String = "Cylindrical(radius=$radius, theta=$theta, y=$y)"
}
