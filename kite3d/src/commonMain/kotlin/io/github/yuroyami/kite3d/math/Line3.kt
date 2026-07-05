/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Line3.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

/**
 * An analytical line segment in 3D space represented by a start and end point.
 *
 * The segment is **mutable** and **not thread-safe**; confine an instance (and any
 * object graph holding it) to a single thread, exactly as in three.js.
 *
 * The constructor stores the given vectors **by reference** (it does not copy
 * them). Passing the same vector as both endpoints — or sharing a vector with
 * another object — means later mutations are visible through every alias; use
 * [set] or [clone] when you need independent storage.
 */
public class Line3(
    /** Start of the line segment. */
    public val start: Vector3 = Vector3(),
    /** End of the line segment. */
    public val end: Vector3 = Vector3(),
) {

    /**
     * Sets the start and end values by copying the given vectors.
     *
     * @return A reference to this line segment.
     */
    public fun set(start: Vector3, end: Vector3): Line3 {
        this.start.copy(start)
        this.end.copy(end)

        return this
    }

    /**
     * Copies the values of the given line segment [line] to this instance.
     *
     * @return A reference to this line segment.
     */
    public fun copy(line: Line3): Line3 {
        start.copy(line.start)
        end.copy(line.end)

        return this
    }

    /**
     * Writes the center of the line segment into [target].
     *
     * @return [target].
     */
    public fun getCenter(target: Vector3): Vector3 =
        target.addVectors(start, end).multiplyScalar(0.5)

    /**
     * Writes the delta vector of the line segment's start and end point into
     * [target].
     *
     * @return [target].
     */
    public fun delta(target: Vector3): Vector3 =
        target.subVectors(end, start)

    /**
     * Returns the squared Euclidean distance between the line's start and end point.
     */
    public fun distanceSq(): Double =
        start.distanceToSquared(end)

    /**
     * Returns the Euclidean distance between the line's start and end point.
     */
    public fun distance(): Double =
        start.distanceTo(end)

    /**
     * Writes a point at position [t] (in `[0,1]`) along the line segment into
     * [target].
     *
     * @return [target].
     */
    public fun at(t: Double, target: Vector3): Vector3 =
        delta(target).multiplyScalar(t).add(start)

    /**
     * Returns a point parameter based on the closest point as projected on the
     * line segment.
     *
     * @param point The point for which to return a point parameter.
     * @param clampToLine Whether to clamp the result to the range `[0,1]` or not.
     * @return The point parameter.
     */
    public fun closestPointToPointParameter(point: Vector3, clampToLine: Boolean): Double {
        // three.js uses module-level _startP/_startEnd scratch vectors; allocated
        // locally here (no file-level mutable state).
        val startP = Vector3().subVectors(point, start)
        val startEnd = Vector3().subVectors(end, start)

        val startEnd2 = startEnd.dot(startEnd)

        if (startEnd2 == 0.0) return 0.0

        val startEndStartP = startEnd.dot(startP)

        var t = startEndStartP / startEnd2

        if (clampToLine) {
            t = MathUtils.clamp(t, 0.0, 1.0)
        }

        return t
    }

    /**
     * Writes the closest point on the line for a given [point] into [target].
     *
     * @param point The point to compute the closest point on the line for.
     * @param clampToLine Whether to clamp the result to the range `[0,1]` or not.
     * @param target The target vector that is used to store the method's result.
     * @return The closest point on the line.
     */
    public fun closestPointToPoint(point: Vector3, clampToLine: Boolean, target: Vector3): Vector3 {
        val t = closestPointToPointParameter(point, clampToLine)

        return delta(target).multiplyScalar(t).add(start)
    }

    /**
     * Returns the closest squared distance between this line segment and the given
     * one [line]. When provided, writes the closest point on this segment into [c1]
     * and the closest point on [line] into [c2].
     *
     * @param line The line segment to compute the closest squared distance to.
     * @param c1 The closest point on this line segment (default: a fresh local vector).
     * @param c2 The closest point on the given line segment (default: a fresh local vector).
     * @return The squared distance between this line segment and the given one.
     */
    public fun distanceSqToLine3(line: Line3, c1: Vector3 = Vector3(), c2: Vector3 = Vector3()): Double {
        // from Real-Time Collision Detection by Christer Ericson, chapter 5.1.9

        // Computes closest points C1 and C2 of S1(s)=P1+s*(Q1-P1) and
        // S2(t)=P2+t*(Q2-P2), returning s and t. Function result is squared
        // distance between between S1(s) and S2(t)

        val epsilon = 1e-8 * 1e-8 // must be squared since we compare squared length
        val s: Double
        val t: Double
        // three.js uses module-level _d1/_d2/_r scratch vectors; allocated locally.
        val d1 = Vector3()
        val d2 = Vector3()
        val r = Vector3()

        val p1 = start
        val p2 = line.start
        val q1 = end
        val q2 = line.end

        d1.subVectors(q1, p1) // Direction vector of segment S1
        d2.subVectors(q2, p2) // Direction vector of segment S2
        r.subVectors(p1, p2)

        val a = d1.dot(d1) // Squared length of segment S1, always nonnegative
        val e = d2.dot(d2) // Squared length of segment S2, always nonnegative
        val f = d2.dot(r)

        // Check if either or both segments degenerate into points

        if (a <= epsilon && e <= epsilon) {
            // Both segments degenerate into points

            c1.copy(p1)
            c2.copy(p2)

            c1.sub(c2)

            return c1.dot(c1)
        }

        if (a <= epsilon) {
            // First segment degenerates into a point

            s = 0.0
            t = MathUtils.clamp(f / e, 0.0, 1.0) // s = 0 => t = (b*s + f) / e = f / e
        } else {
            val c = d1.dot(r)

            if (e <= epsilon) {
                // Second segment degenerates into a point

                t = 0.0
                s = MathUtils.clamp(-c / a, 0.0, 1.0) // t = 0 => s = (b*t - c) / a = -c / a
            } else {
                // The general nondegenerate case starts here

                val b = d1.dot(d2)
                val denom = a * e - b * b // Always nonnegative

                // If segments not parallel, compute closest point on L1 to L2 and
                // clamp to segment S1. Else pick arbitrary s (here 0)

                val sTmp = if (denom != 0.0) {
                    MathUtils.clamp((b * f - c * e) / denom, 0.0, 1.0)
                } else {
                    0.0
                }

                // Compute point on L2 closest to S1(s) using
                // t = Dot((P1 + D1*s) - P2,D2) / Dot(D2,D2) = (b*s + f) / e

                var tTmp = (b * sTmp + f) / e

                // If t in [0,1] done. Else clamp t, recompute s for the new value
                // of t using s = Dot((P2 + D2*t) - P1,D1) / Dot(D1,D1)= (t*b - c) / a
                // and clamp s to [0, 1]

                var sFinal = sTmp
                if (tTmp < 0) {
                    tTmp = 0.0
                    sFinal = MathUtils.clamp(-c / a, 0.0, 1.0)
                } else if (tTmp > 1) {
                    tTmp = 1.0
                    sFinal = MathUtils.clamp((b - c) / a, 0.0, 1.0)
                }

                s = sFinal
                t = tTmp
            }
        }

        c1.copy(p1).addScaledVector(d1, s)
        c2.copy(p2).addScaledVector(d2, t)

        return c1.distanceToSquared(c2)
    }

    /**
     * Applies a 4x4 transformation matrix [matrix] to this line segment.
     *
     * @return A reference to this line segment.
     */
    public fun applyMatrix4(matrix: Matrix4): Line3 {
        start.applyMatrix4(matrix)
        end.applyMatrix4(matrix)

        return this
    }

    /**
     * Returns a new line segment with copied values from this instance.
     *
     * @return A clone of this instance.
     */
    public fun clone(): Line3 = Line3().copy(this)

    /**
     * Structural equality: `true` when [other] is a [Line3] with equal [start] and
     * [end] (each compared via [Vector3.equals], so `NaN != NaN`).
     */
    override fun equals(other: Any?): Boolean =
        other is Line3 && other.start == start && other.end == end

    override fun hashCode(): Int = 31 * start.hashCode() + end.hashCode()

    override fun toString(): String = "Line3(start=$start, end=$end)"
}
