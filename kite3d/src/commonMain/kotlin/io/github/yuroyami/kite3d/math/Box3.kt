/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Box3.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.abs

/**
 * An axis-aligned bounding box (AABB) in 3D space.
 *
 * The box is **mutable** and **not thread-safe**; confine an instance (and any
 * object graph holding it) to a single thread, exactly as in three.js. Most
 * methods mutate `this` and return it for chaining.
 *
 * The constructor stores the given vectors **by reference** (it does not copy
 * them). Passing the same vector as both bounds — or sharing a vector with another
 * object — means later mutations are visible through every alias; use [set] or
 * [clone] when you need independent storage.
 */
public class Box3(
    /** The lower (min) boundary of the box. */
    public val min: Vector3 = Vector3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
    /** The upper (max) boundary of the box. */
    public val max: Vector3 = Vector3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY),
) {

    /**
     * Copies the values of [min] and [max] into this box's bounds.
     *
     * @return A reference to this box.
     */
    public fun set(min: Vector3, max: Vector3): Box3 {
        this.min.copy(min)
        this.max.copy(max)
        return this
    }

    /**
     * Expands this box to enclose the 3D position data packed in [array] (x, y, z
     * triples). Fewer than three trailing entries are ignored.
     *
     * @return A reference to this box.
     */
    public fun setFromArray(array: DoubleArray): Box3 {
        makeEmpty()

        // Local scratch vector (three.js reuses a module-level _vector) — allocate
        // locally so the box carries no shared mutable state (dialect rule 13).
        val vector = Vector3()
        var i = 0
        val il = array.size
        while (i < il) {
            expandByPoint(vector.fromArray(array, i))
            i += 3
        }

        return this
    }

    /**
     * Expands this box to enclose the first [count] 3D positions in [attribute].
     *
     * three.js reads `attribute.count`; the [AttributeLike] seam does not expose a
     * count, so the vertex count is passed explicitly as [count] (see PORTING.md
     * "Cross-layer forward dependencies"). The real `BufferAttribute` supplies its
     * own `.count` at the call site once the core layer is ported.
     *
     * @return A reference to this box.
     */
    public fun setFromBufferAttribute(attribute: AttributeLike, count: Int): Box3 {
        makeEmpty()

        val vector = Vector3()
        for (i in 0 until count) {
            expandByPoint(vector.fromBufferAttribute(attribute, i))
        }

        return this
    }

    /**
     * Expands this box to enclose all the given [points]. An empty sequence leaves
     * the box empty.
     *
     * @return A reference to this box.
     */
    public fun setFromPoints(points: Iterable<Vector3>): Box3 {
        makeEmpty()

        for (point in points) {
            expandByPoint(point)
        }

        return this
    }

    /**
     * Centers this box on [center] and sets its width, height and depth to [size].
     *
     * @return A reference to this box.
     */
    public fun setFromCenterAndSize(center: Vector3, size: Vector3): Box3 {
        // Local scratch (three.js reuses the module-level _vector).
        val halfSize = Vector3().copy(size).multiplyScalar(0.5)

        min.copy(center).sub(halfSize)
        max.copy(center).add(halfSize)

        return this
    }

    // setFromObject / expandByObject need core `Object3D`/`Mesh`/geometry types
    // (updateWorldMatrix, geometry.getAttribute, getVertexPosition, boundingBox,
    // computeBoundingBox, matrixWorld). Those live above the math layer, so both
    // methods are deferred to a later object-aware module (see port-ledger.yaml).
    // Their tests (setFromObject/BufferGeometry, setFromObject/Precise,
    // expandByObject) are skipped in Box3Test.kt with matching comments.

    /**
     * Returns a new box with copied bounds (independent storage).
     *
     * @return A clone of this instance.
     */
    public fun clone(): Box3 = Box3().copy(this)

    /**
     * Copies the bounds of [box] into this instance.
     *
     * @return A reference to this box.
     */
    public fun copy(box: Box3): Box3 {
        min.copy(box.min)
        max.copy(box.max)
        return this
    }

    /**
     * Makes this box empty (encloses zero space): min = +infinity, max = -infinity.
     *
     * @return A reference to this box.
     */
    public fun makeEmpty(): Box3 {
        min.x = Double.POSITIVE_INFINITY
        min.y = Double.POSITIVE_INFINITY
        min.z = Double.POSITIVE_INFINITY
        max.x = Double.NEGATIVE_INFINITY
        max.y = Double.NEGATIVE_INFINITY
        max.z = Double.NEGATIVE_INFINITY

        return this
    }

    /**
     * Returns `true` if this box encloses no volume. A box with equal lower and
     * upper bounds is **not** empty — it still contains the single shared point.
     *
     * @return Whether this box is empty or not.
     */
    public fun isEmpty(): Boolean {
        // More robust than (volume <= 0), which can go positive with two negative axes.
        return (max.x < min.x) || (max.y < min.y) || (max.z < min.z)
    }

    /**
     * Writes the center of this box into [target] (the zero vector if empty).
     *
     * @return [target].
     */
    public fun getCenter(target: Vector3): Vector3 =
        if (isEmpty()) target.set(0.0, 0.0, 0.0) else target.addVectors(min, max).multiplyScalar(0.5)

    /**
     * Writes the dimensions (width/height/depth) of this box into [target] (the
     * zero vector if empty).
     *
     * @return [target].
     */
    public fun getSize(target: Vector3): Vector3 =
        if (isEmpty()) target.set(0.0, 0.0, 0.0) else target.subVectors(max, min)

    /**
     * Expands this box to include [point].
     *
     * @return A reference to this box.
     */
    public fun expandByPoint(point: Vector3): Box3 {
        min.min(point)
        max.max(point)

        return this
    }

    /**
     * Expands this box equilaterally by [vector] (x on both horizontal sides, y on
     * both vertical sides, z on both depth sides).
     *
     * @return A reference to this box.
     */
    public fun expandByVector(vector: Vector3): Box3 {
        min.sub(vector)
        max.add(vector)

        return this
    }

    /**
     * Expands each dimension by [scalar]. A negative scalar contracts the box and
     * can invert it (making it [isEmpty]).
     *
     * @return A reference to this box.
     */
    public fun expandByScalar(scalar: Double): Box3 {
        min.addScalar(-scalar)
        max.addScalar(scalar)

        return this
    }

    /**
     * Returns `true` if [point] lies within or on the boundary of this box.
     *
     * @return Whether the bounding box contains the given point or not.
     */
    public fun containsPoint(point: Vector3): Boolean =
        point.x >= min.x && point.x <= max.x &&
            point.y >= min.y && point.y <= max.y &&
            point.z >= min.z && point.z <= max.z

    /**
     * Returns `true` if this box fully contains [box] (an identical box counts as
     * contained).
     *
     * @return Whether the bounding box contains the given bounding box or not.
     */
    public fun containsBox(box: Box3): Boolean =
        min.x <= box.min.x && box.max.x <= max.x &&
            min.y <= box.min.y && box.max.y <= max.y &&
            min.z <= box.min.z && box.max.z <= max.z

    /**
     * Writes [point] as a proportion of this box's width, height and depth into
     * [target].
     *
     * A dimension of size `0` yields a division by zero (`+/-Infinity` or `NaN`) in
     * the corresponding component.
     *
     * @return [target].
     */
    public fun getParameter(point: Vector3, target: Vector3): Vector3 =
        // This can potentially have a divide by zero if the box has a size
        // dimension of 0.
        target.set(
            (point.x - min.x) / (max.x - min.x),
            (point.y - min.y) / (max.y - min.y),
            (point.z - min.z) / (max.z - min.z),
        )

    /**
     * Returns `true` if [box] intersects this box (touching edges count).
     *
     * @return Whether the given bounding box intersects with this bounding box.
     */
    public fun intersectsBox(box: Box3): Boolean =
        // Using 6 splitting planes to rule out intersections.
        box.max.x >= min.x && box.min.x <= max.x &&
            box.max.y >= min.y && box.min.y <= max.y &&
            box.max.z >= min.z && box.min.z <= max.z

    /**
     * Returns `true` if [sphere] intersects this box.
     *
     * @return Whether the given bounding sphere intersects with this bounding box.
     */
    public fun intersectsSphere(sphere: Sphere): Boolean {
        // Find the point on the AABB closest to the sphere center.
        val vector = Vector3()
        clampPoint(sphere.center, vector)

        // If that point is inside the sphere, the AABB and sphere intersect.
        return vector.distanceToSquared(sphere.center) <= (sphere.radius * sphere.radius)
    }

    /**
     * Returns `true` if [plane] intersects this box.
     *
     * @return Whether the given plane intersects with this bounding box.
     */
    public fun intersectsPlane(plane: Plane): Boolean {
        // We compute the minimum and maximum dot product values. If those values
        // are on the same side (back or front) of the plane, then there is no
        // intersection.

        var min: Double
        var max: Double

        if (plane.normal.x > 0) {
            min = plane.normal.x * this.min.x
            max = plane.normal.x * this.max.x
        } else {
            min = plane.normal.x * this.max.x
            max = plane.normal.x * this.min.x
        }

        if (plane.normal.y > 0) {
            min += plane.normal.y * this.min.y
            max += plane.normal.y * this.max.y
        } else {
            min += plane.normal.y * this.max.y
            max += plane.normal.y * this.min.y
        }

        if (plane.normal.z > 0) {
            min += plane.normal.z * this.min.z
            max += plane.normal.z * this.max.z
        } else {
            min += plane.normal.z * this.max.z
            max += plane.normal.z * this.min.z
        }

        return (min <= -plane.constant && max >= -plane.constant)
    }

    /**
     * Returns `true` if [triangle] intersects this box.
     *
     * @return Whether the given triangle intersects with this bounding box.
     */
    public fun intersectsTriangle(triangle: Triangle): Boolean {
        if (isEmpty()) {
            return false
        }

        // Local scratch vectors (three.js reuses module-level temps).
        val center = Vector3()
        val extents = Vector3()
        val v0 = Vector3()
        val v1 = Vector3()
        val v2 = Vector3()
        val f0 = Vector3()
        val f1 = Vector3()
        val f2 = Vector3()
        val triangleNormal = Vector3()

        // compute box center and extents
        getCenter(center)
        extents.subVectors(max, center)

        // translate triangle to aabb origin
        v0.subVectors(triangle.a, center)
        v1.subVectors(triangle.b, center)
        v2.subVectors(triangle.c, center)

        // compute edge vectors for triangle
        f0.subVectors(v1, v0)
        f1.subVectors(v2, v1)
        f2.subVectors(v0, v2)

        // test against axes that are given by cross product combinations of the edges of the triangle and the edges of the aabb
        // make an axis testing of each of the 3 sides of the aabb against each of the 3 sides of the triangle = 9 axis of separation
        // axis_ij = u_i x f_j (u0, u1, u2 = face normals of aabb = x,y,z axes vectors since aabb is axis aligned)
        var axes = doubleArrayOf(
            0.0, -f0.z, f0.y, 0.0, -f1.z, f1.y, 0.0, -f2.z, f2.y,
            f0.z, 0.0, -f0.x, f1.z, 0.0, -f1.x, f2.z, 0.0, -f2.x,
            -f0.y, f0.x, 0.0, -f1.y, f1.x, 0.0, -f2.y, f2.x, 0.0,
        )
        if (!satForAxes(axes, v0, v1, v2, extents)) {
            return false
        }

        // test 3 face normals from the aabb
        axes = doubleArrayOf(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0)
        if (!satForAxes(axes, v0, v1, v2, extents)) {
            return false
        }

        // finally testing the face normal of the triangle
        // use already existing triangle edge vectors here
        triangleNormal.crossVectors(f0, f1)
        axes = doubleArrayOf(triangleNormal.x, triangleNormal.y, triangleNormal.z)

        return satForAxes(axes, v0, v1, v2, extents)
    }

    /**
     * Writes [point] clamped into this box's bounds into [target].
     *
     * @return [target].
     */
    public fun clampPoint(point: Vector3, target: Vector3): Vector3 =
        target.copy(point).clamp(min, max)

    /**
     * Returns the Euclidean distance from [point] to the nearest edge of this box
     * (`0` if inside). An empty box yields `+Infinity`.
     *
     * @return The euclidean distance.
     */
    public fun distanceToPoint(point: Vector3): Double {
        val vector = Vector3()
        return clampPoint(point, vector).distanceTo(point)
    }

    /**
     * Writes a bounding sphere that encloses this box into [target] (an empty
     * sphere if this box is empty).
     *
     * @return [target].
     */
    public fun getBoundingSphere(target: Sphere): Sphere {
        if (isEmpty()) {
            target.makeEmpty()
        } else {
            getCenter(target.center)

            val vector = Vector3()
            target.radius = getSize(vector).length() * 0.5
        }

        return target
    }

    /**
     * Intersects this box with [box] (keeps the overlapping region), making this box
     * empty if there is no overlap.
     *
     * @return A reference to this box.
     */
    public fun intersect(box: Box3): Box3 {
        min.max(box.min)
        max.min(box.max)

        // ensure that if there is no overlap, the result is fully empty, not slightly empty with non-inf/+inf values that will cause subsequence intersects to erroneously return valid values.
        if (isEmpty()) makeEmpty()

        return this
    }

    /**
     * Unions this box with [box] (grows to enclose both).
     *
     * @return A reference to this box.
     */
    public fun union(box: Box3): Box3 {
        min.min(box.min)
        max.max(box.max)

        return this
    }

    /**
     * Transforms this box by the 4x4 matrix [matrix]. The transform of an empty box
     * is an empty box.
     *
     * @return A reference to this box.
     */
    public fun applyMatrix4(matrix: Matrix4): Box3 {
        // transform of empty box is an empty box.
        if (isEmpty()) return this

        // Local scratch corners (three.js reuses the module-level _points array).
        // NOTE: I am using a binary pattern to specify all 2^3 combinations below
        val points = arrayOf(
            Vector3().set(min.x, min.y, min.z).applyMatrix4(matrix), // 000
            Vector3().set(min.x, min.y, max.z).applyMatrix4(matrix), // 001
            Vector3().set(min.x, max.y, min.z).applyMatrix4(matrix), // 010
            Vector3().set(min.x, max.y, max.z).applyMatrix4(matrix), // 011
            Vector3().set(max.x, min.y, min.z).applyMatrix4(matrix), // 100
            Vector3().set(max.x, min.y, max.z).applyMatrix4(matrix), // 101
            Vector3().set(max.x, max.y, min.z).applyMatrix4(matrix), // 110
            Vector3().set(max.x, max.y, max.z).applyMatrix4(matrix), // 111
        )

        setFromPoints(points.asIterable())

        return this
    }

    /**
     * Translates this box by [offset], moving both bounds.
     *
     * @return A reference to this box.
     */
    public fun translate(offset: Vector3): Box3 {
        min.add(offset)
        max.add(offset)

        return this
    }

    // toJSON / fromJSON port faithfully: three.js serializes to { min: [...], max:
    // [...] } via Vector3.toArray()/fromArray(). Kotlin has no JS plain-object /
    // JSON value type at this layer, so these are exposed as array round-trips.

    /**
     * Writes this box as `[minX, minY, minZ, maxX, maxY, maxZ]`.
     *
     * Mirrors three.js `toJSON()` (which produces `{ min: min.toArray(), max:
     * max.toArray() }`); flattened here since the math layer has no JS object type.
     *
     * @return Serialized structure with fields representing the object state.
     */
    public fun toJSON(): DoubleArray = doubleArrayOf(min.x, min.y, min.z, max.x, max.y, max.z)

    /**
     * Sets this box's bounds from [json] laid out as
     * `[minX, minY, minZ, maxX, maxY, maxZ]`.
     *
     * @return A reference to this box.
     */
    public fun fromJSON(json: DoubleArray): Box3 {
        min.fromArray(json, 0)
        max.fromArray(json, 3)
        return this
    }

    /**
     * Structural equality: `true` when [other] is a [Box3] with equal bounds.
     */
    override fun equals(other: Any?): Boolean =
        other is Box3 && other.min == min && other.max == max

    override fun hashCode(): Int = 31 * min.hashCode() + max.hashCode()

    override fun toString(): String = "Box3(min=$min, max=$max)"
}

private fun satForAxes(axes: DoubleArray, v0: Vector3, v1: Vector3, v2: Vector3, extents: Vector3): Boolean {
    // Local scratch axis (three.js reuses the module-level _testAxis).
    val testAxis = Vector3()

    var i = 0
    val j = axes.size - 3
    while (i <= j) {
        testAxis.fromArray(axes, i)
        // project the aabb onto the separating axis
        val r = extents.x * abs(testAxis.x) + extents.y * abs(testAxis.y) + extents.z * abs(testAxis.z)
        // project all 3 vertices of the triangle onto the separating axis
        val p0 = v0.dot(testAxis)
        val p1 = v1.dot(testAxis)
        val p2 = v2.dot(testAxis)
        // actual test, basically see if either of the most extreme of the triangle points intersects r
        if (maxOf(-maxOf(p0, p1, p2), minOf(p0, p1, p2)) > r) {
            // points of the projected triangle are outside the projected half-length of the aabb
            // the axis is separating and we can exit
            return false
        }

        i += 3
    }

    return true
}
