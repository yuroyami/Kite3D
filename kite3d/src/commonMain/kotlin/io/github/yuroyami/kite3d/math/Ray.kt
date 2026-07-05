/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Ray.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * A ray that emits from an origin in a certain direction. The class is used by
 * `Raycaster` to assist with raycasting (e.g. mouse picking — working out which
 * objects in 3D space the pointer is over).
 *
 * The ray is **mutable** and **not thread-safe**; confine an instance (and any
 * object graph holding it) to a single thread, exactly as in three.js.
 *
 * The constructor stores the given vectors **by reference** (it does not copy
 * them); sharing a vector with another object means later mutations are visible
 * through every alias. Use [set] or [clone] when you need independent storage.
 */
public class Ray(
    /** The origin of the ray. */
    public val origin: Vector3 = Vector3(),
    /** The (normalized) direction of the ray. */
    public val direction: Vector3 = Vector3(0.0, 0.0, -1.0),
) {

    /**
     * Copies the given [origin] and [direction] into this ray.
     *
     * @return A reference to this ray.
     */
    public fun set(origin: Vector3, direction: Vector3): Ray {
        this.origin.copy(origin)
        this.direction.copy(direction)

        return this
    }

    /**
     * Copies the values of [ray] into this instance.
     *
     * @return A reference to this ray.
     */
    public fun copy(ray: Ray): Ray {
        origin.copy(ray.origin)
        direction.copy(ray.direction)

        return this
    }

    /**
     * Writes the point located distance [t] along this ray into [target].
     *
     * @return [target].
     */
    public fun at(t: Double, target: Vector3): Vector3 =
        target.copy(origin).addScaledVector(direction, t)

    /**
     * Points this ray's direction at [v] in world space.
     *
     * @return A reference to this ray.
     */
    public fun lookAt(v: Vector3): Ray {
        direction.copy(v).sub(origin).normalize()

        return this
    }

    /**
     * Shifts the origin of this ray along its direction by distance [t].
     *
     * @return A reference to this ray.
     */
    public fun recast(t: Double): Ray {
        val vector = Vector3()
        origin.copy(at(t, vector))

        return this
    }

    /**
     * Writes the point on this ray closest to [point] into [target].
     *
     * @return [target].
     */
    public fun closestPointToPoint(point: Vector3, target: Vector3): Vector3 {
        target.subVectors(point, origin)

        val directionDistance = target.dot(direction)

        if (directionDistance < 0) {
            return target.copy(origin)
        }

        return target.copy(origin).addScaledVector(direction, directionDistance)
    }

    /**
     * Returns the distance of closest approach between this ray and [point].
     */
    public fun distanceToPoint(point: Vector3): Double =
        sqrt(distanceSqToPoint(point))

    /**
     * Returns the squared distance of closest approach between this ray and [point].
     */
    public fun distanceSqToPoint(point: Vector3): Double {
        val vector = Vector3()
        val directionDistance = vector.subVectors(point, origin).dot(direction)

        // point behind the ray

        if (directionDistance < 0) {
            return origin.distanceToSquared(point)
        }

        vector.copy(origin).addScaledVector(direction, directionDistance)

        return vector.distanceToSquared(point)
    }

    /**
     * Returns the squared distance between this ray and the line segment `[v0, v1]`.
     *
     * When provided, [optionalPointOnRay] receives the point on this ray closest to
     * the segment, and [optionalPointOnSegment] receives the point on the segment
     * closest to this ray.
     *
     * @return The squared distance.
     */
    public fun distanceSqToSegment(
        v0: Vector3,
        v1: Vector3,
        optionalPointOnRay: Vector3? = null,
        optionalPointOnSegment: Vector3? = null,
    ): Double {
        // from https://github.com/pmjoniak/GeometricTools/blob/master/GTEngine/Include/Mathematics/GteDistRaySegment.h
        // It returns the min distance between the ray and the segment
        // defined by v0 and v1
        // It can also set two optional targets :
        // - The closest point on the ray
        // - The closest point on the segment

        // Local scratch (three.js reuses module-level _segCenter/_segDir/_diff).
        val segCenter = Vector3()
        val segDir = Vector3()
        val diff = Vector3()

        segCenter.copy(v0).add(v1).multiplyScalar(0.5)
        segDir.copy(v1).sub(v0).normalize()
        diff.copy(origin).sub(segCenter)

        val segExtent = v0.distanceTo(v1) * 0.5
        val a01 = -direction.dot(segDir)
        val b0 = diff.dot(direction)
        val b1 = -diff.dot(segDir)
        val c = diff.lengthSq()
        val det = abs(1 - a01 * a01)
        var s0: Double
        var s1: Double
        var sqrDist: Double
        val extDet: Double

        if (det > 0) {
            // The ray and segment are not parallel.

            s0 = a01 * b1 - b0
            s1 = a01 * b0 - b1
            extDet = segExtent * det

            if (s0 >= 0) {
                if (s1 >= -extDet) {
                    if (s1 <= extDet) {
                        // region 0
                        // Minimum at interior points of ray and segment.

                        val invDet = 1 / det
                        s0 *= invDet
                        s1 *= invDet
                        sqrDist = s0 * (s0 + a01 * s1 + 2 * b0) + s1 * (a01 * s0 + s1 + 2 * b1) + c
                    } else {
                        // region 1

                        s1 = segExtent
                        s0 = maxOf(0.0, -(a01 * s1 + b0))
                        sqrDist = -s0 * s0 + s1 * (s1 + 2 * b1) + c
                    }
                } else {
                    // region 5

                    s1 = -segExtent
                    s0 = maxOf(0.0, -(a01 * s1 + b0))
                    sqrDist = -s0 * s0 + s1 * (s1 + 2 * b1) + c
                }
            } else {
                if (s1 <= -extDet) {
                    // region 4

                    s0 = maxOf(0.0, -(-a01 * segExtent + b0))
                    s1 = if (s0 > 0) -segExtent else minOf(maxOf(-segExtent, -b1), segExtent)
                    sqrDist = -s0 * s0 + s1 * (s1 + 2 * b1) + c
                } else if (s1 <= extDet) {
                    // region 3

                    s0 = 0.0
                    s1 = minOf(maxOf(-segExtent, -b1), segExtent)
                    sqrDist = s1 * (s1 + 2 * b1) + c
                } else {
                    // region 2

                    s0 = maxOf(0.0, -(a01 * segExtent + b0))
                    s1 = if (s0 > 0) segExtent else minOf(maxOf(-segExtent, -b1), segExtent)
                    sqrDist = -s0 * s0 + s1 * (s1 + 2 * b1) + c
                }
            }
        } else {
            // Ray and segment are parallel.

            s1 = if (a01 > 0) -segExtent else segExtent
            s0 = maxOf(0.0, -(a01 * s1 + b0))
            sqrDist = -s0 * s0 + s1 * (s1 + 2 * b1) + c
        }

        if (optionalPointOnRay != null) {
            optionalPointOnRay.copy(origin).addScaledVector(direction, s0)
        }

        if (optionalPointOnSegment != null) {
            optionalPointOnSegment.copy(segCenter).addScaledVector(segDir, s1)
        }

        return sqrDist
    }

    /**
     * Intersects this ray with [sphere], writing the intersection point into
     * [target].
     *
     * @return [target] on a hit, or `null` if there is no intersection.
     */
    public fun intersectSphere(sphere: Sphere, target: Vector3): Vector3? {
        val vector = Vector3()
        vector.subVectors(sphere.center, origin)
        val tca = vector.dot(direction)
        val d2 = vector.dot(vector) - tca * tca
        val radius2 = sphere.radius * sphere.radius

        if (d2 > radius2) return null

        val thc = sqrt(radius2 - d2)

        // t0 = first intersect point - entrance on front of sphere
        val t0 = tca - thc

        // t1 = second intersect point - exit point on back of sphere
        val t1 = tca + thc

        // test to see if t1 is behind the ray - if so, return null
        if (t1 < 0) return null

        // test to see if t0 is behind the ray:
        // if it is, the ray is inside the sphere, so return the second exit point scaled by t1,
        // in order to always return an intersect point that is in front of the ray.
        if (t0 < 0) return at(t1, target)

        // else t0 is in front of the ray, so return the first collision point scaled by t0
        return at(t0, target)
    }

    /**
     * Returns `true` if this ray intersects [sphere].
     */
    public fun intersectsSphere(sphere: Sphere): Boolean {
        if (sphere.radius < 0) return false // handle empty spheres, see #31187

        return distanceSqToPoint(sphere.center) <= (sphere.radius * sphere.radius)
    }

    /**
     * Returns the distance from this ray's origin to [plane], or `null` if the ray
     * does not intersect the plane.
     */
    public fun distanceToPlane(plane: Plane): Double? {
        val denominator = plane.normal.dot(direction)

        if (denominator == 0.0) {
            // line is coplanar, return origin
            if (plane.distanceToPoint(origin) == 0.0) {
                return 0.0
            }

            // Null is preferable to undefined since undefined means.... it is undefined

            return null
        }

        val t = -(origin.dot(plane.normal) + plane.constant) / denominator

        // Return if the ray never intersects the plane

        return if (t >= 0) t else null
    }

    /**
     * Intersects this ray with [plane], writing the intersection point into
     * [target].
     *
     * @return [target] on a hit, or `null` if there is no intersection.
     */
    public fun intersectPlane(plane: Plane, target: Vector3): Vector3? {
        val t = distanceToPlane(plane)

        if (t == null) {
            return null
        }

        return at(t, target)
    }

    /**
     * Returns `true` if this ray intersects [plane].
     */
    public fun intersectsPlane(plane: Plane): Boolean {
        // check if the ray lies on the plane first

        val distToPoint = plane.distanceToPoint(origin)

        if (distToPoint == 0.0) {
            return true
        }

        val denominator = plane.normal.dot(direction)

        if (denominator * distToPoint < 0) {
            return true
        }

        // ray origin is behind the plane (and is pointing behind it)

        return false
    }

    /**
     * Intersects this ray with the bounding box [box], writing the intersection
     * point into [target].
     *
     * @return [target] on a hit, or `null` if there is no intersection.
     */
    public fun intersectBox(box: Box3, target: Vector3): Vector3? {
        // three.js declares tmin/tmax as reassigned (var) and the ty*/tz* pairs as
        // single-assignment (val).
        var tMin: Double
        var tMax: Double
        val tymin: Double
        val tymax: Double
        val tzmin: Double
        val tzmax: Double

        val invdirx = 1 / direction.x
        val invdiry = 1 / direction.y
        val invdirz = 1 / direction.z

        val origin = this.origin

        if (invdirx >= 0) {
            tMin = (box.min.x - origin.x) * invdirx
            tMax = (box.max.x - origin.x) * invdirx
        } else {
            tMin = (box.max.x - origin.x) * invdirx
            tMax = (box.min.x - origin.x) * invdirx
        }

        if (invdiry >= 0) {
            tymin = (box.min.y - origin.y) * invdiry
            tymax = (box.max.y - origin.y) * invdiry
        } else {
            tymin = (box.max.y - origin.y) * invdiry
            tymax = (box.min.y - origin.y) * invdiry
        }

        if ((tMin > tymax) || (tymin > tMax)) return null

        if (tymin > tMin || tMin.isNaN()) tMin = tymin

        if (tymax < tMax || tMax.isNaN()) tMax = tymax

        if (invdirz >= 0) {
            tzmin = (box.min.z - origin.z) * invdirz
            tzmax = (box.max.z - origin.z) * invdirz
        } else {
            tzmin = (box.max.z - origin.z) * invdirz
            tzmax = (box.min.z - origin.z) * invdirz
        }

        if ((tMin > tzmax) || (tzmin > tMax)) return null

        if (tzmin > tMin || tMin.isNaN()) tMin = tzmin

        if (tzmax < tMax || tMax.isNaN()) tMax = tzmax

        // return point closest to the ray (positive side)

        if (tMax < 0) return null

        return at(if (tMin >= 0) tMin else tMax, target)
    }

    /**
     * Returns `true` if this ray intersects [box].
     */
    public fun intersectsBox(box: Box3): Boolean {
        val vector = Vector3()
        return intersectBox(box, vector) != null
    }

    /**
     * Intersects this ray with the triangle `(a, b, c)`, writing the intersection
     * point into [target].
     *
     * @param backfaceCulling if `true`, backfacing triangles are not intersected.
     * @return [target] on a hit, or `null` if there is no intersection.
     */
    public fun intersectTriangle(
        a: Vector3,
        b: Vector3,
        c: Vector3,
        backfaceCulling: Boolean,
        target: Vector3,
    ): Vector3? {
        // Compute the offset origin, edges, and normal.

        // from https://github.com/pmjoniak/GeometricTools/blob/master/GTEngine/Include/Mathematics/GteIntrRay3Triangle3.h

        // Local scratch (three.js reuses module-level _edge1/_edge2/_normal/_diff).
        val edge1 = Vector3()
        val edge2 = Vector3()
        val normal = Vector3()
        val diff = Vector3()

        edge1.subVectors(b, a)
        edge2.subVectors(c, a)
        normal.crossVectors(edge1, edge2)

        // Solve Q + t*D = b1*E1 + b2*E2 (Q = kDiff, D = ray direction,
        // E1 = kEdge1, E2 = kEdge2, N = Cross(E1,E2)) by
        //   |Dot(D,N)|*b1 = sign(Dot(D,N))*Dot(D,Cross(Q,E2))
        //   |Dot(D,N)|*b2 = sign(Dot(D,N))*Dot(D,Cross(E1,Q))
        //   |Dot(D,N)|*t = -sign(Dot(D,N))*Dot(Q,N)
        var DdN = direction.dot(normal)
        val sign: Double

        if (DdN > 0) {
            if (backfaceCulling) return null
            sign = 1.0
        } else if (DdN < 0) {
            sign = -1.0
            DdN = -DdN
        } else {
            return null
        }

        diff.subVectors(origin, a)
        val DdQxE2 = sign * direction.dot(edge2.crossVectors(diff, edge2))

        // b1 < 0, no intersection
        if (DdQxE2 < 0) {
            return null
        }

        val DdE1xQ = sign * direction.dot(edge1.cross(diff))

        // b2 < 0, no intersection
        if (DdE1xQ < 0) {
            return null
        }

        // b1+b2 > 1, no intersection
        if (DdQxE2 + DdE1xQ > DdN) {
            return null
        }

        // Line intersects triangle, check if ray does.
        val QdN = -sign * diff.dot(normal)

        // t < 0, no intersection
        if (QdN < 0) {
            return null
        }

        // Ray intersects triangle.
        return at(QdN / DdN, target)
    }

    /**
     * Transforms this ray by the 4x4 matrix [matrix4].
     *
     * @return A reference to this ray.
     */
    public fun applyMatrix4(matrix4: Matrix4): Ray {
        origin.applyMatrix4(matrix4)
        direction.transformDirection(matrix4)

        return this
    }

    /**
     * Returns a new ray with copied values from this instance.
     *
     * @return A clone of this instance.
     */
    public fun clone(): Ray = Ray().copy(this)

    /**
     * Structural equality: `true` when [other] is a [Ray] with equal origin and
     * direction.
     */
    override fun equals(other: Any?): Boolean =
        other is Ray && other.origin == origin && other.direction == direction

    override fun hashCode(): Int = 31 * origin.hashCode() + direction.hashCode()

    override fun toString(): String = "Ray(origin=$origin, direction=$direction)"
}
